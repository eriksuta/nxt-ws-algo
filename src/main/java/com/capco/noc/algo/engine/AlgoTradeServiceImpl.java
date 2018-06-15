package com.capco.noc.algo.engine;

import com.capco.noc.algo.schema.*;
import com.capco.noc.algo.schema.strategy.StrategyUnit;
import com.capco.noc.algo.schema.strategy.StrategyUnitBuy;
import com.capco.noc.algo.schema.strategy.StrategyUnitStopLoss;
import com.capco.noc.algo.schema.strategy.StrategyUnitTakeProfit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AlgoTradeServiceImpl implements AlgoTradeService{

    /*
     * key - date
     * value - list containing tuples with Ticker and price for that day
     * */
    private Map<String, List<Tick>> stockPriceMap = new LinkedHashMap<>();

    private BrokerService brokerService;

    @Autowired
    AlgoTradeServiceImpl(BrokerService brokerService){
        this.brokerService = brokerService;
    }

    public void setStockPriceMap(Map<String, List<Tick>> stockPriceMap) {
        this.stockPriceMap = stockPriceMap;
    }

    /*
     * TODO - document
     * */
    public void performInitialPortfolioAllocation(Portfolio portfolio){
        System.out.println("========== Initial Portfolio Allocation ==========");

        //If initial allocation not set, define everything at 0
        for(Ticker availableTicker: Ticker.values()){
            portfolio.getInitialAllocation().putIfAbsent(availableTicker, 0.0);
        }

        //Clear products in portfolio by invalidating old portfolio list
        portfolio.setProducts(new ArrayList<>());
        portfolio.setSnapshotList(new LinkedHashMap<>());

        double cashBalance = portfolio.getCashBalance();
        Map<Ticker, Double> initialAllocation = portfolio.getInitialAllocation();

        String date = stockPriceMap.entrySet().iterator().next().getKey();
        for(Ticker ticker: initialAllocation.keySet()){
            Product product = new Product();
            product.setId(UUID.randomUUID().toString());
            product.setTicker(ticker);
            portfolio.getProducts().add(product);

            List<Tick> initialTicks = stockPriceMap.entrySet().iterator().next().getValue();
            initialTicks.forEach(tick -> {
                if(ticker.equals(tick.getTicker())){
                    double amountToSpend = (initialAllocation.get(ticker)/100.0)*cashBalance;
                    double unitPrice = tick.getValue();
                    int position = (int)(amountToSpend/unitPrice);

                    //Perform the transaction
                    if(position > 0) {
                        brokerService.buy(portfolio, ticker, position, unitPrice, date);
                    } else {
                        product.setUnitValue(unitPrice);
                        product.setPosition(0);
                        product.setValue(0.0);
                    }
                    product.setInitialTick(tick);
                }
            });
        }

        //Take snapshot after initial allocation
        takeSnapshot(portfolio, date);
        takeFirstSnapshot(portfolio);

        //Remove first-days values
        stockPriceMap.remove(date);
    }

    /*
     * TODO - document
     * */
    public void trade(Portfolio portfolio){
        System.out.println("========== Trading ==========");
        String year = null;

        for(Map.Entry<String, List<Tick>> entry: stockPriceMap.entrySet()){
            String date = entry.getKey();

            //If this is the 1st trading day of the year - take portfolio snapshot for displaying purposes
            String tickYear = date.substring(0, 4);
            if(year == null){ year = tickYear; }
            if(!tickYear.equals(year)){
                year = tickYear;
                takeSnapshot(portfolio, date);
            }

            for(Tick tick: entry.getValue()){

                //Perform stop-loss operations
                evaluateStopLoss(tick, portfolio, date);

                //Perform take-profit operations
                evaluateTakeProfit(tick, portfolio, date);

                //Perform buy operations
                evaluateBuy(tick, portfolio, date);

                //Update products in portfolio with current prices
                for(Product product: portfolio.getProducts()){
                    if(product.getTicker().equals(tick.getTicker())){
                        product.setUnitValue(tick.getValue());
                    }
                }
            }
        }

        takeSnapshot(portfolio, "Today");
    }

    /*
     * TODO - document
     * */
    private void evaluateStopLoss(Tick tick, Portfolio portfolio, String date){
        StrategyUnitStopLoss unit = (StrategyUnitStopLoss)getStrategyForTicker(tick.getTicker(), portfolio.getStrategy(), StrategyUnitStopLoss.class);

        if(unit == null){
            return;
        }

        //If there is no position of the product in portfolio, no need to continue
        if(!portfolioHasTicker(portfolio, tick.getTicker())){
            return;
        }

        //If price has gone up from last time, stop-loss will not be applied, no need to continue
        Product product = findProduct(portfolio, tick.getTicker());
        if(product.getUnitValue() <= tick.getValue()){
            return;
        }

        switch (unit.getPositionSellType()){
            case ENTIRE_POSITION:
                switch (unit.getValueType()){
                    case STATIC:
                        if(tick.getValue() <= unit.getTriggerPrice()){
                            //Sell 100% of currently hold position
                            System.out.print("SL ");
                            brokerService.sell(portfolio, tick.getTicker(), product.getPosition(), tick.getValue(), date);
                            updateBuyStrategyAfterStopLossSell(portfolio, tick.getTicker(), tick);
                        }

                        break;
                    case PERCENT:
                        double percentChange = (1.0 -(tick.getValue()/unit.getLastValue())) * 100.0;
                        if(percentChange > unit.getTriggerPercentage()){
                            //Sell 100% of currently hold position
                            System.out.print("SL ");
                            brokerService.sell(portfolio, tick.getTicker(), product.getPosition(), tick.getValue(), date);
                            updateBuyStrategyAfterStopLossSell(portfolio, tick.getTicker(), tick);
                        }
                        break;
                    default:
                        break;
                }

                break;
            case POSITION_MAP:
                double percentToSell;
                switch (unit.getValueType()){
                    case STATIC:
                        percentToSell = 0;
                        for(double dropLevel: unit.getTriggerPriceMap().keySet()){
                            if(tick.getValue() <= dropLevel){
                                if(unit.getTriggerPriceMap().get(dropLevel) >= percentToSell) {
                                    percentToSell = unit.getTriggerPriceMap().get(dropLevel);
                                }
                            }
                        }

                        //Perform SELL operation
                        if(percentToSell > 0){
                            int position = (int)((percentToSell/100)*product.getPosition());

                            if(position > 0) {
                                System.out.print("SL ");
                                brokerService.sell(portfolio, tick.getTicker(), position, tick.getValue(), date);
                                updateBuyStrategyAfterStopLossSell(portfolio, tick.getTicker(), tick);

                                //Update the strategy after sell
                                Map<Double, Double> newPriceMap = new HashMap<>();
                                for (Map.Entry<Double, Double> entry : unit.getTriggerPriceMap().entrySet()) {
                                    double pricePointDelta = entry.getKey() - unit.getLastValue();
                                    newPriceMap.put(tick.getValue() + pricePointDelta, entry.getValue());
                                }

                                unit.setTriggerPriceMap(newPriceMap);
                                unit.setLastValue(tick);
                            }
                        }

                        break;
                    case PERCENT:
                        percentToSell = 0;
                        double percentDrop = (1.0 - (tick.getValue()/unit.getLastValue())) * 100.0;
                        for(double percentDropLevel: unit.getTriggerPercentageMap().keySet()){
                            if(percentDrop > percentDropLevel){
                                if(unit.getTriggerPercentageMap().get(percentDropLevel) >= percentToSell) {
                                    percentToSell = unit.getTriggerPercentageMap().get(percentDropLevel);
                                }
                            }
                        }

                        //Perform SELL operation
                        if(percentToSell > 0){
                            int position = (int)((percentToSell/100)*product.getPosition());

                            if(position > 0) {
                                System.out.print("SL ");
                                brokerService.sell(portfolio, tick.getTicker(), position, tick.getValue(), date);
                                updateBuyStrategyAfterStopLossSell(portfolio, tick.getTicker(), tick);

                                //Update the strategy after sell
                                unit.setLastValue(tick);
                            }
                        }

                        break;
                    default:
                        break;
                }

                break;
            default:
                break;

        }   
    }

    /*
    * TODO - document
    * */
    private void evaluateTakeProfit(Tick tick, Portfolio portfolio, String date){
        StrategyUnitTakeProfit unit = (StrategyUnitTakeProfit) getStrategyForTicker(tick.getTicker(), portfolio.getStrategy(), StrategyUnitTakeProfit.class);

        if(unit == null){
            return;
        }

        //If there is no position of the product in portfolio, no need to continue
        if(!portfolioHasTicker(portfolio, tick.getTicker())){
            return;
        }

        //If price has gone down from last time, take-profit will not be applied, no need to continue
        Product product = findProduct(portfolio, tick.getTicker());
        if(product.getUnitValue() >= tick.getValue()){
            return;
        }

        switch (unit.getPositionSellType()){
            case ENTIRE_POSITION:
                switch (unit.getValueType()){
                    case STATIC:
                        if(tick.getValue() >= unit.getTriggerPrice()){
                            //Sell 100% of currently hold position
                            System.out.print("TP ");
                            brokerService.sell(portfolio, tick.getTicker(), product.getPosition(), tick.getValue(), date);
                            updateBuyStrategyAfterTakeProfitSell(portfolio, tick.getTicker(), tick);
                        }

                        break;
                    case PERCENT:
                        double percentChange = ((tick.getValue()/unit.getLastValue()) - 1.0) * 100.0;
                        if(percentChange > unit.getTriggerPercentage()){
                            //Sell 100% of currently hold position
                            System.out.print("TP ");
                            brokerService.sell(portfolio, tick.getTicker(), product.getPosition(), tick.getValue(), date);
                            updateBuyStrategyAfterTakeProfitSell(portfolio, tick.getTicker(), tick);
                        }

                        break;
                    default:
                        break;
                }

                break;
            case POSITION_MAP:
                double percentToSell;
                switch (unit.getValueType()){
                    case STATIC:
                        percentToSell = 0;
                        for(double hikeLevel: unit.getTriggerPriceMap().keySet()){
                            if(tick.getValue() >= hikeLevel){
                                if(unit.getTriggerPriceMap().get(hikeLevel) >= percentToSell) {
                                    percentToSell = unit.getTriggerPriceMap().get(hikeLevel);
                                }
                            }
                        }

                        //Perform SELL operation
                        if(percentToSell > 0){
                            int position = (int)((percentToSell/100) * product.getPosition());

                            if(position > 0) {
                                System.out.print("TP ");
                                brokerService.sell(portfolio, tick.getTicker(), position, tick.getValue(), date);
                                updateBuyStrategyAfterTakeProfitSell(portfolio, tick.getTicker(), tick);

                                //Update the strategy after sell
                                Map<Double, Double> newPriceMap = new HashMap<>();
                                for (Map.Entry<Double, Double> entry : unit.getTriggerPriceMap().entrySet()) {
                                    double pricePointDelta = entry.getKey() - unit.getLastValue();
                                    newPriceMap.put(tick.getValue() + pricePointDelta, entry.getValue());
                                }

                                unit.setTriggerPriceMap(newPriceMap);
                                unit.setLastValue(tick);
                            }
                        }

                        break;
                    case PERCENT:
                        percentToSell = 0;
                        double percentHike = ((tick.getValue()/unit.getLastValue()) - 1.0) * 100.0;
                        for(double percentHikeLevel: unit.getTriggerPercentageMap().keySet()){
                            if(percentHike > percentHikeLevel){
                                if(unit.getTriggerPercentageMap().get(percentHikeLevel) >= percentToSell) {
                                    percentToSell = unit.getTriggerPercentageMap().get(percentHikeLevel);
                                }
                            }
                        }

                        //Perform SELL operation
                        if(percentToSell > 0){
                            int position = (int)((percentToSell/100) * product.getPosition());

                            if(position > 0) {
                                System.out.print("TP ");
                                brokerService.sell(portfolio, tick.getTicker(), position, tick.getValue(), date);
                                updateBuyStrategyAfterTakeProfitSell(portfolio, tick.getTicker(), tick);

                                //Update the strategy after sell
                                unit.setLastValue(tick);
                            }
                        }

                        break;
                    default:
                        break;
                }

                break;
            default:
                break;

        }
    }

    /*
    * TODO - document
    * */
    private void evaluateBuy(Tick tick, Portfolio portfolio, String date){
        StrategyUnitBuy unit = (StrategyUnitBuy) getStrategyForTicker(tick.getTicker(), portfolio.getStrategy(), StrategyUnitBuy.class);
        Product product = findProduct(portfolio, tick.getTicker());

        if(unit == null){
            return;
        }

        //If price has gone up from last time, buy will not be applied, no need to continue
        if(product.getUnitValue() <= tick.getValue()){
            return;
        }

        switch (unit.getFundsAllocationType()){
            case ENTIRE_CASH_BALANCE:
                switch (unit.getValueType()){
                    case STATIC:
                        if(tick.getValue() <= unit.getTriggerPrice()){
                            //Use all currently available cash to buy the product
                            int positionToBuy = (int)(portfolio.getCashBalance()/tick.getValue());
                            if(positionToBuy == 0){
                                break;
                            }

                            brokerService.buy(portfolio, tick.getTicker(), positionToBuy, tick.getValue(), date);
                            updateStrategiesAfterBuy(portfolio, tick.getTicker(), tick);

                            //Update the trigger price
                            unit.setTriggerPrice(tick.getValue() - (unit.getReferenceValue() - unit.getTriggerPrice()));
                            unit.setReferenceValue(tick);
                        }

                        break;
                    case PERCENT:
                        double percentChange = (1.0 - (tick.getValue()/unit.getReferenceValue())) * 100.0;
                        if(percentChange > unit.getTriggerPercentage()){
                            //Use all currently available cash to buy the product
                            int positionToBuy = (int)(portfolio.getCashBalance()/tick.getValue());
                            if(positionToBuy == 0){
                                break;
                            }

                            brokerService.buy(portfolio, tick.getTicker(), positionToBuy, tick.getValue(), date);
                            updateStrategiesAfterBuy(portfolio, tick.getTicker(), tick);

                            //Update the trigger percentage
                            unit.setReferenceValue(tick);
                        }

                        break;
                    default:
                        break;
                }

                break;
            case ALLOCATION_MAP:
                double percentToAllocate;
                switch (unit.getValueType()){
                    case STATIC:
                        percentToAllocate = 0;
                        for(double priceLevel: unit.getTriggerPriceMap().keySet()){
                            if(tick.getValue() <= priceLevel){
                                if(unit.getTriggerPriceMap().get(priceLevel) >= percentToAllocate) {
                                    percentToAllocate = unit.getTriggerPriceMap().get(priceLevel);
                                }
                            }
                        }

                        //Perform BUY operation
                        if(percentToAllocate > 0){
                            int positionToBuy = (int)(((percentToAllocate/100) * portfolio.getCashBalance())/tick.getValue());
                            if(positionToBuy == 0){
                                break;
                            }

                            brokerService.buy(portfolio, tick.getTicker(), positionToBuy, tick.getValue(), date);
                            updateStrategiesAfterBuy(portfolio, tick.getTicker(), tick);

                            //Update the strategy after buy
                            Map<Double, Double> newPriceMap = new HashMap<>();
                            for(Map.Entry<Double, Double> entry: unit.getTriggerPriceMap().entrySet()){
                                double pricePointDelta = unit.getReferenceValue() - entry.getKey();
                                newPriceMap.put(tick.getValue() - pricePointDelta, entry.getValue());
                            }

                            unit.setTriggerPriceMap(newPriceMap);
                            unit.setReferenceValue(tick);
                        }

                        break;
                    case PERCENT:
                        percentToAllocate = 0;
                        double percentDrop = (1.0 - (tick.getValue()/unit.getReferenceValue())) * 100.0;
                        for(double percentDropLevel: unit.getTriggerPercentageMap().keySet()){
                            if(percentDrop > percentDropLevel){
                                if(unit.getTriggerPercentageMap().get(percentDropLevel) >= percentToAllocate) {
                                    percentToAllocate = unit.getTriggerPercentageMap().get(percentDropLevel);
                                }
                            }
                        }

                        //Perform BUY operation
                        if(percentToAllocate > 0){
                            int positionToBuy = (int)(((percentToAllocate/100) * portfolio.getCashBalance())/tick.getValue());
                            if(positionToBuy == 0){
                                break;
                            }

                            brokerService.buy(portfolio, tick.getTicker(), positionToBuy, tick.getValue(), date);
                            updateStrategiesAfterBuy(portfolio, tick.getTicker(), tick);

                            //Update the strategy after buy
                            unit.setReferenceValue(tick);
                        }

                        break;
                    default:
                        break;
                }

                break;
            default:
                break;

        }
    }

    /*
     * TODO - document
     * */
    private void updateStrategiesAfterBuy(Portfolio portfolio, Ticker ticker, Tick referenceTick){
        for(StrategyUnit unit: portfolio.getStrategy().getUnits()){
            for(Product product: portfolio.getProducts()){
                if(ticker.equals(product.getTicker()) && ticker.equals(unit.getTicker())){
                    //Update Stop loss units, if present
                    if(unit.getClass().equals(StrategyUnitStopLoss.class)){
                        StrategyUnitStopLoss stopLossUnit = (StrategyUnitStopLoss) unit;
                        if(StrategyUnitStopLoss.ValueType.PERCENT.equals(stopLossUnit.getValueType())){
                            stopLossUnit.setLastValue(referenceTick);
                        }

                        if(StrategyUnitStopLoss.ValueType.STATIC.equals(stopLossUnit.getValueType())){
                            if(StrategyUnitStopLoss.PositionSellType.ENTIRE_POSITION.equals(stopLossUnit.getPositionSellType())){
                                double triggerPriceDelta = stopLossUnit.getLastValue() - stopLossUnit.getTriggerPrice();

                                stopLossUnit.setLastValue(referenceTick);
                                stopLossUnit.setTriggerPrice(stopLossUnit.getLastValue() - triggerPriceDelta);
                            }

                            if(StrategyUnitStopLoss.PositionSellType.POSITION_MAP.equals(stopLossUnit.getPositionSellType())){
                                Map<Double, Double> newPriceMap = new HashMap<>();
                                double oldReferenceValue = stopLossUnit.getLastValue();
                                stopLossUnit.setLastValue(referenceTick);

                                for(Map.Entry<Double, Double> entry: stopLossUnit.getTriggerPriceMap().entrySet()){
                                    double pricePointDelta = oldReferenceValue - entry.getKey();
                                    newPriceMap.put(stopLossUnit.getLastValue() - pricePointDelta, entry.getValue());
                                }

                                stopLossUnit.setTriggerPriceMap(newPriceMap);
                            }
                        }
                    }

                    //Update Take profit units, if present
                    if(unit.getClass().equals(StrategyUnitTakeProfit.class)){
                        StrategyUnitTakeProfit takeProfitUnit = (StrategyUnitTakeProfit) unit;
                        if(StrategyUnitTakeProfit.ValueType.PERCENT.equals(takeProfitUnit.getValueType())){
                            takeProfitUnit.setLastValue(referenceTick);
                        }

                        if(StrategyUnitTakeProfit.ValueType.STATIC.equals(takeProfitUnit.getValueType())){
                            if(StrategyUnitTakeProfit.PositionSellType.ENTIRE_POSITION.equals(takeProfitUnit.getPositionSellType())){
                                double triggerPriceDelta = takeProfitUnit.getTriggerPrice() - takeProfitUnit.getLastValue();

                                takeProfitUnit.setLastValue(referenceTick);
                                takeProfitUnit.setTriggerPrice(takeProfitUnit.getLastValue() + triggerPriceDelta);
                            }

                            if(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP.equals(takeProfitUnit.getPositionSellType())) {
                                Map<Double, Double> newPriceMap = new HashMap<>();
                                double oldReferenceValue = takeProfitUnit.getLastValue();
                                takeProfitUnit.setLastValue(referenceTick);

                                for (Map.Entry<Double, Double> entry : takeProfitUnit.getTriggerPriceMap().entrySet()) {
                                    double pricePointDelta = entry.getKey() - oldReferenceValue;
                                    newPriceMap.put(takeProfitUnit.getLastValue() + pricePointDelta, entry.getValue());
                                }

                                takeProfitUnit.setTriggerPriceMap(newPriceMap);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateBuyStrategyAfterTakeProfitSell(Portfolio portfolio, Ticker ticker, Tick referenceTick){
        for(StrategyUnit unit: portfolio.getStrategy().getUnits()){
            for(Product product: portfolio.getProducts()){
                if(ticker.equals(product.getTicker()) && ticker.equals(unit.getTicker())){
                    if(unit.getClass().equals(StrategyUnitBuy.class)){
                        StrategyUnitBuy buyUnit = (StrategyUnitBuy) unit;

                        if(StrategyUnitBuy.ValueType.PERCENT.equals(buyUnit.getValueType())){
                            buyUnit.setReferenceValue(referenceTick);
                        }

                        if(StrategyUnitBuy.ValueType.STATIC.equals(buyUnit.getValueType())){
                            if(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE.equals(buyUnit.getFundsAllocationType())){
                                double triggerPriceDelta = buyUnit.getReferenceValue() - buyUnit.getTriggerPrice();

                                buyUnit.setReferenceValue(referenceTick);
                                buyUnit.setTriggerPrice(buyUnit.getReferenceValue() - triggerPriceDelta);
                            }

                            if(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP.equals(buyUnit.getFundsAllocationType())) {
                                Map<Double, Double> newPriceMap = new HashMap<>();
                                double oldReferenceValue = buyUnit.getReferenceValue();
                                buyUnit.setReferenceValue(referenceTick);

                                for (Map.Entry<Double, Double> entry : buyUnit.getTriggerPriceMap().entrySet()) {
                                    double pricePointDelta = oldReferenceValue - entry.getKey();
                                    newPriceMap.put(buyUnit.getReferenceValue() - pricePointDelta, entry.getValue());
                                }

                                buyUnit.setTriggerPriceMap(newPriceMap);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateBuyStrategyAfterStopLossSell(Portfolio portfolio, Ticker ticker, Tick referenceTick){
        for(StrategyUnit unit: portfolio.getStrategy().getUnits()){
            for(Product product: portfolio.getProducts()){
                if(ticker.equals(product.getTicker()) && ticker.equals(unit.getTicker())){
                    if(unit.getClass().equals(StrategyUnitBuy.class)){
                        StrategyUnitBuy buyUnit = (StrategyUnitBuy) unit;

                        if(StrategyUnitBuy.ValueType.PERCENT.equals(buyUnit.getValueType())){
                            buyUnit.setReferenceValue(referenceTick);
                        }

                        if(StrategyUnitBuy.ValueType.STATIC.equals(buyUnit.getValueType())){
                            if(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE.equals(buyUnit.getFundsAllocationType())){
                                double triggerPriceDelta = buyUnit.getReferenceValue() - buyUnit.getTriggerPrice();

                                buyUnit.setReferenceValue(referenceTick);
                                buyUnit.setTriggerPrice(buyUnit.getReferenceValue() - triggerPriceDelta);
                            }

                            if(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP.equals(buyUnit.getFundsAllocationType())) {
                                Map<Double, Double> newPriceMap = new HashMap<>();
                                double oldReferenceValue = buyUnit.getReferenceValue();
                                buyUnit.setReferenceValue(referenceTick);

                                for (Map.Entry<Double, Double> entry : buyUnit.getTriggerPriceMap().entrySet()) {
                                    double pricePointDelta = oldReferenceValue - entry.getKey();
                                    newPriceMap.put(buyUnit.getReferenceValue() - pricePointDelta, entry.getValue());
                                }

                                buyUnit.setTriggerPriceMap(newPriceMap);
                            }
                        }
                    }
                }
            }
        }
    }

    private void takeSnapshot(Portfolio portfolio, String date){
        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setCashBalance(portfolio.getCashBalance());

        for(Product product: portfolio.getProducts()){
            snapshot.getProductListSnapshot().add(new PortfolioSnapshot.ProductSnapshot(
                    product.getTicker(), product.getUnitValue(), product.getPosition()
            ));
        }

        portfolio.getSnapshotList().put(date, snapshot);
    }

    private void takeFirstSnapshot(Portfolio portfolio){
        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setCashBalance(portfolio.getCashBalance());

        for(Product product: portfolio.getProducts()){
            snapshot.getProductListSnapshot().add(new PortfolioSnapshot.ProductSnapshot(
                    product.getTicker(), product.getUnitValue(), product.getPosition()
            ));
        }

        portfolio.setFirstSnapshot(snapshot);
    }

    /*==================================================*/
    /* Helper Methods                                   */
    /*==================================================*/
    private StrategyUnit getStrategyForTicker(Ticker ticker, Strategy strategy, Class<? extends StrategyUnit> strategyType){
        if(strategy == null || strategyType == null){
            return null;
        }

        for(StrategyUnit unit: strategy.getUnits()){
            if(ticker.equals(unit.getTicker()) && unit.getClass().equals(strategyType)){
                return unit;
            }
        }

        return null;
    }

    private boolean portfolioHasTicker(Portfolio portfolio, Ticker ticker){
        for(Product product: portfolio.getProducts()){
            if(ticker.equals(product.getTicker()) && product.getPosition() > 0){
                return true;
            }
        }

        return false;
    }

    private Product findProduct(Portfolio portfolio, Ticker ticker){
        for(Product product: portfolio.getProducts()){
            if(ticker.equals(product.getTicker())){
                return product;
            }
        }

        return null;
    }
}
