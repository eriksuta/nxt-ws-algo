package com.capco.noc.algo.schema.strategy;

import com.capco.noc.algo.schema.Ticker;

public class StrategyUnit {
    private Ticker ticker;
    private Indicator indicator;

    public StrategyUnit() {}

    public StrategyUnit(Ticker ticker, Indicator indicator) {
        this.ticker = ticker;
        this.indicator = indicator;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public void setIndicator(Indicator indicator) {
        this.indicator = indicator;
    }
}
