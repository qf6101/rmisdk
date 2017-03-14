package io.github.qf6101.rmisdk.addressservice;

import io.github.qf6101.rmisdk.base.RMIAddress;
import io.github.qf6101.rmisdk.util.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * User: qfeng
 * Date: 15-11-11 下午3:54
 * Usage: 地址服务客户端
 */
public class RMIAddressClient extends RMIBoosting {
    private static final Logger LOGGER = LoggerFactory.getLogger(RMIAddressClient.class);
    //初始化参数
    private final int addressRefreshingDelay = Parameters.RMIAddressClient_addressRefreshingDelay;
    private final int trySleepInterval = Parameters.RMIAddressClient_trySleepInterval;
    private final int returnToFirstAddressThreshold = Parameters.RMIAddressClient_ReturnToFirstAddressThreshold;
    //地址服务对象、地址列表、自举地址、备用自举地址、当前使用的自举地址
    private volatile IRMIAddressService addressService = null;
    private volatile List<String> addresses = null;
    private RMIAddress firstBoostAddress = null;
    private RMIAddress secondBoostAddress = null;
    private RMIAddress currentBoostAddress = null;

    /**
     * 地址服务客户端
     *
     * @param firstHostName    自举地址的主机名（如果为null，则从resources下的配置文件获取自举地址）
     * @param firstListenPort  自举地址的端口（大于1024）
     * @param secondHostName   备用自举地址的主机名（如果为null，则忽略它）
     * @param secondListenPort 备用自举地址的端口（大于1024，如果secondhostName为null，则忽略它）
     * @throws Exception
     */
    public RMIAddressClient(String firstHostName,
                            int firstListenPort,
                            String secondHostName,
                            int secondListenPort) throws Exception {
        //获取自举地址
        if (firstHostName != null) {
            firstBoostAddress = new RMIAddress(firstHostName, firstListenPort, getRmiAddressServiceName());
            currentBoostAddress = firstBoostAddress;
        } else {
            //主地址不能为空
            throw new Exception("First boost address should be specified");
        }
        //获取备用自举地址
        if (secondHostName != null) {
            secondBoostAddress = new RMIAddress(secondHostName, secondListenPort, getRmiAddressServiceName());
        }
        //打印地址至日志
        LOGGER.info("Use hard code boost addresses: " + firstBoostAddress + ", " + secondBoostAddress);
        //初始化时，阻塞式刷新地址服务对象，并获取地址列表
        refreshServiceInstance();
        addresses = addressService.getAddresses();
        //创建守护线程，定时刷新地址列表
        Thread addressRefreshingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int returnToFirstAddressCount = 0;
                while (true) {
                    try {
                        //获取地址最新列表
                        addresses = addressService.getAddresses();
                        //打印日志信息
                        LOGGER.info("Successfully refresh address list");
                        for (String address : addresses) {
                            LOGGER.info("Refreshed addresses include " + address);
                        }
                        //暂停一段时间再行获取
                        Thread.sleep(addressRefreshingDelay);
                        //尝试从备用自举地址返回到主自举地址
                        if (currentBoostAddress == secondBoostAddress
                                && returnToFirstAddressCount++ >= returnToFirstAddressThreshold) {
                            returnToFirstAddressCount = 0;
                            currentBoostAddress = firstBoostAddress;
                            refreshServiceInstance();
                        }
                    } catch (Exception ex) {
                        //调用地址服务对象出错时，重新获取地址服务对象
                        refreshServiceInstance();
                    }
                }
            }
        });
        //启动守护线程
        addressRefreshingThread.start();
        LOGGER.info("Successfully start address demon thread");
    }

    /**
     * 获取地址列表（可能为空）
     *
     * @return 地址列表
     */
    public List<String> getAddresses() {
        return addresses;
    }

    /**
     * 刷新地址服务对象
     */
    private void refreshServiceInstance() {
        while (true) {
            try {
                //根据自举地址获取地址服务对象
                Registry registry = LocateRegistry.getRegistry(currentBoostAddress.hostName, currentBoostAddress.port);
                addressService = (IRMIAddressService) registry.lookup(currentBoostAddress.serviceName);
                //获取成功则从函数退出，否则继续获取
                break;
            } catch (Exception ex) {
                //获取失败，打印错误日志信息
                LOGGER.warn("Lookup address " + currentBoostAddress + " fail (" + ex.getMessage() + ")");
                //暂停一会, 切换到另一个自举地址，再继续尝试获取地址服务对象
                try {
                    Thread.sleep(trySleepInterval);
                    if (secondBoostAddress != null)
                        currentBoostAddress = currentBoostAddress == firstBoostAddress ? secondBoostAddress : firstBoostAddress;
                } catch (InterruptedException sleepEx) {
                    LOGGER.error("", sleepEx);
                }
            }
        }
    }
}
