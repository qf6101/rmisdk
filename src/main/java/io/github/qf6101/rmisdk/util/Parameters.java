package io.github.qf6101.rmisdk.util;

/**
 * User: qfeng
 * Date: 15-11-14 下午9:46
 * Usage:
 */
public class Parameters {
    //地址服务客户端中，刷新地址的频率（毫秒）
    public static int RMIAddressClient_addressRefreshingDelay = 30000;
    //地址服务客户端中，获取服务对象失败后，再次尝试的时间间隔（毫秒）
    public static int RMIAddressClient_trySleepInterval = 30000;
    //地址服务客户端中，从地址服务对象获取地址列表的次数达到阈值后，强制从备用地址切回到第一地址
    public static int RMIAddressClient_ReturnToFirstAddressThreshold = 10;
    //地址服务对象中，重新从文件载入地址列表频率（毫秒）
    public static int RMIAddressService_addressLoadingDelay = 30000;
    //RMI客户端基类中，检测绑定对象列表的频率（毫秒）
    public static int RMIBaseClient_boundObjectsCheckingDelay = 30000;
    //RMI客户端基类中，检测次数达到该阈值后，强制刷新绑定对象列表
    public static int RMIBaseClient_forceRefreshingThreshold = 10;
}
