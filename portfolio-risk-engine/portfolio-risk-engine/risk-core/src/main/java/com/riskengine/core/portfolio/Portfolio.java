package com.riskengine.core.portfolio;

import com.riskengine.core.model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A portfolio is a collection of {@link Position}s. Risk engines operate on a Portfolio
 * as their unit of analysis (computing portfolio-level VaR, CVaR, stress P&L, etc.).
 */
public class Portfolio {

    private String name;
    private List<Position> positions;

    public Portfolio() {
        this.positions = new ArrayList<>();
    }

    public Portfolio(String name, List<Position> positions) {
        this.name = name;
        this.positions = new ArrayList<>(positions);
    }

    public void addPosition(Position position) {
        positions.add(position);
    }

    public void removePosition(Position position) {
        positions.remove(position);
    }

    /** Total mark-to-market value of the portfolio (sum of quantity * marketPrice). */
    public double getTotalValue() {
        return positions.stream()
                .mapToDouble(Position::getMarketValue)
                .sum();
    }

    /**
     * Returns a new Portfolio identical to this one but excluding the given position.
     * Used by Incremental VaR (VaR with vs. without a position).
     */
    public Portfolio withoutPosition(Position toExclude) {
        List<Position> filtered = new ArrayList<>();
        for (Position p : positions) {
            if (!p.equals(toExclude)) {
                filtered.add(p);
            }
        }
        return new Portfolio(this.name, filtered);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Position> getPositions() {
        return Collections.unmodifiableList(positions);
    }

    public void setPositions(List<Position> positions) {
        this.positions = new ArrayList<>(positions);
    }

    public int size() {
        return positions.size();
    }
}
