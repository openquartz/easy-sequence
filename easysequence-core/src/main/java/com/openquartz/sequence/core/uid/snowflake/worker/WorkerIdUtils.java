package com.openquartz.sequence.core.uid.snowflake.worker;

import com.openquartz.sequence.generator.common.utils.NetUtils;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import lombok.extern.slf4j.Slf4j;

/**
 * WorkerIdUtils
 *
 * @author svnee
 */
@Slf4j
public final class WorkerIdUtils {

    private WorkerIdUtils() {
    }

    /**
     * workerID文件 分隔符
     */
    public static final String WORKER_SPLIT = "_";

    /**
     * <pre>获取workId文件名</pre>
     *
     * @param pidPort 使用端口(同机多uid应用时区分端口)
     * @param socket socket
     * @return pid name
     */
    public static String getPidName(Integer pidPort, ServerSocket socket) {
        String pidName = NetUtils.getLocalInetAddress().getHostAddress();
        if (-1 != pidPort) {
            // 占用端口
            pidPort = pidPort > 0 ? pidPort : NetUtils.getAvailablePort();
            try {
                socket = new ServerSocket(pidPort);
            } catch (IOException e) {
                throw new RuntimeException("接口占用失败！");
            }
        }
        return pidName + WorkerIdUtils.WORKER_SPLIT + pidPort;
    }

    /**
     * <pre>查找pid文件，根据前缀获取workid</pre>
     *
     * @param pidHome workerID文件存储路径
     * @param prefix workerID文件前缀
     * @return workerID值
     */
    public static Long getPid(String pidHome, String prefix) {
        String pid = null;
        File home = new File(pidHome);
        if (home.exists() && home.isDirectory()) {
            File[] files = home.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.getName().startsWith(prefix)) {
                    pid = file.getName();
                    break;
                }
            }
            if (null != pid) {
                return Long.valueOf(pid.substring(pid.lastIndexOf(WORKER_SPLIT) + 1));
            }
        } else {
            boolean mkdirs = home.mkdirs();
            log.info("[WorkUtils#getPid] mkdir dir result:{}", mkdirs);
        }
        return null;
    }

    /**
     * <pre>回拨时间睡眠等待</pre>
     *
     * @param ms 平均心跳时间
     * @param diff 回拨差时间
     */
    public static void sleepMs(long ms, long diff) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignoredException) {
            Thread.currentThread().interrupt();
        }
        diff += ms;
        if (diff < 0) {
            sleepMs(ms, diff);
        }
    }

    /**
     * <pre>创建workerID文件(workerID文件已经存在,则不创建,返回一个false；如果没有,则返回true)</pre>
     *
     * @param name name
     */
    public static void writePidFile(String name) {
        File pidFile = new File(name);
        try {
            boolean newFile = pidFile.createNewFile();
            log.info("Writing pid file,result:{}", newFile);
        } catch (IOException ignored) {
        }
    }
}
