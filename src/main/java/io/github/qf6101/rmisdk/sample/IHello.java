package io.github.qf6101.rmisdk.sample;

import io.github.qf6101.rmisdk.base.IBound;

import java.rmi.RemoteException;

/**
 * User: qfeng
 * Date: 15-11-12 下午7:14
 * Usage: 随机数服务接口
 */
public interface IHello extends IBound {
    /**
     * 生成随机数
     *
     * @param requestID 请求ID
     * @return 随机数
     * @throws RemoteException
     */
    int randNumber(int requestID) throws RemoteException;
}
