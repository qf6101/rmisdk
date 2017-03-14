package io.github.qf6101.rmisdk.addressservice;

import io.github.qf6101.rmisdk.base.IBound;

import java.rmi.RemoteException;
import java.util.List;

/**
 * User: qfeng
 * Date: 15-11-11 上午11:54
 * Usage: 地址服务接口
 */
public interface IRMIAddressService extends IBound {
    /**
     * 获取地址列表
     *
     * @return 地址列表
     * @throws RemoteException
     */
    List<String> getAddresses() throws RemoteException;
}
