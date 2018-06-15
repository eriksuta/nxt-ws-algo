package com.capco.noc.algo.strategy;

import com.capco.noc.algo.schema.Portfolio;
import com.capco.noc.algo.schema.Strategy;
import com.capco.noc.algo.schema.Ticker;

import java.util.HashMap;

public class StrategyCreatorEmpty implements StrategyCreator {

    @Override
    public void defineInitialPortfolioAllocation(Portfolio portfolio) {
        portfolio.setInitialAllocation(new HashMap<Ticker, Double>(){{
            put(Ticker.A, 20.0);
            put(Ticker.D, 20.0);
            put(Ticker.B, 25.0);
            put(Ticker.C, 25.0);
        }});
    }

    @Override
    public void defineAccountStrategy(Portfolio portfolio) {
        Strategy strategy = new Strategy();

        portfolio.setStrategy(strategy, this.getClass().getSimpleName());
    }
}
