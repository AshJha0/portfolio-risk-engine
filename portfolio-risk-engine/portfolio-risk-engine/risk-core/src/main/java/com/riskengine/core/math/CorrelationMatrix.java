package com.riskengine.core.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps a correlation matrix between instruments and exposes its Cholesky decomposition,
 * which is the standard technique for generating *correlated* random shocks in Monte Carlo VaR.
 *
 * <p>Usage: given independent standard-normal draws Z, correlated draws are L * Z, where
 * L is the lower-triangular Cholesky factor of the correlation matrix.</p>
 */
public class CorrelationMatrix {

    private final List<String> symbols;
    private final RealMatrix matrix;
    private RealMatrix choleskyLower;

    public CorrelationMatrix(List<String> symbols, double[][] correlations) {
        if (correlations.length != symbols.size()) {
            throw new IllegalArgumentException("Correlation matrix dimension must match symbol count");
        }
        this.symbols = symbols;
        this.matrix = new Array2DRowRealMatrix(correlations);
    }

    /**
     * Computes (and caches) the Cholesky lower-triangular factor L such that L * L^T = correlation matrix.
     *
     * @throws NonPositiveDefiniteMatrixException if the supplied matrix is not positive-definite
     *         (i.e. not a valid correlation matrix)
     */
    public RealMatrix getCholeskyLower() {
        if (choleskyLower == null) {
            CholeskyDecomposition decomposition = new CholeskyDecomposition(matrix);
            choleskyLower = decomposition.getL();
        }
        return choleskyLower;
    }

    /**
     * Transforms a vector of independent standard-normal draws into correlated draws
     * using the Cholesky factor: correlated = L * independent.
     */
    public double[] correlate(double[] independentStandardNormals) {
        if (independentStandardNormals.length != symbols.size()) {
            throw new IllegalArgumentException("Expected " + symbols.size() + " independent draws");
        }
        RealMatrix l = getCholeskyLower();
        double[] result = new double[symbols.size()];
        for (int i = 0; i < symbols.size(); i++) {
            double sum = 0.0;
            for (int j = 0; j <= i; j++) {
                sum += l.getEntry(i, j) * independentStandardNormals[j];
            }
            result[i] = sum;
        }
        return result;
    }

    /** Convenience: maps the correlated draws back onto their symbols, in input order. */
    public Map<String, Double> correlateToMap(double[] independentStandardNormals) {
        double[] correlated = correlate(independentStandardNormals);
        Map<String, Double> out = new LinkedHashMap<>();
        for (int i = 0; i < symbols.size(); i++) {
            out.put(symbols.get(i), correlated[i]);
        }
        return out;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public RealMatrix getMatrix() {
        return matrix;
    }
}
