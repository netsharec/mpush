package com.mpush.tools.thread.pool;

import com.mpush.api.spi.common.ThreadPoolFactory;
import com.mpush.tools.config.CC;
import com.mpush.tools.thread.PoolThreadFactory;

import java.util.concurrent.*;

import static com.mpush.tools.thread.ThreadNames.*;

/**
 * 此线程池可伸缩，线程空闲一定时间后回收，新请求重新创建线程
 */
public class DefaultThreadPoolFactory implements ThreadPoolFactory {

    private Executor get(ThreadPoolConfig config) {
        String name = config.getName();
        int corePoolSize = config.getCorePoolSize();
        int maxPoolSize = config.getMaxPoolSize();
        int keepAliveSeconds = config.getKeepAliveSeconds();

        BlockingQueue queue = config.getQueue();
        ThreadFactory threadFactory = new PoolThreadFactory(name);

        return new ThreadPoolExecutor(corePoolSize
                , maxPoolSize
                , keepAliveSeconds
                , TimeUnit.SECONDS
                , queue
                , threadFactory
                , new DumpThreadRejectedHandler(config));
    }

    @Override
    public Executor get(String name) {
        final ThreadPoolConfig config;
        switch (name) {
            case SERVER_BOSS:
                config = ThreadPoolConfig
                        .build(NETTY_BOSS)
                        .setCorePoolSize(CC.mp.thread.pool.boss.min)
                        .setMaxPoolSize(CC.mp.thread.pool.boss.max)
                        .setKeepAliveSeconds(TimeUnit.MINUTES.toSeconds(5))
                        .setQueueCapacity(CC.mp.thread.pool.boss.queue_size);
                break;
            case SERVER_WORK:
                config = ThreadPoolConfig
                        .build(NETTY_WORKER)
                        .setCorePoolSize(CC.mp.thread.pool.work.min)
                        .setMaxPoolSize(CC.mp.thread.pool.work.max)
                        .setKeepAliveSeconds(TimeUnit.MINUTES.toSeconds(5))
                        .setQueueCapacity(CC.mp.thread.pool.work.queue_size);
                break;
            case HTTP_CLIENT_WORK:
                config = ThreadPoolConfig
                        .buildFixed(NETTY_HTTP,
                                CC.mp.thread.pool.http_proxy.min,
                                CC.mp.thread.pool.http_proxy.queue_size
                        );
                break;
            case EVENT_BUS:
                config = ThreadPoolConfig
                        .buildFixed(EVENT_BUS,
                                CC.mp.thread.pool.event_bus.min,
                                CC.mp.thread.pool.event_bus.queue_size
                        );
                break;
            case MQ:
                config = ThreadPoolConfig
                        .buildFixed(MQ,
                                CC.mp.thread.pool.mq.min,
                                CC.mp.thread.pool.mq.queue_size
                        );
                break;
            default:
            case BIZ:
                config = ThreadPoolConfig
                        .build(BIZ)
                        .setCorePoolSize(CC.mp.thread.pool.biz.min)
                        .setMaxPoolSize(CC.mp.thread.pool.biz.max)
                        .setKeepAliveSeconds(TimeUnit.MINUTES.toSeconds(5))
                        .setQueueCapacity(CC.mp.thread.pool.biz.queue_size);
                break;
        }

        return get(config);
    }
}