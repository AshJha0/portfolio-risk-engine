package com.riskengine.realtime;

import com.lmax.disruptor.EventHandler;
import com.riskengine.core.marketdata.MarketData;
import com.riskengine.core.marketdata.MarketDataCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Disruptor event handler invoked on the consumer thread for every published market tick.
 * Updates the shared {@link MarketDataCache} and, if a recompute callback is registered
 * (typically wired to re-run intraday VaR), invokes it after the cache update.
 *
 * <p>This is intentionally lightweight: heavy VaR recomputation should be debounced/throttled
 * by the recompute callback itself (e.g. recompute at most once per N ticks or per time
 * window) rather than recomputing full portfolio VaR on every single tick, which would
 * defeat the purpose of using a low-latency dispatcher in the first place.</p>
 */
public class MarketTickEventHandler implements EventHandler<MarketTickEvent> {

    private static final Logger log = LoggerFactory.getLogger(MarketTickEventHandler.class);

    private final MarketDataCache marketDataCache;
    private final Consumer<MarketTickEvent> onTick;

    public MarketTickEventHandler(MarketDataCache marketDataCache) {
        this(marketDataCache, null);
    }

    public MarketTickEventHandler(MarketDataCache marketDataCache, Consumer<MarketTickEvent> onTick) {
        this.marketDataCache = marketDataCache;
        this.onTick = onTick;
    }

    @Override
    public void onEvent(MarketTickEvent event, long sequence, boolean endOfBatch) {
        try {
            MarketData existing = marketDataCache.get(event.getSymbol()).orElse(null);
            double interestRate = existing != null ? existing.getInterestRate() : 0.0;

            marketDataCache.put(new MarketData(event.getSymbol(), event.getPrice(),
                    event.getVolatility(), interestRate));

            if (onTick != null) {
                onTick.accept(event);
            }
        } catch (Exception e) {
            // Disruptor handlers must not throw past onEvent without an ExceptionHandler
            // configured; log and continue so one bad tick doesn't stall the ring buffer.
            log.error("Error processing market tick for symbol {}: {}", event.getSymbol(), e.getMessage(), e);
        }
    }
}
