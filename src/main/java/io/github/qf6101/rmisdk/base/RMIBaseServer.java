package io.github.qf6101.rmisdk.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * User: qfeng
 * Date: 15-11-13 上午11:48
 * Usage: RMI服务端基类，不可实例化
 */
public abstract class RMIBaseServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMIBaseServer.class);
    //服务名称、服务对象、注册存根
    private String serviceName = null;
    private Remote serviceObject = null;
    private Registry registry = null;

    /**
     * 启动服务
     *
     * @param listenPort    监听端口
     * @param serviceName   服务名称
     * @param serviceObject 服务对象
     */
    public void start(int listenPort, String serviceName, Remote serviceObject) {
        try {
            //记录服务名称和绑定对象，解除绑定时用
            this.serviceName = serviceName;
            this.serviceObject = serviceObject;
            //注册监听端口
            registry = getUsableRegistry(listenPort);
            LOGGER.info("Successfully create registry on port " + listenPort);
            //在命名空间中，将名称绑定到服务对象
            registry.rebind(serviceName, serviceObject);
            LOGGER.info("Successfully bind " + serviceName + " on port " + listenPort);
        } catch (Exception ex) {
            //启动出错时打印日志信息并退出进程
            LOGGER.error("Start " + serviceName + " on port " + listenPort + " fail!", ex);
            System.exit(1);
        }
    }

    /**
     * 获取有用的监听端口
     *
     * @param listenPort 监听端口
     * @return 注册存根
     * @throws RemoteException
     */
    private Registry getUsableRegistry(int listenPort) throws RemoteException {
        try {
            //首先尝试创建依赖该端口的注册存根
            return LocateRegistry.createRegistry(listenPort);
        } catch (RemoteException e) {
            //失败后尝试获取已存在的注册存根
            return LocateRegistry.getRegistry(listenPort);
        }
    }

    /**
     * 停止服务
     */
    public void stop() {
        try {
            //注册存根和绑定名称不能为空
            if (registry == null) {
                LOGGER.warn("Cannot stop null registry!");
            } else if (serviceName == null) {
                LOGGER.warn("Cannot stop null bind name!");
            } else {
                //解除绑定名称
                registry.unbind(serviceName);
                //强制解除监听端口的注册，即使当前仍然存在有客户端连接
                UnicastRemoteObject.unexportObject(serviceObject, true);
                LOGGER.info("Successfully stop " + serviceName);
            }
        } catch (Exception ex) {
            //启动出错时打印日志信息
            LOGGER.error("Stop " + serviceName + " fail!", ex);
        }
    }
}
