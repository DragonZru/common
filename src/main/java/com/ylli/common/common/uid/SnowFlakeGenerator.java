package com.ylli.common.common.uid;


/**
 * 雪花❄️算法，生成64位long 整形
 * 1 - 41 - 10 - 12  组成
 * 1 始终是0，可视为未标识的符号位
 * 41 表示时间戳，41bit 可以存储2^41个数，对应毫秒可以使用的时间69年  (1<<41)/(365*24*60*60*1000) = 69
 * 10 表示机器数 2^10=1024 可以自行拆分like 5 - 数据中心IDC 5 - 工作机
 * 12 自增序列 2^12 = 4096
 * 以上可以达到 1ms 一个数据中心上的一台工作机可以产生4096个id
 * <p>
 * 缺点，因为基于System.currentTimeMillis() ，当系统时间回拨时会不可用
 */
public class SnowFlakeGenerator implements IDGenerator<Long> {

    /*
     * 开始时间截 (2020-01-01)
     */
    private final long epoch = 1577836800000L;
    /*
     * 机器id所占的位数
     */
    private final long workerIdBits = 5L;
    /*
     * 数据标识id所占的位数
     */
    private final long datacenterIdBits = 5L;
    /*
     * 序列在id中占的位数
     */
    private final long sequenceBits = 12L;
    /*
     * 生成序列的掩码，这里为4095
     * 二进制最高位0表示正数，1表示负数，故取值范围为 0111111...
     */
    private final long sequenceMask = (1 << sequenceBits) - 1;
    /*
    数据中心最大值与工作机最大值
     */
    private final long maxWorkerId = (1 << workerIdBits) - 1;
    private final long maxDatacenterId = (1 << datacenterIdBits) - 1;
    /*
     * 工作机器ID(0~31) 默认0
     */
    private long workerId = 0L;
    /*
     * 数据中心ID(0~31) 默认0
     */
    private long datacenterId = 0L;
    /*
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;
    /*
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /*
     * 构造函数
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowFlakeGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public SnowFlakeGenerator() {
    }

    /*
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    private synchronized long invoke() {
        long timestamp = System.currentTimeMillis();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //时间戳改变，毫秒内序列重置
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - epoch) << (sequenceBits + workerIdBits + datacenterIdBits)) // 时间戳 （左移）
                | (datacenterId << (sequenceBits + workerIdBits)) // 数据中心id (左移 sequence+ workerId 位)
                | (workerId << sequenceBits) // 工作机器id (左移 sequence 位)
                | sequence;  // 序列号
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            tilNextMillis(lastTimestamp);
            Thread.onSpinWait();
        }
        return timestamp;
    }

    @Override
    public Long next() {
        return invoke();
    }
}
