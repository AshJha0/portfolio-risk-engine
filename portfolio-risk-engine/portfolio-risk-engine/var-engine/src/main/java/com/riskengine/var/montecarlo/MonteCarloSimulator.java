package com.riskengine.var.montecarlo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates simulated future prices for a single instrument via random normal shocks.
 * This is the base building block of Monte Carlo VaR: simulate many price paths, revalue
 * the portfolio under each, then take the desired quantile of the resulting P&amp;L distribution.
 */
public class MonteCarloSimulator {

    private final Random random;

    public MonteCarloSimulator() {
        this.random = new Random();
    }

    public MonteCarloSimulator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Simple shock model matching the original spec: price * (1 + Z * volatility).
     * Adequate for short horizons; for longer horizons prefer {@link #simulateGbm}.
     */
    public List<Double> simulate(double currentPrice, double volatility, int scenarios) {
        List<Double> prices = new ArrayList<>(scenarios);

        for (int i = 0; i < scenarios; i++) {
            double shock = random.nextGaussian() * volatility;
            prices.add(currentPrice * (1 + shock));
        }

        return prices;
    }

    /**
     * Geometric Brownian motion simulation: S_T = S_0 * exp((mu - 0.5*sigma^2)*T + sigma*sqrt(T)*Z).
     * Preferred over the simple shock model for multi-day horizons since it guarantees
     * positive prices and compounds correctly over time.
     *
     * @param drift          annualized drift (mu), use risk-free rate for risk-neutral simulation
     * @param volatility     annualized volatility (sigma)
     * @param timeHorizonYrs simulation horizon in years (e.g. 1/252 for one trading day)
     */
    public List<Double> simulateGbm(double currentPrice, double drift, double volatility,
                                     double timeHorizonYrs, int scenarios) {
        List<Double> prices = new ArrayList<>(scenarios);
        double driftTerm = (drift - 0.5 * volatility * volatility) * timeHorizonYrs;
        double volTerm = volatility * Math.sqrt(timeHorizonYrs);

        for (int i = 0; i < scenarios; i++) {
            double z = random.nextGaussian();
            prices.add(currentPrice * Math.exp(driftTerm + volTerm * z));
        }

        return prices;
    }

    /** Generates a vector of independent standard-normal draws, e.g. for correlated multi-asset simulation. */
    public double[] nextIndependentNormals(int count) {
        double[] draws = new double[count];
        for (int i = 0; i < count; i++) {
            draws[i] = random.nextGaussian();
        }
        return draws;
    }
}
