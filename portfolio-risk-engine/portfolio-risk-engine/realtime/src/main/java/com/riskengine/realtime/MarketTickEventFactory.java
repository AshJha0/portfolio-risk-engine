package com.riskengine.realtime;

import com.lmax.disruptor.EventFactory;

/**
 * Factory the Disruptor uses to pre-allocate every slot in the ring buffer at startup.
 * Pre-allocation (rather than allocating a new event object per tick) is what gives the
 * Disruptor its low-latency, low-GC-pressure characteristics.
 */
public class MarketTickEventFactory implements EventFactory<MarketTickEvent> {

    @Override
    public MarketTickEvent newInstance() {
        return new MarketTickEvent();
    }
}
