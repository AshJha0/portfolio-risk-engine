package com.riskengine.core.model;

/**
 * Call/Put designation, used only when {@link AssetClass#OPTION} positions are priced
 * for Greeks-based risk (Delta/Gamma/Vega VaR).
 */
public enum OptionType {
    CALL,
    PUT
}
