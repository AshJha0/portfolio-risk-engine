package com.riskengine.realtime;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.riskengine.core.marketdata.MarketDataCache;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Wires together an LMAX Disruptor ring buffer for processing market data ticks at low
 * latency, feeding the shared {@link MarketDataCache} that all risk engines read from.
 *
 * <p>This is the "Real-Time Risk" / "Low Latency" component referenced in the spec's
 * Advanced Extensions: ticks arrive (in production, from a Kafka consumer — see
 * {@link KafkaMarketDataConsumer}), get published onto the ring buffer, and are processed
 * by {@link MarketTickEventHandler} on a dedicated consumer thread without blocking the
 * producer (market data feed thread).</p>
 *
 * <p>Ring buffer size must be a power of two (Disruptor requirement) for efficient
 * sequence-to-slot masking. The {@link Disruptor} instance manages its own consumer
 * thread internally (created via the supplied {@link ThreadFactory}); no separate
 * {@code ExecutorService} is needed.</p>
 */
public class RealTimeRiskPipeline {

    private static final int DEFAULT_RING_BUFFER_SIZE = 1024; // power of two

    private final Disruptor<MarketTickEvent> disruptor;
    private final RingBuffer<MarketTickEvent> ringBuffer;
    private final AtomicLong publishedCount = new AtomicLong();

    public RealTimeRiskPipeline(MarketDataCache marketDataCache) {
        this(marketDataCache, DEFAULT_RING_BUFFER_SIZE, null);
    }

    public RealTimeRiskPipeline(MarketDataCache marketDataCache, int ringBufferSize,
                                 Consumer<MarketTickEvent> onTick) {
        if ((ringBufferSize & (ringBufferSize - 1)) != 0) {
            throw new IllegalArgumentException("ringBufferSize must be a power of two");
        }

        ThreadFactory threadFactory = runnable -> {
            Thread t = new Thread(runnable, "risk-realtime-consumer");
            t.setDaemon(true);
            return t;
        };

        this.disruptor = new Disruptor<>(
                new MarketTickEventFactory(),
                ringBufferSize,
                threadFactory,
                ProducerType.MULTI, // multiple market data feed threads may publish concurrently
                new BlockingWaitStrategy()
        );

        disruptor.handleEventsWith(new MarketTickEventHandler(marketDataCache, onTick));
        this.ringBuffer = disruptor.start();
    }

    /**
     * Publishes a single market tick onto the ring buffer. Non-blocking under normal load;
     * the producer only blocks if the ring buffer is completely full (i.e. the consumer
     * cannot keep up), which is the Disruptor's natural backpressure mechanism.
     */
    public void publishTick(String symbol, double price, double volatility) {
        long sequence = ringBuffer.next();
        try {
            MarketTickEvent event = ringBuffer.get(sequence);
            event.set(symbol, price, volatility, System.nanoTime());
        } finally {
            ringBuffer.publish(sequence);
        }
        publishedCount.incrementAndGet();
    }

    public long getPublishedCount() {
        return publishedCount.get();
    }

    public void shutdown() {
        disruptor.shutdown();
    }
}
