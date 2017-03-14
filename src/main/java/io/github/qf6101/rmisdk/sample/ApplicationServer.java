package io.github.qf6101.rmisdk.sample;

import io.github.qf6101.rmisdk.addressservice.RMIAddressServer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: qfeng
 * Date: 15-11-15 下午10:45
 * Usage: 业务服务端示例
 */
public class ApplicationServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServer.class);
    //服务类型：address或hello
    @Option(name = "-s", aliases = {"--servicetype"}, required = true, usage = "Service Type")
    private String serviceType = null;
    //监听端口
    @Option(name = "-p", aliases = {"--ports"}, required = false, usage = "Server Ports")
    private String portsString = null;

    /**
     * 解析命令行参数，获得服务类型
     *
     * @param args 命令行参数
     */
    public ApplicationServer(String... args) {
        //初始化参数解析器
        CmdLineParser parser = new CmdLineParser(this);
        try {
            //解析参数
            parser.parseArgument(args);
        } catch (CmdLineException ex) {
            //解析出错时，打印错误日志信息，并终止进程
            LOGGER.error("Parse arguments fail!", ex);
            System.exit(1);
        }
    }

    /**
     * 根据服务类型，启动对应服务
     * （1）启动地址服务的参数示例：-s address -p 6001
     * （2）启动随机数服务的参数示例：-s hello -p 7001,7002,7003（逗号中间没有空格）
     */
    public void start() {
        if (serviceType.equals("address")) {
            try {
                //解析地址服务端口
                int port = Integer.parseInt(portsString);
                //当服务类型为address时，启动地址服务（一个业务只起一个地址服务）
                new RMIAddressServer(port).start();
            } catch (Exception ex) {
                //启动地址服务错误时，打印错误日志信息
                LOGGER.error("Start address server fail!");
                //终止进程，返回异常代码
                System.exit(1);
            }
        } else if (serviceType.equals("hello")) {
            //初始化启动成功计数器
            int successCount = 0;
            //当服务类型为hello时，为每个端口号启动一个随机数服务
            String[] portStrings = portsString.split(",");
            for (String portString : portStrings) {
                try {
                    int port = Integer.parseInt(portString);
                    new HelloServer(port).start();
                    //启动成功计数自增
                    successCount++;
                } catch (Exception ex) {
                    LOGGER.error("Start hello server on port " + portString + " fail!", ex);
                }
            }
            if (successCount == 0) {
                //每个随机数服务都启动失败，打印错误日志信息
                LOGGER.error("All the hello servers start fail!");
                //终止进程，返回异常代码
                System.exit(1);
            }
        } else {
            //匹配不到合适的服务类型，打印日志信息
            LOGGER.error("Receive invalid service type " + serviceType + "!");
            //终止进程，返回异常代码
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        //初始化业务服务器
        ApplicationServer server = new ApplicationServer(args);
        //根据参数内容，启动对应服务
        server.start();
    }
}
