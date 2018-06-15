package com.capco.noc.algo.engine;

import com.capco.noc.algo.schema.Portfolio;
import com.capco.noc.algo.schema.Ticker;

public interface BrokerService {

    /**
     * Performs BUY operation
     *
     * @param portfolio
     * @param ticker
     * @param position
     * @param unitPrice
     * @param date
     */
    void buy(Portfolio portfolio, Ticker ticker, int position, double unitPrice, String date);

    /**
     * Performs SELL operation
     *
     * @param portfolio
     * @param ticker
     * @param position
     * @param unitPrice
     * @param date
     */
    void sell(Portfolio portfolio, Ticker ticker, int position, double unitPrice, String date);
}
