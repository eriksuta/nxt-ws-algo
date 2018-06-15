package com.capco.noc.algo.schema.strategy;

import com.capco.noc.algo.schema.Tick;
import com.capco.noc.algo.schema.Ticker;

import java.util.Map;

public class StrategyUnitBuy extends StrategyUnit{

    private ValueType valueType;
    private FundsAllocationType fundsAllocationType;

    private double referenceValue;

    private double triggerPrice;
    private double triggerPercentage;

    /*
     * key   - price of unit drop according to referenceValue
     * value - funds allocation to buy in %
     * */
    private Map<Double, Double> triggerPriceMap;

    /*
     * key   - percentage drop of price of unit drop according to referenceValue
     * value - funds allocation to buy in %
     * */
    private Map<Double, Double> triggerPercentageMap;

    public StrategyUnitBuy(Ticker ticker, Indicator indicator) {
        super(ticker, indicator);
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public FundsAllocationType getFundsAllocationType() {
        return fundsAllocationType;
    }

    public void setFundsAllocationType(FundsAllocationType fundsAllocationType) {
        this.fundsAllocationType = fundsAllocationType;
    }

    public double getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(Tick tick) {
        switch (getIndicator()){
            case CURRENT_PRICE:
                this.referenceValue = tick.getValue();
                break;
            case MOVING_AVG_30:
                this.referenceValue = tick.getAvg30();
                break;
            case MOVING_AVG_200:
                this.referenceValue = tick.getAvg200();
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
     * STATIC  - buy order static trigger value, e.g. buy when price drops to certain point
     * PERCENT - buy order contains percentage trigger, e.g. buy when price drops 10%
     * */
    public enum ValueType {
        STATIC,
        PERCENT
    }

    /*
     * ENTIRE_CASH_BALANCE         - use entire currently unallocated cash to perform buy
     * ALLOCATION_MAP - funds allocation changes based on price drops, e.g.
     *                      - use 20% of cash to buy, if price drops 3%
     *                      - use 50% of cash to buy, it price drops 8% etc.
     *
     * */
    public enum FundsAllocationType {
        ENTIRE_CASH_BALANCE,
        ALLOCATION_MAP
    }
}
