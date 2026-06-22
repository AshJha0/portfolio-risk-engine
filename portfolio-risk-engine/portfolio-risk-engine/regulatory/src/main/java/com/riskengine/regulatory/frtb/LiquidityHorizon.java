package com.riskengine.regulatory.frtb;

/**
 * FRTB-prescribed liquidity horizons (in trading days) by broad risk class, used to scale
 * a base (10-day) VaR/ES figure up to the regulatory horizon for capital purposes.
 *
 * <p>Values reflect the Basel Committee's FRTB minimum capital requirements for market risk
 * (BCBS d457/d352 standardized liquidity horizon table). Equity/FX large-cap and liquid
 * G10 currencies get the shortest horizon (10 days); less liquid risk factors (small-cap
 * equity, credit spreads, certain commodities) require longer horizons reflecting the time
 * realistically needed to exit or hedge a position without moving the market.</p>
 */
public enum LiquidityHorizon {

    INTEREST_RATE_G10(10),
    INTEREST_RATE_OTHER(20),
    EQUITY_LARGE_CAP(10),
    EQUITY_SMALL_CAP(20),
    EQUITY_VOLATILITY_LARGE_CAP(20),
    FX_G10(10),
    FX_OTHER(20),
    CREDIT_SPREAD_SOVEREIGN_IG(20),
    CREDIT_SPREAD_CORPORATE_IG(40),
    CREDIT_SPREAD_HIGH_YIELD(60),
    COMMODITY(60);

    private final int days;

    LiquidityHorizon(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }

    /**
     * Square-root-of-time scaling factor from a 10-day base horizon to this risk class's
     * prescribed liquidity horizon, as used in the FRTB ES capital formula.
     */
    public double scalingFactorFromTenDayBase() {
        return Math.sqrt(this.days / 10.0);
    }
}
