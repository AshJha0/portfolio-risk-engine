package com.riskengine.core.model;

import java.util.Objects;

/**
 * A single position held in a {@link com.riskengine.core.portfolio.Portfolio}.
 *
 * <p>The base fields (symbol, assetClass, quantity, marketPrice, delta/gamma/vega) match the
 * original spec. Additional optional fields support option pricing (strike, expiry,
 * volatility, riskFreeRate, optionType) which are only populated for {@link AssetClass#OPTION}
 * positions and consumed by the Greeks engine and option pricing models.</p>
 */
public class Position {

    private String symbol;
    private AssetClass assetClass;

    private double quantity;
    private double marketPrice;

    // Greeks - either supplied directly by an upstream pricing system,
    // or computed by analytics-greeks via Black-Scholes / Garman-Kohlhagen.
    private double delta;
    private double gamma;
    private double vega;
    private double theta;
    private double rho;

    // Option-specific fields (only relevant when assetClass == OPTION)
    private OptionType optionType;
    private double strike;
    private double timeToExpiryYears;
    private double impliedVolatility;
    private double riskFreeRate;
    private double dividendYield;

    public Position() {
    }

    public Position(String symbol, AssetClass assetClass, double quantity, double marketPrice) {
        this.symbol = symbol;
        this.assetClass = assetClass;
        this.quantity = quantity;
        this.marketPrice = marketPrice;
    }

    /** Market value of this position = quantity * marketPrice. */
    public double getMarketValue() {
        return quantity * marketPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(AssetClass assetClass) {
        this.assetClass = assetClass;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(double marketPrice) {
        this.marketPrice = marketPrice;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getVega() {
        return vega;
    }

    public void setVega(double vega) {
        this.vega = vega;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getRho() {
        return rho;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }

    public double getStrike() {
        return strike;
    }

    public void setStrike(double strike) {
        this.strike = strike;
    }

    public double getTimeToExpiryYears() {
        return timeToExpiryYears;
    }

    public void setTimeToExpiryYears(double timeToExpiryYears) {
        this.timeToExpiryYears = timeToExpiryYears;
    }

    public double getImpliedVolatility() {
        return impliedVolatility;
    }

    public void setImpliedVolatility(double impliedVolatility) {
        this.impliedVolatility = impliedVolatility;
    }

    public double getRiskFreeRate() {
        return riskFreeRate;
    }

    public void setRiskFreeRate(double riskFreeRate) {
        this.riskFreeRate = riskFreeRate;
    }

    public double getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(double dividendYield) {
        this.dividendYield = dividendYield;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return Objects.equals(symbol, position.symbol) && assetClass == position.assetClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, assetClass);
    }

    @Override
    public String toString() {
        return "Position{symbol='" + symbol + "', assetClass=" + assetClass +
                ", quantity=" + quantity + ", marketPrice=" + marketPrice + '}';
    }
}
