package com.openquartz.sequence.generator.common.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Random;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网络工具类
 *
 * @author svnee
 */
public final class NetUtils {

    public static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

    public static final String ANYHOST = "0.0.0.0";

    public static final String LOCALHOST = "127.0.0.1";

    private static final int RND_PORT_START = 30000;

    private static final int RND_PORT_RANGE = 10000;

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final int MAX_PORT = 65535;

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private NetUtils() {
    }

    /**
     * 获取本地地址
     */
    public static InetAddress getLocalInetAddress() {
        InetAddress localAddress;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (UnknownHostException e) {
            logger.error("本地地址获取失败: {}", e.getMessage(), e);
        }
        return getLocalLanAddress();
    }

    /**
     * 获取Lan地址
     */
    public static InetAddress getLocalLanAddress() {
        try {
            // 1、遍历所有的网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (network.isLoopback()) {
                    // 排除loopback类型地址
                    continue;
                }
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                // 2、在所有的接口下再遍历IP
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (isValidAddress(address)) {
                        return address;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("无法确定Lan地址: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取机器码
     */
    public static byte[] getMachineNum() {
        try {
            InetAddress ip = NetUtils.getLocalInetAddress();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    return mac;
                }
            }
        } catch (Exception e) {
            logger.error("机器码获取失败：{}", e.getMessage(), e);
        }
        return new byte[0];
    }

    /**
     * 是否有效地址
     */
    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches());
    }

    public static int getRandomPort() {
        return RND_PORT_START + RANDOM.nextInt(RND_PORT_RANGE);
    }

    public static int getAvailablePort() {
        try (ServerSocket ss = new ServerSocket()) {
            ss.bind(null);
            return ss.getLocalPort();
        } catch (IOException e) {
            return getRandomPort();
        }
    }

    public static int getAvailablePort(int port) {
        if (port <= 0) {
            return getAvailablePort();
        }
        for (int i = port; i < MAX_PORT; i++) {
            try (ServerSocket ss = new ServerSocket(i)) {
                return i;
            } catch (IOException e) {
                // continue
            }
        }
        return port;
    }
}
