package io.github.qf6101.rmisdk.sample;

import io.github.qf6101.rmisdk.base.RMIBaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: qfeng
 * Date: 15-11-12 下午7:14
 * Usage: 随机数服务器
 */
public class HelloServer extends RMIBaseServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloServer.class);
    private int listenPort = -1;

    public HelloServer(int listenPort) {
        this.listenPort = listenPort;
    }

    /**
     * 启动随机数服务
     */
    public HelloServer start() {
        try {
            //初始化随机数服务对象，线程池大小为5，请求最长等待时间为3秒
            IHello helloService = new HelloService(listenPort, 5, 3);
            LOGGER.info("Successfully create hello service object");
            //将服务对象注册到RMI命名空间
            super.start(listenPort, "HelloService", helloService);
        } catch (Exception ex) {
            //启动出错时打印日志信息并退出进程
            LOGGER.error("Start hello server on " + listenPort + " fail!", ex);
            System.exit(1);
        }
        return this;
    }
}
