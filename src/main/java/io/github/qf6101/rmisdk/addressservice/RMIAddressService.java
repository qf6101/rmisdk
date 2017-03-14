package io.github.qf6101.rmisdk.addressservice;

import io.github.qf6101.rmisdk.util.IPUtil;
import io.github.qf6101.rmisdk.util.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * User: qfeng
 * Date: 15-11-11 上午9:50
 * Usage: 地址服务对象，实现地址服务接口
 */
public class RMIAddressService extends UnicastRemoteObject implements IRMIAddressService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMIAddressService.class);
    //声明远程对象的唯一uuid
    private static final long serialVersionUID = -6596622154817856774L;
    //初始化参数
    private final int addressLoadingDelay = Parameters.RMIAddressService_addressLoadingDelay;
    //地址列表、服务名称全称
    private volatile List<String> addresses = null;
    private String fullServiceName = null;

    /**
     * 地址服务对象，实现地址服务接口
     *
     * @param listenPort 地址服务的监听端口
     * @throws Exception
     */
    public RMIAddressService(int listenPort) throws Exception {
        //初始化服务名称全称，由三部分组成：类名、IP地址、监听端口
        fullServiceName = RMIAddressService.class.getSimpleName() + "@" + IPUtil.getIP() + ":" + listenPort;
        LOGGER.info("Successfully initialize service name: " + fullServiceName);
        //守护线程，定时刷新地址列表
        Thread addressLoadingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //从文件刷新地址列表
                        List<String> replaceAddresses = loadAddresses();
                        if (replaceAddresses != null) {
                            addresses = replaceAddresses;
                        }
                        //打印日志信息
                        LOGGER.info("Successfully refresh the address list");
                        for (String address : addresses) {
                            LOGGER.debug("Refreshed addresses include " + address);
                        }
                    } catch (Exception ex) {
                        //刷新出错时打印日志信息，然后继续刷新
                        LOGGER.error("", ex);
                    }
                    //暂停一段时间再行刷新
                    try {
                        Thread.sleep(addressLoadingDelay);
                    } catch (InterruptedException e) {
                        LOGGER.error("", e);
                    }
                }
            }
        });
        //启动守护线程
        addressLoadingThread.start();
        LOGGER.info("Successfully start address demon thread");
    }

    /**
     * 检测心跳
     *
     * @param clientMessage 客户端发上来的检测信息
     * @return 服务端返回心跳信息（服务名称全称）
     * @throws RemoteException
     */
    @Override
    public String heartBeat(String clientMessage) throws RemoteException {
        //打印调试日志信息
        LOGGER.debug("[" + fullServiceName + "] heart beats to [" + clientMessage + "]");
        //返回心跳信息（服务名称全称）
        return fullServiceName;
    }

    /**
     * 获取地址列表（返回值可能为空）
     *
     * @return 地址列表
     * @throws RemoteException
     */
    @Override
    public List<String> getAddresses() throws RemoteException {
        return addresses;
    }

    /**
     * 从文件刷新地址列表
     *
     * @return 地址列表
     * @throws Exception
     */
    private List<String> loadAddresses() throws Exception {
        //读取器对象、临时字符串（对应文件中的一行）
        BufferedReader bufRdr = null;
        String line = null;

        try {
            //初始化刷新的地址列表
            List<String> replaceAddresses = new ArrayList<String>(10);
            //获取文件地址（${user.dir}/conf/RMIServiceAddress.txt）
            String serviceAddressFile = System.getProperty("user.dir") + "/conf/RMIServiceAddress.txt";
            bufRdr = new BufferedReader(new InputStreamReader(new FileInputStream(serviceAddressFile), "UTF-8"));
            //打开读取器对象并逐行读取到地址列表
            while ((line = bufRdr.readLine()) != null) {
                //地址类表必须以rmi://开头，否则打印错误日志信息
                if (line.startsWith("rmi://")) {
                    //将文件中的当前行加入到地址列表中
                    replaceAddresses.add(line);
                } else {
                    LOGGER.error("Error address: " + line);
                }
            }
            //返回地址列表（可能为空）
            if (replaceAddresses.size() > 0) {
                return replaceAddresses;
            } else {
                return null;
            }
        } finally {
            //关闭文件读取器
            try {
                if (bufRdr != null) {
                    bufRdr.close();
                }
            } catch (Exception ex) {
                LOGGER.error("", ex);
            }
        }
    }

}
