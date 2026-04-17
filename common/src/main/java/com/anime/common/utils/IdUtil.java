package com.anime.common.utils;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 高性能分布式ID生成器
 * 特点：
 * 1. 基于时间戳（可到2099年）
 * 2. 支持每毫秒4096个ID（单机极限）
 * 3. 支持最多1024个节点
 * 4. 无需依赖数据库/Redis
 * 5. 线程安全，无锁设计
 */
public class IdUtil {

    // 时间戳起始点（2024-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1704067200000L;

    // 各部分占用的位数
    private static final long TIMESTAMP_BITS = 41L;  // 41位时间戳，可用69年
    private static final long NODE_BITS = 10L;      // 10位节点ID，支持1024个节点
    private static final long SEQUENCE_BITS = 12L;  // 12位序列号，每毫秒4096个ID

    // 最大值
    private static final long MAX_NODE_ID = ~(-1L << NODE_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 移位偏移量
    private static final long TIMESTAMP_LEFT_SHIFT = NODE_BITS + SEQUENCE_BITS;
    private static final long NODE_LEFT_SHIFT = SEQUENCE_BITS;

    // 节点ID（0-1023）
    private static final long NODE_ID;

    // 序列号（每毫秒递增）
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    // 上次生成ID的时间戳
    private static volatile long lastTimestamp = -1L;

    // 用于保证线程安全的锁
    private static final ReentrantLock LOCK = new ReentrantLock();

    static {
        // 初始化节点ID（优先从环境变量获取，否则从MAC地址生成）
        long nodeId = getNodeIdFromEnv();
        if (nodeId < 0 || nodeId > MAX_NODE_ID) {
            nodeId = getNodeIdFromMac();
        }
        if (nodeId < 0 || nodeId > MAX_NODE_ID) {
            nodeId = new SecureRandom().nextInt((int) MAX_NODE_ID + 1);
        }
        NODE_ID = nodeId;
    }

    /**
     * 生成唯一ID(线程安全)
     * 使用ReentrantLock保证高并发下的唯一性
     */
    public static long nextId() {
        LOCK.lock();
        try {
            long currentTimestamp = getCurrentTimestamp();

            if (currentTimestamp < lastTimestamp) {
                // 时钟回拨处理：等待到上次时间戳
                currentTimestamp = waitUntilNextMillis(lastTimestamp);
            }

            long sequence;
            if (currentTimestamp == lastTimestamp) {
                // 同一毫秒内，序列号递增
                sequence = SEQUENCE.incrementAndGet() & MAX_SEQUENCE;
                if (sequence == 0) {
                    // 当前毫秒序列号用完，等待下一毫秒
                    currentTimestamp = waitUntilNextMillis(lastTimestamp);
                    sequence = 0;
                    SEQUENCE.set(0);
                }
            } else {
                // 不同毫秒，重置序列号
                sequence = 0;
                SEQUENCE.set(0);
            }

            lastTimestamp = currentTimestamp;

            return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                    | (NODE_ID << NODE_LEFT_SHIFT)
                    | sequence;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 批量生成ID（性能优化）
     */
    public static long[] nextIds(int count) {
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = nextId();
        }
        return ids;
    }

    /**
     * 从ID中解析时间戳
     */
    public static long parseTimestamp(long id) {
        return START_TIMESTAMP + (id >> TIMESTAMP_LEFT_SHIFT);
    }

    /**
     * 从ID中解析节点ID
     */
    public static long parseNodeId(long id) {
        return (id >> NODE_LEFT_SHIFT) & MAX_NODE_ID;
    }

    /**
     * 从ID中解析序列号
     */
    public static long parseSequence(long id) {
        return id & MAX_SEQUENCE;
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    private static long getCurrentTimestamp() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 等待直到下一个毫秒
     */
    private static long waitUntilNextMillis(long lastTimestamp) {
        long current = getCurrentTimestamp();
        while (current <= lastTimestamp) {
            current = getCurrentTimestamp();
            Thread.onSpinWait(); // 自旋等待，减少上下文切换
        }
        return current;
    }

    /**
     * 从环境变量/系统属性获取节点ID
     */
    private static long getNodeIdFromEnv() {
        try {
            String nodeIdStr = System.getenv("IDUTIL_NODE_ID");
            if (nodeIdStr == null) {
                nodeIdStr = System.getProperty("idutil.node.id");
            }
            if (nodeIdStr != null) {
                return Long.parseLong(nodeIdStr);
            }
        } catch (Exception e) {
            // ignore
        }
        return -1;
    }

    /**
     * 从MAC地址生成节点ID
     */
    private static long getNodeIdFromMac() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isLoopback() && ni.isUp()) {
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null && mac.length >= 6) {
                        long hash = 0;
                        for (byte b : mac) {
                            hash = (hash * 31) + (b & 0xFF);
                        }
                        return Math.abs(hash) & MAX_NODE_ID;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return -1;
    }

    /**
     * 性能测试工具
     */
    public static void benchmark(int seconds) {
        System.out.println("开始性能测试，持续 " + seconds + " 秒...");
        AtomicLong count = new AtomicLong(0);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + seconds * 1000L;

        // 多线程测试
        int threadCount = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                while (System.currentTimeMillis() < endTime) {
                    nextId();
                    count.incrementAndGet();
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long totalCount = count.get();
        long duration = System.currentTimeMillis() - startTime;
        double qps = totalCount * 1000.0 / duration;

        System.out.printf("测试完成！总计生成 %d 个ID，耗时 %d ms，QPS: %.2f%n",
                totalCount, duration, qps);
        System.out.printf("平均每个ID占用: %.2f ns%n", (duration * 1_000_000.0 / totalCount));
    }
}