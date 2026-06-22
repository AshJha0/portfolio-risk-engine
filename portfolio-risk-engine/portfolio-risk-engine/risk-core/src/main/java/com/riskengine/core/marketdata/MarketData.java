package com.riskengine.core.marketdata;

/**
 * Snapshot of market data for a single instrument/symbol.
 */
public class MarketData {

    private String symbol;
    private double price;
    private double volatility;
    private double interestRate;

    public MarketData() {
    }

    public MarketData(String symbol, double price, double volatility, double interestRate) {
        this.symbol = symbol;
        this.price = price;
        this.volatility = volatility;
        this.interestRate = interestRate;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    @Override
    public String toString() {
        return "MarketData{symbol='" + symbol + "', price=" + price +
                ", volatility=" + volatility + ", interestRate=" + interestRate + '}';
    }
}
