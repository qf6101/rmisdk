package io.github.qf6101.rmisdk.addressservice;

import io.github.qf6101.rmisdk.base.RMIBaseServer;
import org.apache.log4j.Logger;

/**
 * User: qfeng
 * Date: 15-11-11 下午3:29
 * Usage: 自举器，读取自举地址或自举端口
 */
public abstract class RMIBoosting extends RMIBaseServer {
    private static final Logger LOGGER = Logger.getLogger(RMIBoosting.class);
    private static String RMI_ADDRESS_SERVICE_NAME = "RMIAddressService";

    public static String getRmiAddressServiceName() {
        return RMI_ADDRESS_SERVICE_NAME;
    }
}
