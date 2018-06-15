package com.capco.noc.algo.strategy;

import com.capco.noc.algo.schema.Portfolio;

public interface StrategyCreator {

     void defineInitialPortfolioAllocation(Portfolio portfolio);

     void defineAccountStrategy(Portfolio portfolio);
}
