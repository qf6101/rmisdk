package io.github.qf6101.rmisdk.base;

import io.github.qf6101.rmisdk.addressservice.RMIAddressClient;
import io.github.qf6101.rmisdk.util.CircularList;
import io.github.qf6101.rmisdk.util.IPUtil;
import io.github.qf6101.rmisdk.util.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * User: qfeng
 * Date: 15-11-11 下午5:00
 * Usage: RMI客户端基类，不可实例化
 */
public abstract class RMIBaseClient<T extends IBound> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMIBaseClient.class);
    //初始化参数
    private final int boundObjectsCheckingDelay = Parameters.RMIBaseClient_boundObjectsCheckingDelay;
    private final int forceRefreshingThreshold = Parameters.RMIBaseClient_forceRefreshingThreshold;
    //客户端IP、地址服务客户端、绑定对象列表（循环列表）
    private String IP = null;
    private RMIAddressClient addressClient = null;
    private volatile CircularList<T> boundObjects = null;

    /**
     * RMI客户端基类，不可实例化
     *
     * @param firstHostName    自举地址的主机名（如果为null，则从resources下的配置文件获取自举地址）
     * @param firstListenPort  自举地址的端口（大于1024）
     * @param secondHostName   备用自举地址的主机名（如果为null，则忽略它）
     * @param secondListenPort 备用自举地址的端口（大于1024，如果secondhostName为null，则忽略它）
     * @throws Exception
     */
    public RMIBaseClient(String firstHostName,
                         int firstListenPort,
                         String secondHostName,
                         int secondListenPort) throws Exception {
        //获取客户端IP
        IP = IPUtil.getIP();
        //初始化时，阻塞式实例化地址服务客户端（包含的守护线程开始运行），并绑定对象
        addressClient = new RMIAddressClient(firstHostName, firstListenPort, secondHostName, secondListenPort);
        refreshBoundObjects();
        //守护线程，定时检查绑定对象们的有效性
        Thread daemonThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //初始化强制刷新计数器
                int forceRefreshingCount = 0;
                while (true) {
                    //当到达计数阈值或绑定对象列表为空时，强制刷新绑定对象列表
                    if (forceRefreshingCount >= forceRefreshingThreshold
                            || boundObjects == null
                            || boundObjects.size() <= 0) {
                        refreshBoundObjects();
                        //重置强制刷新计数器
                        forceRefreshingCount = 0;
                    } else {
                        //检查每个绑定对象，将无效的绑定对象从绑定列表清除
                        List<T> removeBoundObjects = new ArrayList<T>();
                        for (int i = 0; i < boundObjects.size(); ++i) {
                            T boundObject = boundObjects.get(i);
                            try {
                                //采用心跳方式检测绑定对象的有效性
                                boundObject.heartBeat(IP);
                            } catch (RemoteException e) {
                                //将无效的对象加入删除对象列表
                                removeBoundObjects.add(boundObject);
                            }
                        }
                        //当删除对象数量达到总量一半时，强制刷新整个绑定对象列表
                        //否则将无效对象从绑定对象列表中删除
                        if (removeBoundObjects.size() * 2 > boundObjects.size()) {
                            refreshBoundObjects();
                            //重置强制刷新计数器
                            forceRefreshingCount = 0;
                        } else {
                            boundObjects.removeAll(removeBoundObjects);
                        }
                    }
                    //暂停一段时间再行检测，并将强制刷新计数自增
                    try {
                        Thread.sleep(boundObjectsCheckingDelay);
                        forceRefreshingCount++;
                    } catch (InterruptedException ex) {
                        LOGGER.error("", ex);
                    }
                }
            }
        });
        //启动守护线程
        daemonThread.start();
        LOGGER.info("Successfully start service demon thread");
    }

    /**
     * 刷新绑定对象列表
     */
    private void refreshBoundObjects() {
        //获取地址列表
        List<String> addresses = addressClient.getAddresses();
        //当地址列表不为空时，按照地址逐个获取绑定对象
        if (addresses != null) {
            //初始化刷新的绑定对象列表
            CircularList<T> replaceBoundObjects = new CircularList<T>();
            for (String addressString : addresses) {
                try {
                    //解析RMI地址结构
                    RMIAddress address = RMIAddress.parse(addressString);
                    //获取注册存根
                    Registry registry = LocateRegistry.getRegistry(address.hostName, address.port);
                    //从存根中查找绑定对象
                    T boundObj = (T) registry.lookup(address.serviceName);
                    //将绑定对象加入到刷新列表中
                    replaceBoundObjects.add(boundObj);
                    LOGGER.debug("Refreshed bound object from " + addressString);
                } catch (Exception ex) {
                    //查找失败，打印日志信息（失败原因）
                    LOGGER.error("Lookup address fail: " + addressString + " (" + ex.getMessage() + ")");
                }
            }
            //用刷新列表替换原有列表
            if (replaceBoundObjects.size() > 0) {
                boundObjects = replaceBoundObjects;
            }
        }
    }

    /**
     * 获取服务实例，异常情况下会发生两件事情：
     * （1）返回值为空，可能池子里没有实例可用；
     * （2）返回的服务对象不可用，调用时会发生异常。
     * 因此，需要对返回值做空值判断，并对方法调用做异常捕获。
     *
     * @return 服务实例对象
     */
    public T getInstance() {
        if (boundObjects != null) {
            return boundObjects.next();
        } else {
            return null;
        }
    }
}
