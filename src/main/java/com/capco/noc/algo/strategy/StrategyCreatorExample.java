package com.capco.noc.algo.strategy;

import com.capco.noc.algo.schema.Portfolio;
import com.capco.noc.algo.schema.Strategy;
import com.capco.noc.algo.schema.Ticker;
import com.capco.noc.algo.schema.strategy.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StrategyCreatorExample implements StrategyCreator {

    @Override
    public void defineInitialPortfolioAllocation(Portfolio portfolio) {
        portfolio.setInitialAllocation(new HashMap<Ticker, Double>(){{
            put(Ticker.A, 20.0);
            put(Ticker.D, 20.0);
            put(Ticker.B, 20.0);
            put(Ticker.C, 20.0);
        }});
    }

    @Override
    public void defineAccountStrategy(Portfolio portfolio) {
        Strategy strategy = new Strategy();

        strategy.getUnits().addAll(prepareStrategyCompanyA());
        strategy.getUnits().addAll(prepareStrategyCompanyB());
        strategy.getUnits().addAll(prepareStrategyCompanyC());
        strategy.getUnits().addAll(prepareStrategyCompanyD());

        portfolio.setStrategy(strategy, this.getClass().getSimpleName());
    }

    private List<StrategyUnit> prepareStrategyCompanyA(){
        List<StrategyUnit> strategies = new ArrayList<>();

        //Prepare STOP LOSS strategy
        StrategyUnitStopLoss stopLoss = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
        stopLoss.setValueType(StrategyUnitStopLoss.ValueType.STATIC);
        stopLoss.setPositionSellType(StrategyUnitStopLoss.PositionSellType.POSITION_MAP);
        stopLoss.setTriggerPriceMap(new HashMap<Double, Double>(){{
            put(78.0, 25.0);     //When price drops to 78/share, sell 25% of currently held position
            put(75.0, 35.0);     //When price drops to 75/share, sell 35% of currently held position
            put(73.0, 45.0);     //When price drops to 73/share, sell 45% of currently held position
            put(70.0, 50.0);     //When price drops to 70/share, sell 50% of currently held position
            put(68.0, 75.0);    //When price drops to 68/share, sell 75% of currently held position
        }});

        //Prepare BUY strategy
        StrategyUnitBuy buy = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
        buy.setValueType(StrategyUnitBuy.ValueType.STATIC);
        buy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP);
        buy.setTriggerPriceMap(new HashMap<Double, Double>(){{
            put(79.0, 25.0);     //When price drops to 77/share, buy using 25% of currently free cash balance
            put(76.0, 35.0);     //When price drops to 74/share, buy using 35% of currently free cash balance
            put(74.0, 45.0);     //When price drops to 72/share, buy using 45% of currently free cash balance
            put(71.0, 50.0);     //When price drops to 69/share, buy using 50% of currently free cash balance
            put(69.0, 100.0);    //When price drops to 67/share, buy using 100% of currently free cash balance
        }});

        //Prepare TAKE PROFIT strategy
        StrategyUnitTakeProfit takeProfit = new StrategyUnitTakeProfit(Ticker.A, Indicator.CURRENT_PRICE);
        takeProfit.setValueType(StrategyUnitTakeProfit.ValueType.STATIC);
        takeProfit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP);
        takeProfit.setTriggerPriceMap(new HashMap<Double, Double>(){{
            put(85.0, 30.0);    //When price hikes to 85/share, sell 30% of currently held position
            put(87.0, 40.0);    //When price hikes to 87/share, sell 40% of currently held position
            put(89.0, 50.0);    //When price hikes to 89/share, sell 50% of currently held position
            put(95.0, 60.0);    //When price hikes to 95/share, sell 60% of currently held position
            put(100.0, 80.0);   //When price hikes to 100/share, sell 80% of currently held position
        }});

        strategies.add(takeProfit);
        strategies.add(stopLoss);
        strategies.add(buy);

        return strategies;
    }

    private List<StrategyUnit> prepareStrategyCompanyB(){
        List<StrategyUnit> strategies = new ArrayList<>();

        //Prepare STOP LOSS strategy
        StrategyUnitStopLoss stopLoss = new StrategyUnitStopLoss(Ticker.B, Indicator.CURRENT_PRICE);
        stopLoss.setValueType(StrategyUnitStopLoss.ValueType.STATIC);
        stopLoss.setPositionSellType(StrategyUnitStopLoss.PositionSellType.ENTIRE_POSITION);
        stopLoss.setTriggerPrice(25.0); //Sell everything when price drops to 65/share

        //Prepare BUY strategy
        StrategyUnitBuy buy = new StrategyUnitBuy(Ticker.B, Indicator.CURRENT_PRICE);
        buy.setValueType(StrategyUnitBuy.ValueType.STATIC);
        buy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
        buy.setTriggerPrice(26.0);  //Buy with all free cash, when price drops to 60/share

        //Prepare TAKE PROFIT strategy
        StrategyUnitTakeProfit takeProfit = new StrategyUnitTakeProfit(Ticker.B, Indicator.CURRENT_PRICE);
        takeProfit.setValueType(StrategyUnitTakeProfit.ValueType.STATIC);
        takeProfit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.ENTIRE_POSITION);
        takeProfit.setTriggerPrice(35.0);  //Sell everything when price drops to 105/share

        strategies.add(takeProfit);
        strategies.add(stopLoss);
        strategies.add(buy);

        return strategies;
    }

    private List<StrategyUnit> prepareStrategyCompanyC(){
        List<StrategyUnit> strategies = new ArrayList<>();

        //Prepare STOP LOSS strategy
        StrategyUnitStopLoss stopLoss = new StrategyUnitStopLoss(Ticker.C, Indicator.CURRENT_PRICE);
        stopLoss.setValueType(StrategyUnitStopLoss.ValueType.PERCENT);
        stopLoss.setPositionSellType(StrategyUnitStopLoss.PositionSellType.POSITION_MAP);
        stopLoss.setTriggerPercentageMap(new HashMap<Double, Double>(){{
            put(5.0, 25.0);     //When price drops 5%, sell 25% of currently held position
            put(7.0, 35.0);     //When price drops 7%, sell 35% of currently held position
            put(9.0, 45.0);     //When price drops 9%, sell 45% of currently held position
            put(10.0, 50.0);    //When price drops 10%, sell 50% of currently held position
            put(15.0, 100.0);   //When price drops 15%, sell 100% of currently held position
        }});

        //Prepare BUY strategy
        StrategyUnitBuy buy = new StrategyUnitBuy(Ticker.C, Indicator.CURRENT_PRICE);
        buy.setValueType(StrategyUnitBuy.ValueType.PERCENT);
        buy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP);
        buy.setTriggerPercentageMap(new HashMap<Double, Double>(){{
            put(4.0, 25.0);     //When price drops 4%, buy using 25% of currently free cash balance
            put(6.0, 35.0);     //When price drops 6%, buy using 35% of currently free cash balance
            put(8.0, 45.0);     //When price drops 8%, buy using 45% of currently free cash balance
            put(11.0, 50.0);    //When price drops 11%, buy using 50% of currently free cash balance
            put(15.0, 100.0);   //When price drops 15%, buy using 100% of currently free cash balance
        }});

        //Prepare TAKE PROFIT strategy
        StrategyUnitTakeProfit takeProfit = new StrategyUnitTakeProfit(Ticker.C, Indicator.CURRENT_PRICE);
        takeProfit.setValueType(StrategyUnitTakeProfit.ValueType.PERCENT);
        takeProfit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP);
        takeProfit.setTriggerPercentageMap(new HashMap<Double, Double>(){{
            put(20.0, 30.0);    //When price hikes 20%, sell 30% of currently held position
            put(22.0, 40.0);    //When price hikes 22%, sell 40% of currently held position
            put(25.0, 50.0);    //When price hikes 25%, sell 50% of currently held position
            put(30.0, 60.0);    //When price hikes 30%, sell 60% of currently held position
            put(50.0, 100.0);   //When price hikes 50%, sell 100% of currently held position
        }});

        strategies.add(takeProfit);
        strategies.add(stopLoss);
        strategies.add(buy);

        return strategies;
    }

    private List<StrategyUnit> prepareStrategyCompanyD(){
        List<StrategyUnit> strategies = new ArrayList<>();

        //Prepare STOP LOSS strategy
        StrategyUnitStopLoss stopLoss = new StrategyUnitStopLoss(Ticker.D, Indicator.CURRENT_PRICE);
        stopLoss.setValueType(StrategyUnitStopLoss.ValueType.PERCENT);
        stopLoss.setPositionSellType(StrategyUnitStopLoss.PositionSellType.ENTIRE_POSITION);
        stopLoss.setTriggerPercentage(30.0);    //Sell everything when price drops 30%

        //Prepare BUY strategy
        StrategyUnitBuy buy = new StrategyUnitBuy(Ticker.D, Indicator.CURRENT_PRICE);
        buy.setValueType(StrategyUnitBuy.ValueType.PERCENT);
        buy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
        buy.setTriggerPercentage(40.0); //Buy with all free cash when price drops 40%

        //Prepare TAKE PROFIT strategy
        StrategyUnitTakeProfit takeProfit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
        takeProfit.setValueType(StrategyUnitTakeProfit.ValueType.PERCENT);
        takeProfit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.ENTIRE_POSITION);
        takeProfit.setTriggerPercentage(30.0);  //Sell everything when price hikes 30%

        strategies.add(takeProfit);
        strategies.add(stopLoss);
        strategies.add(buy);

        return strategies;
    }
}
