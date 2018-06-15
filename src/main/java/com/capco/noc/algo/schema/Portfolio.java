package com.capco.noc.algo.schema;

import com.capco.noc.algo.schema.strategy.StrategyUnit;
import com.capco.noc.algo.schema.strategy.StrategyUnitBuy;
import com.capco.noc.algo.schema.strategy.StrategyUnitTakeProfit;
import com.capco.noc.algo.schema.strategy.StrategyUnitStopLoss;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {

    private double cashBalance;
    private String currency;
    private List<Product> products = new ArrayList<>();

    private Map<Ticker, Double> initialAllocation;
    private Strategy strategy;
    private String strategyName;

    private Map<String, PortfolioSnapshot> snapshotList = new LinkedHashMap<>();
    private PortfolioSnapshot firstSnapshot;

    public Portfolio() {}

    public double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Map<Ticker, Double> getInitialAllocation() {
        return initialAllocation;
    }

    public void setInitialAllocation(Map<Ticker, Double> initialAllocation) {
        this.initialAllocation = initialAllocation;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    /*
     * TODO - move this logic to a service method
     *
     * As strategy initialization is done after the initial portfolio allocation, we can set some
     * default on the strategy units
     * */
    public void setStrategy(Strategy strategy, String strategyName) {
        this.strategy = strategy;
        this.strategyName = strategyName;

        for(StrategyUnit unit: strategy.getUnits()){

            //Stop loss default initiation
            if(unit.getClass().equals(StrategyUnitStopLoss.class)){
                StrategyUnitStopLoss stopLossUnit = (StrategyUnitStopLoss) unit;

                for(Product product: products){
                    if(stopLossUnit.getTicker().equals(product.getTicker())){
                        stopLossUnit.setLastValue(product.getInitialTick());
                    }
                }
            } else if(unit.getClass().equals(StrategyUnitTakeProfit.class)) {

                //Take profit default initiation
                StrategyUnitTakeProfit takeProfitUnit = (StrategyUnitTakeProfit) unit;

                for(Product product: products){
                    if(takeProfitUnit.getTicker().equals(product.getTicker())){
                        takeProfitUnit.setLastValue(product.getInitialTick());
                    }
                }
            } else if(unit.getClass().equals(StrategyUnitBuy.class)) {

                //Buy default initiation
                StrategyUnitBuy buyUnit = (StrategyUnitBuy) unit;

                for(Product product: products){
                    if(buyUnit.getTicker().equals(product.getTicker())){
                        buyUnit.setReferenceValue(product.getInitialTick());
                    }
                }
            }
        }
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public Map<String, PortfolioSnapshot> getSnapshotList() {
        return snapshotList;
    }

    public void setSnapshotList(Map<String, PortfolioSnapshot> snapshotList) {
        this.snapshotList = snapshotList;
    }

    public PortfolioSnapshot getFirstSnapshot() {
        return firstSnapshot;
    }

    public void setFirstSnapshot(PortfolioSnapshot firstSnapshot) {
        this.firstSnapshot = firstSnapshot;
    }
}
