package io.github.qf6101.rmisdk.sample;

import io.github.qf6101.rmisdk.base.RMIBaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * User: qfeng
 * Date: 15-11-12 下午7:14
 * Usage: 随机数服务客户端（RMI客户端测试类）
 */
public class HelloClient extends RMIBaseClient<IHello> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloClient.class);
    //连续失败计数、报警阈值
    private int failCount = 0;
    private final int alertThreshold = 100;

    /**
     * 随机数服务客户端（RMI客户端测试类）
     *
     * @throws Exception
     */
    public HelloClient(String firstHostName,
                       int firstListenPort,
                       String secondHostName,
                       int secondListenPort) throws Exception {
        super(firstHostName, firstListenPort, secondHostName, secondListenPort);
    }

    /**
     * 向服务端请求随机数并返回
     *
     * @param requestID 请求ID
     * @return 随机数（从服务端获取得到）
     */
    public int nextNumber(int requestID) {
        //初始化返回值为-1
        int number = -1;
        //获取服务对象存根
        IHello h = getInstance();
        try {
            if (h != null) {
                //通过存根向服务端请求随机数
                number = h.randNumber(requestID);
                //重置连续失败计数
                failCount = 0;
                //打印日志信息
                LOGGER.info("RequestID " + requestID + " get number " + number);
            } else {
                //如果服务对象存根无效，打印日志信息，返回值为-1
                LOGGER.warn("Discard requestID " + requestID + " due to null instance");
            }
        } catch (RemoteException e) {
            //当连续失败计数到达阈值时，打印错误信息
            if (++failCount % alertThreshold == 0) {
                LOGGER.error("Receive random number fail over " + failCount + " times!", e);
            }
        }
        //返回随机数，错误情况下为默认值-1
        return number;
    }
}
