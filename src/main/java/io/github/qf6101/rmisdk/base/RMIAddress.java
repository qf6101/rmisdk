package io.github.qf6101.rmisdk.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: qfeng
 * Date: 15-11-20 下午4:21
 * Usage: RMI地址结构
 */
public class RMIAddress {
    //用于从字符串解析地址结构的正则表达式
    //字符串格式如下rmi://localhost:7000/RMIAddressService
    private static Pattern pattern = Pattern.compile("rmi://(.*):(\\d*)/(.*)");
    //主机名称（或IP）、监听端口、服务名称
    public String hostName = null;
    public int port = 0;
    public String serviceName = null;

    /**
     * 初始化RMI地址
     *
     * @param hostName    主机名称（或IP）
     * @param port        监听端口
     * @param serviceName 服务名称
     */
    public RMIAddress(String hostName, int port, String serviceName) {
        this.hostName = hostName;
        this.port = port;
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "rmi://" + hostName + ":" + port + "/" + serviceName;
    }

    /**
     * 检查RMI地址结构是否有效
     *
     * @return
     */
    public boolean checkValidation() {
        //主机名、服务名必须长度大于0，端口必须大于0
        if (hostName != null
                && hostName.length() > 0
                && port > 0
                && serviceName != null
                && serviceName.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 从字符串解析RMI地址结构
     *
     * @param addressString 包含地址信息的字符串，格式如下rmi://localhost:7000/RMIAddressService
     * @return RMI地址结构
     */
    public static RMIAddress parse(String addressString) {
        //用正则表达式解析地址结构
        Matcher matcher = pattern.matcher(addressString);
        if (matcher.find()) {
            //执行解析
            String hostName = matcher.group(1);
            int port = Integer.parseInt(matcher.group(2));
            String serviceName = matcher.group(3);
            RMIAddress boostAddress = new RMIAddress(hostName, port, serviceName);
            //地址结构有效，则返回
            if (boostAddress.checkValidation()) {
                return boostAddress;
            }
        }
        //如果解析不到或者地址无效，则返回空
        return null;
    }
}
