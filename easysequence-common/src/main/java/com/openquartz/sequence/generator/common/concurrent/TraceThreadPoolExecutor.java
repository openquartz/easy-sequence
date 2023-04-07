package com.openquartz.sequence.generator.common.concurrent;

import com.openquartz.sequence.generator.common.utils.MapUtils;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Trace线程池执行
 *
 * @author svnee
 */
@Slf4j
public class TraceThreadPoolExecutor extends ThreadPoolExecutor {

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
        TimeUnit unit,
        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public TraceThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
        RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if (r instanceof TraceRunnable) {
            TraceRunnable traceRunnable = (TraceRunnable) r;
            try {
                Map<String, Object> traceContextMap = traceRunnable.getTraceContextMap();
                if (traceContextMap != null && !traceContextMap.isEmpty()) {
                    TraceContext.setTraceContextMap(traceContextMap);
                    Map<String, String> trace = TraceContext.getTrace();
                    if (MapUtils.isNotEmpty(trace)) {
                        MDC.setContextMap(trace);
                    }
                }
            } catch (Exception ex) {
                log.error("[trace execute thread pool] set context error", ex);
            }
        }
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        try {
            super.afterExecute(r, t);
        } finally {
            if (r instanceof TraceRunnable) {
                TraceRunnable traceRunnable = (TraceRunnable) r;
                try {
                    TraceContext.clear();
                    MDC.clear();
                } catch (Exception ex) {
                    log.error("[trace execute thread pool] clear slf4j context error", ex);
                }
                try {
                    traceRunnable.clear();
                } catch (Exception ex) {
                    log.error("[trace execute thread pool] clear context error", ex);
                }
            }
        }
    }

    @Override
    public void execute(Runnable command) {
        TraceRunnable traceRunnable = new TraceRunnable(command);
        Map<String, String> traceContextMap = MDC.getCopyOfContextMap();
        TraceContext.putTrace(traceContextMap);
        Map<String, Object> contextMap = TraceContext.getTraceContextMap();
        if (contextMap != null && !contextMap.isEmpty()) {
            traceRunnable.setTraceContextMap(contextMap);
        }
        super.execute(traceRunnable);
    }
}
