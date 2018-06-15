package com.capco.noc.algo.schema.strategy;

import com.capco.noc.algo.schema.Tick;
import com.capco.noc.algo.schema.Ticker;

import java.util.Map;

public class StrategyUnitStopLoss extends StrategyUnit {

    private ValueType valueType;
    private PositionSellType positionSellType;

    private double lastValue;

    private double triggerPrice;
    private double triggerPercentage;

    /*
     * key   - price drop of unit
     * value - percentage to sell
     * */
    private Map<Double, Double> triggerPriceMap;

    /*
     * key   - percentage drop of price of unit
     * value - percentage to sell
     * */
    private Map<Double, Double> triggerPercentageMap;

    public StrategyUnitStopLoss(Ticker ticker, Indicator indicator) {
        super(ticker, indicator);
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public PositionSellType getPositionSellType() {
        return positionSellType;
    }

    public void setPositionSellType(PositionSellType positionSellType) {
        this.positionSellType = positionSellType;
    }

    public double getLastValue() {
        return lastValue;
    }

    public void setLastValue(Tick tick) {
        switch (getIndicator()){
            case CURRENT_PRICE:
                this.lastValue = tick.getValue();
                break;
            case MOVING_AVG_30:
                this.lastValue = tick.getAvg30();
                break;
            case MOVING_AVG_200:
                this.lastValue = tick.getAvg200();
                break;
            default: break;
        }
    }

    public double getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(double triggerPrice) {
        this.triggerPrice = triggerPrice;
    }

    public double getTriggerPercentage() {
        return triggerPercentage;
    }

    public void setTriggerPercentage(double triggerPercentage) {
        this.triggerPercentage = triggerPercentage;
    }

    public Map<Double, Double> getTriggerPriceMap() {
        return triggerPriceMap;
    }

    public void setTriggerPriceMap(Map<Double, Double> triggerPriceMap) {
        this.triggerPriceMap = triggerPriceMap;
    }

    public Map<Double, Double> getTriggerPercentageMap() {
        return triggerPercentageMap;
    }

    public void setTriggerPercentageMap(Map<Double, Double> triggerPercentageMap) {
        this.triggerPercentageMap = triggerPercentageMap;
    }

    /*
     * STATIC  - stop loss contains static trigger value, e.g. sell when price drops to certain point
     * PERCENT - stop loss contains percentage trigger, e.g. sell when price drops 10%
     * */
    public enum ValueType {
        STATIC,
        PERCENT
    }

    /*
     * ENTIRE_POSITION - when stop loss is triggered, sell entire position
     * POSITION_MAP    - ability to set a position sell map, e.g.:
     *                      - sell 20% of owned asset, if price drops 5%
     *                      - sell 50% of owned asset, if price drops 10% etc.
     * */
    public enum PositionSellType {
        ENTIRE_POSITION,
        POSITION_MAP
    }
}
