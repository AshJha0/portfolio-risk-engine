package com.riskengine.realtime;

/**
 * Mutable event slot used in the LMAX Disruptor ring buffer. Disruptor pre-allocates these
 * and reuses them on every publish (to avoid GC pressure under high tick rates), so this
 * class intentionally has mutable setters rather than being an immutable value object.
 */
public class MarketTickEvent {

    private String symbol;
    private double price;
    private double volatility;
    private long timestampNanos;

    public void set(String symbol, double price, double volatility, long timestampNanos) {
        this.symbol = symbol;
        this.price = price;
        this.volatility = volatility;
        this.timestampNanos = timestampNanos;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getVolatility() {
        return volatility;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }
}
