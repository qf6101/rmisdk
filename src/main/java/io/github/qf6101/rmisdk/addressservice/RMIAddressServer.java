package io.github.qf6101.rmisdk.addressservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: qfeng
 * Date: 15-11-11 上午11:53
 * Usage: 地址服务器
 */
public class RMIAddressServer extends RMIBoosting {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMIAddressServer.class);
    private int listenPort = -1;

    /**
     * 初始化地址服务器
     *
     * @param listenPort 地址服务器的启动端口
     */
    public RMIAddressServer(int listenPort) {
        this.listenPort = listenPort;
    }

    /**
     * 启动地址服务
     */
    public RMIAddressServer start() {
        try {
            //根据启动端口创建地址服务对象
            IRMIAddressService addressService = new RMIAddressService(listenPort);
            LOGGER.info("Successfully create address service object");
            //将地址服务对象注册到RMI命名空间
            super.start(listenPort, getRmiAddressServiceName(), addressService);
        } catch (Exception ex) {
            //启动出错时打印日志信息并退出进程
            LOGGER.error("Start address server fail!", ex);
            System.exit(1);
        }
        return this;
    }
}
