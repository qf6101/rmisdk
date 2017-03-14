package io.github.qf6101.rmisdk;

import io.github.qf6101.rmisdk.addressservice.RMIAddressServer;
import io.github.qf6101.rmisdk.sample.HelloClient;
import io.github.qf6101.rmisdk.sample.HelloServer;
import io.github.qf6101.rmisdk.util.Parameters;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: qfeng
 * Date: 15-11-13 下午4:51
 * Usage: RMISDK单元测试类
 */
public class RMISDKTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMISDKTest.class);

    @BeforeClass
    public static void setUpClass() throws Exception {
        //为了加速线程刷新进度，修改各个参数值
        Parameters.RMIAddressClient_addressRefreshingDelay = 1000;
        Parameters.RMIAddressClient_trySleepInterval = 1000;
        Parameters.RMIAddressService_addressLoadingDelay = 1000;
        Parameters.RMIBaseClient_boundObjectsCheckingDelay = 1000;
        Parameters.RMIBaseClient_forceRefreshingThreshold = 3;
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        //显式终止所有的线程
        System.exit(0);
    }

    @Test
    public void testService() throws Exception {
        //刷新配置文件，使客户端连本地7001端口的服务
        ArrayList<String> serviceAddressList = new ArrayList<String>();
        serviceAddressList.add("rmi://localhost:7001/HelloService");
        refreshServiceAddressFile(serviceAddressList);
        //创建和启动地址服务器和1个随机数服务器
        RMIAddressServer addressServer = new RMIAddressServer(6001).start();
        HelloServer helloServer1 = new HelloServer(7001).start();
        Thread.sleep(6000000);
    }

    @Test
    public void testClientThread() throws Exception{
        for (int i = 0; i < 1; i++) {
            //创建随机数请求线程
            Thread clientRequestingThread = new Thread(new Runnable() {
                //初始化随机数客户端
                HelloClient hello = new HelloClient("localhost", 6001, "localhost", 6002);
                //每隔5秒请求6个随机数
                @Override
                public void run() {
                    try {
                        //每5秒钟请求一次
                        while(true){
                            //Thread.sleep(5000);
                            hello.nextNumber(1);
                        }
                    } catch (Exception e) {
                        //测试环境下，仅打印出错信息
                        e.printStackTrace();
                    }
                }
            });
            //启动随机数请求线程
            clientRequestingThread.start();

        }
        Thread.sleep(100000000);

    }

    /**
     * 启动一个服务，然后关闭
     *
     * @throws Exception
     */
    @Test
    public void testOneService() throws Exception {
        //刷新配置文件，使客户端连本地7001端口的服务
        ArrayList<String> serviceAddressList = new ArrayList<String>();
        serviceAddressList.add("rmi://localhost:7001/HelloService");
        refreshServiceAddressFile(serviceAddressList);
        //创建和启动地址服务器和1个随机数服务器
        RMIAddressServer addressServer = new RMIAddressServer(6001).start();
        HelloServer helloServer1 = new HelloServer(7001).start();
        //启动客户端
        startClient();
        //暂停60秒，关闭服务
        Thread.sleep(60000);
        helloServer1.stop();
        addressServer.stop();
    }

    /**
     * 启动三个服务，逐一关闭
     *
     * @throws Exception
     */
    @Test
    public void testThreeServices() throws Exception {
        //刷新配置文件，使客户端连本地7001,7002,7003三个端口的服务
        refreshServiceAddressFile(7001, 7002, 7003);
        //创建和启动地址服务器和3个随机数服务器
        RMIAddressServer addressServer = new RMIAddressServer(6001).start();
        HelloServer helloServer1 = new HelloServer(7001).start();
        HelloServer helloServer2 = new HelloServer(7002).start();
        HelloServer helloServer3 = new HelloServer(7003).start();
        //启动客户端
        startClient();
        //每隔10秒关闭一个服务
        Thread.sleep(10000);
        helloServer1.stop();
        Thread.sleep(10000);
        helloServer2.stop();
        Thread.sleep(10000);
        //第三个关不掉！！！
        helloServer3.stop();
        Thread.sleep(10000);
        addressServer.stop();
        Thread.sleep(20000);
    }

    /**
     * 启动5个服务，通过修改地址服务的配置文件
     * 让客户端先连前7001,7002,7003三个端口
     * 再转而连7003,7004,7005三个端口
     * 再转回7001,7002,7003三个端口
     *
     * @throws Exception
     */
    @Test
    public void testChangeServiceAddress() throws Exception {
        //刷新配置文件，使客户端连本地7001,7002,7003三个端口的服务
        refreshServiceAddressFile(7001, 7002, 7003);
        //创建和启动地址服务器和5个随机数服务器
        RMIAddressServer addressServer = new RMIAddressServer(6001).start();
        HelloServer helloServer1 = new HelloServer(7001).start();
        HelloServer helloServer2 = new HelloServer(7002).start();
        HelloServer helloServer3 = new HelloServer(7003).start();
        HelloServer helloServer4 = new HelloServer(7004).start();
        HelloServer helloServer5 = new HelloServer(7005).start();
        //启动随机数客户端
        startClient();
        //延迟10秒，关闭7001，7002两个服务
        Thread.sleep(10000);
        helloServer1.stop();
        helloServer2.stop();
        //刷新配置文件，使客户端连本地7003,7004,7005三个端口的服务,并暂停10秒
        refreshServiceAddressFile(7003, 7004, 7005);
        Thread.sleep(10000);
        //刷新配置文件，使客户端连本地7001,7002,7003三个端口的服务,并暂停10秒
        refreshServiceAddressFile(7001, 7002, 7003);
        Thread.sleep(10000);
        //刷新配置文件，使客户端连本地7003,7004,7005三个端口的服务,并暂停10秒
        refreshServiceAddressFile(7003, 7004, 7005);
        Thread.sleep(10000);
        //逐个关闭剩余的服务
        helloServer3.stop();
        helloServer4.stop();
        helloServer5.stop();
        addressServer.stop();
    }

    /**
     * 模拟地址服务异常停止后，客户端是否可以继续访问服务
     *
     * @throws Exception
     */
    @Test
    public void testStopAddressService() throws Exception {
        //刷新配置文件，使客户端连本地7001,7002,7003三个端口的服务
        refreshServiceAddressFile(7001, 7002, 7003);
        //创建和启动地址服务器和5个随机数服务器
        RMIAddressServer addressServer = new RMIAddressServer(6001).start();
        HelloServer helloServer1 = new HelloServer(7001).start();
        HelloServer helloServer2 = new HelloServer(7002).start();
        HelloServer helloServer3 = new HelloServer(7003).start();
        //启动随机数客户端
        startClient();
        //终止地址服务
        Thread.sleep(10000);
        addressServer.stop();
        Thread.sleep(30000);
    }

    /**
     * 模拟地址服务停止后，客户端切换到备用地址服务
     *
     * @throws Exception
     */
    @Test
    public void testSwitchAddressService() throws Exception {
        //刷新配置文件，使客户端连本地7001,7002,7003三个端口的服务
        refreshServiceAddressFile(7001, 7002, 7003);
        //创建和启动地址服务器和5个随机数服务器
        RMIAddressServer addressServer1 = new RMIAddressServer(6001).start();
        RMIAddressServer addressServer2 = new RMIAddressServer(6002).start();
        HelloServer helloServer1 = new HelloServer(7001).start();
        HelloServer helloServer2 = new HelloServer(7002).start();
        HelloServer helloServer3 = new HelloServer(7003).start();
        HelloServer helloServer4 = new HelloServer(7004).start();
        HelloServer helloServer5 = new HelloServer(7005).start();
        //启动随机数客户端
        startClient();
        Thread.sleep(10000);
        //停止主地址服务
        addressServer1.stop();
        Thread.sleep(10000);
        //备用地址服务刷新服务列表
        refreshServiceAddressFile(7003, 7004, 7005);
        Thread.sleep(30000);
        //重新启动主服务
        addressServer1.start();
        //主地址服务刷新服务列表
        refreshServiceAddressFile(7001, 7002, 7003);
        Thread.sleep(30000);
    }

    /**
     * 启动客户端
     *
     * @throws Exception
     */
    private void startClient() throws Exception {
        //创建随机数请求线程
        Thread clientRequestingThread = new Thread(new Runnable() {
            //初始化随机数客户端
            HelloClient hello = new HelloClient("localhost", 6001, "localhost", 6002);

            //每隔5秒请求6个随机数
            @Override
            public void run() {
                try {
                    while (true) {
                        //每5秒钟请求一次
                        Thread.sleep(5000);
                        //每次请求6个随机数
                        for (int i = 1; i <= 6; ++i) {
                            hello.nextNumber(i);
                        }
                    }
                } catch (InterruptedException e) {
                    //测试环境下，仅打印出错信息
                    e.printStackTrace();
                }
            }
        });
        //启动随机数请求线程
        clientRequestingThread.start();
    }

    /**
     * 刷新${user.dir}/conf/RMIServiceAddress.txt文件中的地址列表
     * 3个地址的IP都是localhost，端口从参数中获取
     *
     * @param servicePort1 第一个地址的端口（地址为localhost）
     * @param servicePort2 第二个地址的端口（地址为localhost）
     * @param servicePort3 第三个地址的端口（地址为localhost）
     * @throws IOException
     */
    private void refreshServiceAddressFile(int servicePort1, int servicePort2, int servicePort3) throws IOException {
        //创建包含三个地址的地址列表，地址的IP都算localhost，端口从输入参数中获得
        List<String> addresses = new ArrayList<String>(3);
        addresses.add("rmi://localhost:" + servicePort1 + "/HelloService");
        addresses.add("rmi://localhost:" + servicePort2 + "/HelloService");
        addresses.add("rmi://localhost:" + servicePort3 + "/HelloService");
        //根据新的地址列表，刷新配置文件
        refreshServiceAddressFile(addresses);
    }

    /**
     * 刷新${user.dir}/conf/RMIServiceAddress.txt文件中的地址列表
     * 新的地址列表从参数中获得
     *
     * @param addresses 新的地址列表
     * @throws IOException
     */
    private void refreshServiceAddressFile(List<String> addresses) throws IOException {
        //配置文件地址为{user.dir}/conf/RMIServiceAddress.txt
        String serviceAddressFile = System.getProperty("user.dir") + "/conf/RMIServiceAddress.txt";
        File addressFile = new File(serviceAddressFile);
        //删除已存在文件
        if (addressFile.exists()) {
            addressFile.delete();
        }
        //创建新的文件，并将地址列表写入
        BufferedWriter bufWrt = null;
        try {
            bufWrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(addressFile), "UTF-8"));
            for (int i = 0; i < addresses.size(); i++) {
                bufWrt.write(addresses.get(i));
                if (i < addresses.size() - 1) {
                    bufWrt.write("\n");
                }
            }
        } finally {
            //关闭文件读取器
            if (bufWrt != null) {
                bufWrt.close();
            }
        }
    }
}
