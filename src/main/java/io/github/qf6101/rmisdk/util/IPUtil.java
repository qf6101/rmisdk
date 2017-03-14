package io.github.qf6101.rmisdk.util;

import java.net.*;
import java.util.Enumeration;

/**
 *
 * @author qfeng
 *
 */
public class IPUtil {
    /**
     * 获取当前IP
     *
     * @return 当前IP
     * @throws Exception
     */
    public static String getIP() throws Exception {
        InetAddress inetAddress = getFirstNonLoopbackAddress(true, false);
        if (inetAddress != null) {
            return inetAddress.getHostAddress();
        } else {
            return "unknown_ip";
        }
    }

    @SuppressWarnings("rawtypes")
    private static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements(); ) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        return addr;
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        return addr;
                    }
                }
            }
        }
        return null;
    }

}
