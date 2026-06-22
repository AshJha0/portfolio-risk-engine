package com.riskengine.core.marketdata;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of the latest {@link MarketData} snapshot per symbol.
 *
 * <p>This is the "Market Data Cache" component shown in the architecture diagram, sitting
 * between the market data feed and the various risk engines. Backed by a
 * {@link ConcurrentHashMap} so it can be safely read/written from multiple threads
 * (e.g. a Kafka consumer thread updating it while VaR calculations read from it).</p>
 */
public class MarketDataCache {

    private final Map<String, MarketData> cache = new ConcurrentHashMap<>();

    public void put(MarketData marketData) {
        cache.put(marketData.getSymbol(), marketData);
    }

    public void putAll(Collection<MarketData> snapshots) {
        snapshots.forEach(this::put);
    }

    public Optional<MarketData> get(String symbol) {
        return Optional.ofNullable(cache.get(symbol));
    }

    public MarketData getOrThrow(String symbol) {
        MarketData md = cache.get(symbol);
        if (md == null) {
            throw new IllegalStateException("No market data cached for symbol: " + symbol);
        }
        return md;
    }

    public Collection<MarketData> all() {
        return cache.values();
    }

    public boolean contains(String symbol) {
        return cache.containsKey(symbol);
    }

    public int size() {
        return cache.size();
    }
}
