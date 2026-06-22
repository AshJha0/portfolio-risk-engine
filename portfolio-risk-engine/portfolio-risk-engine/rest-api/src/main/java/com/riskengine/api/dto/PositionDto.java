package com.riskengine.api.dto;

import com.riskengine.core.model.AssetClass;
import com.riskengine.core.model.OptionType;
import com.riskengine.core.model.Position;

/**
 * REST-layer representation of a {@link Position}, used to build portfolios from incoming
 * JSON request bodies. Kept separate from the core domain model so the API's wire format
 * can evolve independently of internal engine types.
 */
public class PositionDto {

    public String symbol;
    public AssetClass assetClass;
    public double quantity;
    public double marketPrice;

    // Option-specific fields, optional unless assetClass == OPTION
    public OptionType optionType;
    public Double strike;
    public Double timeToExpiryYears;
    public Double impliedVolatility;
    public Double riskFreeRate;

    public Position toPosition() {
        Position p = new Position(symbol, assetClass, quantity, marketPrice);
        if (assetClass == AssetClass.OPTION) {
            p.setOptionType(optionType);
            if (strike != null) p.setStrike(strike);
            if (timeToExpiryYears != null) p.setTimeToExpiryYears(timeToExpiryYears);
            if (impliedVolatility != null) p.setImpliedVolatility(impliedVolatility);
            if (riskFreeRate != null) p.setRiskFreeRate(riskFreeRate);
        }
        return p;
    }
}
