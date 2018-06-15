package com.capco.noc.algo.engine;

import com.capco.noc.algo.schema.Portfolio;
import com.capco.noc.algo.schema.Tick;

import java.util.List;
import java.util.Map;

public interface AlgoTradeService {

    /**
     *
     * @param stockPriceMap
     */
    void setStockPriceMap(Map<String, List<Tick>> stockPriceMap);

    /**
     *
     * @param portfolio
     */
    void performInitialPortfolioAllocation(Portfolio portfolio);

    /**
     *
     * @param portfolio
     */
    void trade(Portfolio portfolio);
}
