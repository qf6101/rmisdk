package io.github.qf6101.rmisdk.base;

import io.github.qf6101.rmisdk.util.IPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: qfeng
 * Date: 15-11-13 下午12:05
 * Usage: RMI服务基类，不可实例化
 */
public abstract class RMIBaseService extends UnicastRemoteObject implements IBound {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMIBaseService.class);
    //服务名称全称、线程池、请求等待最长时间（单位：秒）
    protected String fullServiceName = null;
    protected ExecutorService servicePool = null;
    protected int maxDelay = 3;

    /**
     * RMI服务基类，不可实例化
     *
     * @param listenPort      服务监听端口
     * @param servicePoolSize 线程池大小
     * @param maxDelay        请求等待最长时间（单位：秒）
     * @throws Exception
     */
    public RMIBaseService(int listenPort, int servicePoolSize, int maxDelay) throws Exception {
        //初始化服务名称全称，由三部分组成：类名、IP地址、监听端口
        fullServiceName = this.getClass().getSimpleName() + "@" + IPUtil.getIP() + ":" + listenPort;
        LOGGER.info("Successfully initialize service name: " + fullServiceName);
        //初始化线程池
        servicePool = Executors.newFixedThreadPool(servicePoolSize);
        LOGGER.info("Successfully create thread pool with size of : " + servicePoolSize);
        //初始化请求等待最长时间
        this.maxDelay = maxDelay;
        LOGGER.info("Successfully set maximum delay with " + maxDelay + " seconds");
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
        LOGGER.debug("[" + fullServiceName + "] heart beats to [" + clientMessage + "]");
        return fullServiceName;
    }
}
