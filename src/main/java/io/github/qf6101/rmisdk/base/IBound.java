package io.github.qf6101.rmisdk.base;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * User: qfeng
 * Date: 15-11-11 下午5:00
 * Usage: RMI绑定接口
 */
public interface IBound extends Remote {
    /**
     * 检测心跳
     *
     * @param clientMessage 客户端发上来的检测信息
     * @return 服务端返回心跳信息
     * @throws RemoteException
     */
    String heartBeat(String clientMessage) throws RemoteException;
}
