package com.riskengine.stress.scenarios;

/**
 * Defines a stress scenario: a set of shocks applied across risk factors (equity, FX,
 * rates, volatility) to simulate an extreme market event.
 */
public class StressScenario {

    private String name;

    private double equityShock;   // e.g. -0.40 for -40%
    private double fxShock;       // e.g. -0.10 for -10% currency move
    private double volShock;      // e.g. 1.50 for +150% vol increase
    private double rateShockBps;  // e.g. 300 for +300bps credit spread / rate move

    public StressScenario() {
    }

    public StressScenario(String name, double equityShock, double fxShock,
                           double volShock, double rateShockBps) {
        this.name = name;
        this.equityShock = equityShock;
        this.fxShock = fxShock;
        this.volShock = volShock;
        this.rateShockBps = rateShockBps;
    }

    /** 2008 Global Financial Crisis: Equities -40%, Volatility +150%, Credit Spreads +300bps. */
    public static StressScenario financialCrisis2008() {
        return new StressScenario("2008 Crisis", -0.40, 0.0, 1.50, 300);
    }

    /** COVID-19 Crash: Equities -35%, Oil -60% (modeled here as part of equity/commodity shock), VIX +250%. */
    public static StressScenario covidCrash2020() {
        return new StressScenario("COVID Crash", -0.35, 0.0, 2.50, 0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getEquityShock() {
        return equityShock;
    }

    public void setEquityShock(double equityShock) {
        this.equityShock = equityShock;
    }

    public double getFxShock() {
        return fxShock;
    }

    public void setFxShock(double fxShock) {
        this.fxShock = fxShock;
    }

    public double getVolShock() {
        return volShock;
    }

    public void setVolShock(double volShock) {
        this.volShock = volShock;
    }

    public double getRateShockBps() {
        return rateShockBps;
    }

    public void setRateShockBps(double rateShockBps) {
        this.rateShockBps = rateShockBps;
    }

    @Override
    public String toString() {
        return "StressScenario{name='" + name + "', equityShock=" + equityShock +
                ", fxShock=" + fxShock + ", volShock=" + volShock +
                ", rateShockBps=" + rateShockBps + '}';
    }
}
