package com.capco.noc.algo;

import com.capco.noc.algo.engine.AlgoTradeService;
import com.capco.noc.algo.repository.AccountRepository;
import com.capco.noc.algo.schema.*;
import com.capco.noc.algo.schema.strategy.Indicator;
import com.capco.noc.algo.schema.strategy.StrategyUnitBuy;
import com.capco.noc.algo.schema.strategy.StrategyUnitStopLoss;
import com.capco.noc.algo.schema.strategy.StrategyUnitTakeProfit;
import com.capco.noc.algo.util.FormatterUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AlgoTraderApplicationTests {

	//!!! WARNING - do not change these values, will brake tests
	private static final double INITIAL_CASH_BALANCE = 100000.00;
	private static final double A_INITIAL_ALLOCATION_PERCENT = 10.0;
	private static final double D_INITIAL_ALLOCATION_PERCENT = 20.0;
	private static final double B_INITIAL_ALLOCATION_PERCENT = 15.0;
	private static final double C_INITIAL_ALLOCATION_PERCENT = 25.0;

	private static final double A_INITIAL_PRICE = 60.00;
	private static final double A_SECOND_PRICE = 50.00;
	private static final double A_THIRD_PRICE = 40.00;

	private static final double D_INITIAL_PRICE = 20.00;
	private static final double D_SECOND_PRICE = 27.00;
	private static final double D_THIRD_PRICE = 30.00;

	private static final double B_INITIAL_PRICE = 100.00;
	private static final double B_SECOND_PRICE = 110.00;
	private static final double B_THIRD_PRICE = 95.00;

	private static final double C_INITIAL_PRICE = 80.00;
	private static final double C_SECOND_PRICE = 105.00;
	private static final double C_THIRD_PRICE = 90.00;

	@Autowired
	private AlgoTradeService algoTradeService;

	@Autowired
	private AccountRepository accountRepository;

	@Before
	public void initTestData(){
		algoTradeService.setStockPriceMap(new LinkedHashMap<String, List<Tick>>(){{
			put("2018-01-02", new ArrayList<Tick>(){{
				add(new Tick(Ticker.A, A_INITIAL_PRICE, 0.0, 0.0));
				add(new Tick(Ticker.D, D_INITIAL_PRICE, 0.0, 0.0));
				add(new Tick(Ticker.B, B_INITIAL_PRICE, 0.0, 0.0));
				add(new Tick(Ticker.C, C_INITIAL_PRICE, 0.0, 0.0));
			}});

			put("2018-01-03", new ArrayList<Tick>(){{
				add(new Tick(Ticker.A, A_SECOND_PRICE, 0.0, 0.0));
				add(new Tick(Ticker.D, D_SECOND_PRICE, 0.0, 0.0));
				add(new Tick(Ticker.B, B_SECOND_PRICE, 0.0, 0.0));
				add(new Tick(Ticker.C, C_SECOND_PRICE, 0.0, 0.0));
			}});

			put("2018-01-04", new ArrayList<Tick>(){{
				add(new Tick(Ticker.A, A_THIRD_PRICE, 0.0, 0.0));
				add(new Tick(Ticker.D, D_THIRD_PRICE, 0.0, 0.0));
				add(new Tick(Ticker.B, B_THIRD_PRICE, 0.0, 0.0));
				add(new Tick(Ticker.C, C_THIRD_PRICE, 0.0, 0.0));
			}});
		}});
	}

	/*====================================================================================================*/
	/*                                    General unit tests                                              */
	/*====================================================================================================*/

	@Test
	public void test_general_001_initialPortfolioAllocation() {
		Account account = prepareAccountWithEmptyPortfolio();

		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());
		printPortfolio(account.getPortfolio());

		Assert.assertTrue(account.getPortfolio().getCashBalance() >= 30000.0);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				double assetAllocation = A_INITIAL_ALLOCATION_PERCENT * (INITIAL_CASH_BALANCE/100.0);
				Assert.assertTrue(product.getValue() <= assetAllocation);
				Assert.assertEquals(1, product.getTransactions().size());
				Assert.assertEquals(product.getPosition(), (int)(assetAllocation / A_INITIAL_PRICE));
			}

			if(Ticker.D.equals(product.getTicker())){
				double assetAllocation = D_INITIAL_ALLOCATION_PERCENT * (INITIAL_CASH_BALANCE/100.0);
				Assert.assertTrue(product.getValue() <= assetAllocation);
				Assert.assertEquals(1, product.getTransactions().size());
				Assert.assertEquals(product.getPosition(), (int)(assetAllocation / D_INITIAL_PRICE));
			}

			if(Ticker.B.equals(product.getTicker())){
				double assetAllocation = B_INITIAL_ALLOCATION_PERCENT * (INITIAL_CASH_BALANCE/100.0);
				Assert.assertTrue(product.getValue() <= assetAllocation);
				Assert.assertEquals(1, product.getTransactions().size());
				Assert.assertEquals(product.getPosition(), (int)(assetAllocation / B_INITIAL_PRICE));
			}

			if(Ticker.C.equals(product.getTicker())){
				double assetAllocation = C_INITIAL_ALLOCATION_PERCENT * (INITIAL_CASH_BALANCE/100.0);
				Assert.assertTrue(product.getValue() <= assetAllocation);
				Assert.assertEquals(1, product.getTransactions().size());
				Assert.assertEquals(product.getPosition(), (int)(assetAllocation / C_INITIAL_PRICE));
			}
		}
	}

	@Test
	public void test_general_002_productUpdateAfterTrade() {
		Account account = prepareAccountWithEmptyPortfolio();

		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());
		algoTradeService.trade(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(A_THIRD_PRICE, product.getUnitValue(), 0.001);
			}

			if(Ticker.D.equals(product.getTicker())){
				Assert.assertEquals(D_THIRD_PRICE, product.getUnitValue(), 0.001);
			}

			if(Ticker.B.equals(product.getTicker())){
				Assert.assertEquals(B_THIRD_PRICE, product.getUnitValue(), 0.001);
			}

			if(Ticker.C.equals(product.getTicker())){
				Assert.assertEquals(C_THIRD_PRICE, product.getUnitValue(), 0.001);
			}
		}
	}

	/*====================================================================================================*/
	/*                                    Stop Loss strategy unit tests                                   */
	/*====================================================================================================*/

	@Test
	public void test_stopLoss_001_staticEntirePositionSellTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.ENTIRE_POSITION);
		strategyUnit.setTriggerPrice(55.00);

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(0, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 sell
			}
		}
	}

	@Test
	public void test_stopLoss_002_staticEntirePositionSellNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.ENTIRE_POSITION);
		strategyUnit.setTriggerPrice(30.00);

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertNotEquals(0, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size());
			}
		}
	}

	@Test
	public void test_stopLoss_003_staticPositionMapSellTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.POSITION_MAP);

		strategyUnit.setTriggerPriceMap(new HashMap<Double, Double>(){{
			put(48.0, 40.0);
			put(45.0, 50.0); //This should be triggered, as the prices drops to 40.0
			put(39.0, 75.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition/2, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 sell
			}
		}
	}

	@Test
	public void test_stopLoss_004_staticPositionMapSellNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.POSITION_MAP);

		strategyUnit.setTriggerPriceMap(new HashMap<Double, Double>(){{
			put(38.0, 40.0);
			put(35.0, 50.0);
			put(30.0, 75.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy + 1 sell
			}
		}
	}

	@Test
	public void test_stopLoss_005_staticPositionMapSellTwiceTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.POSITION_MAP);

		strategyUnit.setTriggerPriceMap(new HashMap<Double, Double>(){{
			put(55.0, 10.0); //This should be triggered on 2.1.2018, as the price drops to 50.0, and 3.1. as well, as the price drops to 40.0
			put(48.0, 40.0);
			put(45.0, 50.0);
			put(39.0, 75.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				int expectedPostTrade = preTradePosition - (int)(0.1*preTradePosition); //First sell
				expectedPostTrade = expectedPostTrade - (int)(0.1*expectedPostTrade); //Second sell
				Assert.assertEquals(expectedPostTrade, product.getPosition());
				Assert.assertEquals(3, product.getTransactions().size()); //Initial buy + 2 sell ops
			}
		}
	}

	@Test
	public void test_stopLoss_006_percentEntirePositionSellTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.ENTIRE_POSITION);
		strategyUnit.setTriggerPercentage(20.00);	//Sell, if price drops 20% from current level

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(0, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 sell
			}
		}
	}

	@Test
	public void test_stopLoss_007_percentEntirePositionSellNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.ENTIRE_POSITION);
		strategyUnit.setTriggerPercentage(40.00);	//Sell, if price drops 40% from current level

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy
			}
		}
	}

	@Test
	public void test_stopLoss_008_percentPositionMapSellTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.POSITION_MAP);

		strategyUnit.setTriggerPercentageMap(new HashMap<Double, Double>(){{
			put(25.0, 40.0);
			put(30.0, 50.0);  //This should be triggered, as 3.1.2018 the price drop is 33%
			put(35.0, 75.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition/2, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 sell
			}
		}
	}

	@Test
	public void test_stopLoss_009_percentPositionMapSellNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.POSITION_MAP);

		strategyUnit.setTriggerPercentageMap(new HashMap<Double, Double>(){{
			put(35.0, 40.0);
			put(40.0, 50.0);
			put(45.0, 75.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy only
			}
		}
	}

	@Test
	public void test_stopLoss_010_percentPositionMapSellTriggeredTwice() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitStopLoss strategyUnit = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitStopLoss.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitStopLoss.PositionSellType.POSITION_MAP);

		strategyUnit.setTriggerPercentageMap(new HashMap<Double, Double>(){{
			put(15.0, 10.0);  //This should be triggered twice, as 2.1.2018 the price drop is 16%, and 3.1 it drops 20%
			put(25.0, 30.0);
			put(30.0, 40.0);
			put(35.0, 60.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				int expectedPostTrade = preTradePosition - (int)(0.1 * preTradePosition); //First sell
				expectedPostTrade = expectedPostTrade - (int)(0.1 * expectedPostTrade); //Second sell
				Assert.assertEquals(expectedPostTrade, product.getPosition());
				Assert.assertEquals(3, product.getTransactions().size()); //Initial buy + 2 sell
			}
		}
	}

	/*====================================================================================================*/
	/*                                  Take Profit strategy unit tests                                   */
	/*====================================================================================================*/
	@Test
	public void test_takeProfit_001_staticEntirePositionSellTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.ENTIRE_POSITION);
		strategyUnit.setTriggerPrice(25.00);

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				Assert.assertEquals(0, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 sell
			}
		}
	}

	@Test
	public void test_takeProfit_002_staticEntirePositionSellNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.ENTIRE_POSITION);
		strategyUnit.setTriggerPrice(35.00);

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.D, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy
			}
		}
	}

	@Test
	public void test_takeProfit_003_staticPositionMapSellTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP);
		strategyUnit.setTriggerPriceMap(new HashMap<Double, Double>(){{
			put(25.0, 20.0);
			put(27.0, 30.0); //This should be triggered
			put(31.0, 40.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.D, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		//Check, if the price map was updated after the sell
		Assert.assertEquals(20.0, strategyUnit.getTriggerPriceMap().get(32.0), 0.001);
		Assert.assertEquals(30.0, strategyUnit.getTriggerPriceMap().get(34.0), 0.001);
		Assert.assertEquals(40.0, strategyUnit.getTriggerPriceMap().get(38.0), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				int expectedPostTrade = preTradePosition - (int)(0.3 * preTradePosition);
				Assert.assertEquals(expectedPostTrade, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + sell
			}
		}
	}

	@Test
	public void test_takeProfit_004_staticPositionMapSellNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP);
		strategyUnit.setTriggerPriceMap(new HashMap<Double, Double>(){{
			put(35.0, 50.0);
			put(37.0, 60.0);
			put(41.0, 70.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.D, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		//Check, if the price map was updated after the sell - it CANNOT BE, as there was no sell
		Assert.assertEquals(50.0, strategyUnit.getTriggerPriceMap().get(35.0), 0.001);
		Assert.assertEquals(60.0, strategyUnit.getTriggerPriceMap().get(37.0), 0.001);
		Assert.assertEquals(70.0, strategyUnit.getTriggerPriceMap().get(41.0), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy
			}
		}

	}

	@Test
	public void test_takeProfit_005_staticPositionMapSellTriggeredTwice() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.STATIC);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP);
		strategyUnit.setTriggerPriceMap(new HashMap<Double, Double>(){{
			put(22.0, 10.0); //This should be triggered on 1st & 2nd run
			put(31.0, 35.0);
			put(35.0, 45.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.D, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		//Check, if the price map was updated after the sell
		Assert.assertEquals(10.0, strategyUnit.getTriggerPriceMap().get(32.0), 0.001);
		Assert.assertEquals(35.0, strategyUnit.getTriggerPriceMap().get(41.0), 0.001);
		Assert.assertEquals(45.0, strategyUnit.getTriggerPriceMap().get(45.0), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				int expectedPostTrade = preTradePosition - (int)(0.1 * preTradePosition);
				expectedPostTrade = expectedPostTrade - (int)(0.1 * expectedPostTrade);
				Assert.assertEquals(expectedPostTrade, product.getPosition());
				Assert.assertEquals(3, product.getTransactions().size()); //Initial buy + 2 sells
			}
		}
	}

	@Test
	public void test_takeProfit_006_percentEntirePositionSellTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.ENTIRE_POSITION);
		strategyUnit.setTriggerPercentage(20.00);	//Sell everything, when price hikes 20%

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				Assert.assertEquals(0, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 sell
			}
		}
	}

	@Test
	public void test_takeProfit_007_percentEntirePositionSellNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.ENTIRE_POSITION);
		strategyUnit.setTriggerPercentage(55.00); 	//Sell everything, when price hikes 55%

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.D, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy
			}
		}
	}

	@Test
	public void test_takeProfit_008_percentPositionMapSellTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP);
		strategyUnit.setTriggerPercentageMap(new HashMap<Double, Double>(){{
			put(30.0, 50.0); //This should be triggered
			put(55.0, 65.0);
			put(65.0, 80.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.D, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		//Check, if the price map was updated after the sell
		Assert.assertEquals(27.0, strategyUnit.getLastValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				int expectedPostTrade = preTradePosition - (int)(0.5 * preTradePosition);
				Assert.assertEquals(expectedPostTrade, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + sell
			}
		}
	}

	@Test
	public void test_takeProfit_009_percentPositionMapSellNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP);
		strategyUnit.setTriggerPercentageMap(new HashMap<Double, Double>(){{
			put(55.0, 60.0);
			put(65.0, 75.0);
			put(75.0, 90.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.D, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		//Check, if the price map was updated after the sell
		Assert.assertEquals(20.0, strategyUnit.getLastValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy
			}
		}
	}

	@Test
	public void test_takeProfit_010_percentPositionMapSellTriggeredTwice() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitTakeProfit strategyUnit = new StrategyUnitTakeProfit(Ticker.D, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitTakeProfit.ValueType.PERCENT);
		strategyUnit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP);
		strategyUnit.setTriggerPercentageMap(new HashMap<Double, Double>(){{
			put(10.0, 10.0); //This should be triggered on 2nd run
			put(30.0, 60.0); //This should be triggered on 1nd run
			put(55.0, 80.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.D, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		//Check, if the price map was updated after the sell
		Assert.assertEquals(30.0, strategyUnit.getLastValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.D.equals(product.getTicker())){
				int expectedPostTrade = preTradePosition - (int)(0.6 * preTradePosition);
				expectedPostTrade = expectedPostTrade - (int)(0.1 * expectedPostTrade);
				Assert.assertEquals(expectedPostTrade, product.getPosition());
				Assert.assertEquals(3, product.getTransactions().size()); //Initial buy + 2 sells
			}
		}
	}



	/*====================================================================================================*/
	/*                                  Buy strategy unit tests                                           */
	/*====================================================================================================*/

	@Test
	public void test_buy_001_staticEntireCashBalanceBuyTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnit.setTriggerPrice(45.00);  //When price drops to 45/share, buy with all free cash

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(40.0, strategyUnit.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertTrue(account.getPortfolio().getCashBalance() <= 40.0);
				Assert.assertTrue(preTradePosition < product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	@Test
	public void test_buy_002_staticEntireCashBalanceBuyNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnit.setTriggerPrice(39.00);  //When price drops to 39/share, buy with all free cash

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(60.0, strategyUnit.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy
			}
		}
	}

	@Test
	public void test_buy_003_staticPositionMapBuyTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP);
	    strategyUnit.setTriggerPriceMap(new LinkedHashMap<Double, Double>(){{
	    	put(45.0, 50.0);
			put(42.0, 70.0); //This should be triggered - when price drops to 42/share, use 70% of funds to buy
			put(38.0, 80.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		double preTradeCashBalance = account.getPortfolio().getCashBalance();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(40.0, strategyUnit.getReferenceValue(), 0.001);
		Assert.assertEquals(50.0, strategyUnit.getTriggerPriceMap().get(25.0), 0.001);
		Assert.assertEquals(70.0, strategyUnit.getTriggerPriceMap().get(22.0), 0.001);
		Assert.assertEquals(80.0, strategyUnit.getTriggerPriceMap().get(18.0), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				int postTradePosition = preTradePosition + (int)((0.7 * preTradeCashBalance)/strategyUnit.getReferenceValue());
				Assert.assertTrue(account.getPortfolio().getCashBalance() >= 40.0);
				Assert.assertEquals(postTradePosition, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	@Test
	public void test_buy_004_staticPositionMapBuyNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP);
		strategyUnit.setTriggerPriceMap(new LinkedHashMap<Double, Double>(){{
			put(38.0, 70.0);
			put(35.0, 80.0);
			put(32.0, 90.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		double preTradeCashBalance = account.getPortfolio().getCashBalance();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(60.0, strategyUnit.getReferenceValue(), 0.001);
		Assert.assertEquals(70.0, strategyUnit.getTriggerPriceMap().get(38.0), 0.001);
		Assert.assertEquals(80.0, strategyUnit.getTriggerPriceMap().get(35.0), 0.001);
		Assert.assertEquals(90.0, strategyUnit.getTriggerPriceMap().get(32.0), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(preTradeCashBalance, account.getPortfolio().getCashBalance(), 0.001);
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy
			}
		}
	}

	@Test
	public void test_buy_005_staticPositionMapBuyTriggeredTwice() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP);
		strategyUnit.setTriggerPriceMap(new LinkedHashMap<Double, Double>(){{
			put(55.0, 25.0); //This should be triggered 2 times - when price drops to 55/share, use 25% of funds to buy
			put(45.0, 40.0);
			put(35.0, 60.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(40.0, strategyUnit.getReferenceValue(), 0.001);
		Assert.assertEquals(25.0, strategyUnit.getTriggerPriceMap().get(35.0), 0.001);
		Assert.assertEquals(40.0, strategyUnit.getTriggerPriceMap().get(25.0), 0.001);
		Assert.assertEquals(60.0, strategyUnit.getTriggerPriceMap().get(15.0), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertTrue(account.getPortfolio().getCashBalance() >= 40.0);
				Assert.assertTrue(product.getPosition() > preTradePosition);
				Assert.assertEquals(3, product.getTransactions().size()); //Initial buy + 2 more buys
			}
		}
	}

	@Test
	public void test_buy_006_percentEntirePositionBuyTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.PERCENT);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnit.setTriggerPercentage(15.00);  //When price drops 15%, buy with all free cash

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(50.0, strategyUnit.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertTrue(account.getPortfolio().getCashBalance() <= 50.0);
				Assert.assertTrue(preTradePosition < product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	@Test
	public void test_buy_007_percentEntirePositionBuyNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.PERCENT);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnit.setTriggerPercentage(35.00);  //When price drops 35%, buy with all free cash

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		double preTradeCashBalance = account.getPortfolio().getCashBalance();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(60.0, strategyUnit.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(preTradeCashBalance, account.getPortfolio().getCashBalance(), 0.001);
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy
			}
		}
	}

	@Test
	public void test_buy_008_percentPositionMapBuyTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.PERCENT);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP);
		strategyUnit.setTriggerPercentageMap(new LinkedHashMap<Double, Double>(){{
			put(25.0, 30.0);
			put(30.0, 50.0); //This should be triggered - when price drops 30%, use 50% of funds to buy
			put(35.0, 70.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		double preTradeCashBalance = account.getPortfolio().getCashBalance();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(40.0, strategyUnit.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				int postTradePosition = preTradePosition + (int)((0.5 * preTradeCashBalance)/strategyUnit.getReferenceValue());
				Assert.assertTrue(account.getPortfolio().getCashBalance() >= 40.0);
				Assert.assertEquals(postTradePosition, product.getPosition());
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	@Test
	public void test_buy_009_percentPositionMapBuyNotTriggered() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.PERCENT);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP);
		strategyUnit.setTriggerPercentageMap(new LinkedHashMap<Double, Double>(){{
			put(35.0, 50.0);
			put(40.0, 70.0);
			put(45.0, 90.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		double preTradeCashBalance = account.getPortfolio().getCashBalance();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(60.0, strategyUnit.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(preTradeCashBalance, account.getPortfolio().getCashBalance(), 0.001);
				Assert.assertEquals(preTradePosition, product.getPosition());
				Assert.assertEquals(1, product.getTransactions().size()); //Initial buy
			}
		}
	}

	@Test
	public void test_buy_010_percentPositionMapBuyTriggeredTwice() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnit = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnit.setValueType(StrategyUnitBuy.ValueType.PERCENT);
		strategyUnit.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ALLOCATION_MAP);
		strategyUnit.setTriggerPercentageMap(new LinkedHashMap<Double, Double>(){{
			put(15.0, 30.0); //This should be triggered bot times
			put(25.0, 50.0);
			put(35.0, 70.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnit);
		account.getPortfolio().setStrategy(strategy, "");

		int preTradePosition = getProductFromPortfolio(Ticker.A, account.getPortfolio()).getPosition();
		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(40.0, strategyUnit.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertTrue(account.getPortfolio().getCashBalance() >= 40.0);
				Assert.assertTrue(product.getPosition() > preTradePosition);
				Assert.assertEquals(3, product.getTransactions().size()); //Initial buy + 2 more buys
			}
		}
	}

	@Test
	public void test_buy_011_updateStopLossPercentAfterBuy() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnitBuy = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitBuy.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnitBuy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnitBuy.setTriggerPrice(45.00);  //When price drops to 45/share, buy with all free cash

		StrategyUnitStopLoss strategyUnitStopLoss = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitStopLoss.setValueType(StrategyUnitStopLoss.ValueType.PERCENT);
		strategyUnitStopLoss.setPositionSellType(StrategyUnitStopLoss.PositionSellType.ENTIRE_POSITION);
		strategyUnitStopLoss.setTriggerPercentage(40.0);

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnitBuy);
		strategy.getUnits().add(strategyUnitStopLoss);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(40.0, strategyUnitStopLoss.getLastValue(), 0.001);
		Assert.assertEquals(40.0, strategyUnitBuy.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	@Test
	public void test_buy_012_updateStopLossStaticEntirePositionAfterBuy() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnitBuy = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitBuy.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnitBuy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnitBuy.setTriggerPrice(45.00);  //When price drops to 45/share, buy with all free cash

		StrategyUnitStopLoss strategyUnitStopLoss = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitStopLoss.setValueType(StrategyUnitStopLoss.ValueType.STATIC);
		strategyUnitStopLoss.setPositionSellType(StrategyUnitStopLoss.PositionSellType.ENTIRE_POSITION);
		strategyUnitStopLoss.setTriggerPrice(39.0);	//When price drops to 39/share, sell everything

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnitBuy);
		strategy.getUnits().add(strategyUnitStopLoss);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(19.0, strategyUnitStopLoss.getTriggerPrice(), 0.001);
		Assert.assertEquals(40.0, strategyUnitBuy.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	@Test
	public void test_buy_013_updateStopLossStaticPositionMapAfterBuy() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnitBuy = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitBuy.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnitBuy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnitBuy.setTriggerPrice(45.00);  //When price drops to 45/share, buy with all free cash

		StrategyUnitStopLoss strategyUnitStopLoss = new StrategyUnitStopLoss(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitStopLoss.setValueType(StrategyUnitStopLoss.ValueType.STATIC);
		strategyUnitStopLoss.setPositionSellType(StrategyUnitStopLoss.PositionSellType.POSITION_MAP);
		strategyUnitStopLoss.setTriggerPriceMap(new HashMap<Double, Double>(){{
			put(39.0, 30.0);
			put(35.0, 50.0);
			put(31.0, 70.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnitBuy);
		strategy.getUnits().add(strategyUnitStopLoss);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(30.0, strategyUnitStopLoss.getTriggerPriceMap().get(19.0), 0.001);
		Assert.assertEquals(50.0, strategyUnitStopLoss.getTriggerPriceMap().get(15.0), 0.001);
		Assert.assertEquals(70.0, strategyUnitStopLoss.getTriggerPriceMap().get(11.0), 0.001);
		Assert.assertEquals(40.0, strategyUnitBuy.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	@Test
	public void test_buy_014_updateTakeProfitPercentAfterBuy() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnitBuy = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitBuy.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnitBuy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnitBuy.setTriggerPrice(45.00);  //When price drops to 45/share, buy with all free cash

		StrategyUnitTakeProfit strategyUnitTakeProfit = new StrategyUnitTakeProfit(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitTakeProfit.setValueType(StrategyUnitTakeProfit.ValueType.PERCENT);
		strategyUnitTakeProfit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.ENTIRE_POSITION);
		strategyUnitTakeProfit.setTriggerPercentage(40.0);

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnitBuy);
		strategy.getUnits().add(strategyUnitTakeProfit);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(40.0, strategyUnitTakeProfit.getLastValue(), 0.001);
		Assert.assertEquals(40.0, strategyUnitBuy.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	@Test
	public void test_buy_015_updateTakeProfitStaticEntirePositionAfterBuy() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnitBuy = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitBuy.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnitBuy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnitBuy.setTriggerPrice(45.00);  //When price drops to 45/share, buy with all free cash

		StrategyUnitTakeProfit strategyUnitTakeProfit = new StrategyUnitTakeProfit(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitTakeProfit.setValueType(StrategyUnitTakeProfit.ValueType.STATIC);
		strategyUnitTakeProfit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.ENTIRE_POSITION);
		strategyUnitTakeProfit.setTriggerPrice(69.0);	//When price hikes to 69/share, sell everything

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnitBuy);
		strategy.getUnits().add(strategyUnitTakeProfit);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(49.0, strategyUnitTakeProfit.getTriggerPrice(), 0.001);
		Assert.assertEquals(40.0, strategyUnitBuy.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	@Test
	public void test_buy_016_updateTakeProfitStaticPositionMapAfterBuy() {
		Account account = prepareAccountWithEmptyPortfolio();
		algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

		StrategyUnitBuy strategyUnitBuy = new StrategyUnitBuy(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitBuy.setValueType(StrategyUnitBuy.ValueType.STATIC);
		strategyUnitBuy.setFundsAllocationType(StrategyUnitBuy.FundsAllocationType.ENTIRE_CASH_BALANCE);
		strategyUnitBuy.setTriggerPrice(45.00);  //When price drops to 45/share, buy with all free cash

		StrategyUnitTakeProfit strategyUnitTakeProfit = new StrategyUnitTakeProfit(Ticker.A, Indicator.CURRENT_PRICE);
		strategyUnitTakeProfit.setValueType(StrategyUnitTakeProfit.ValueType.STATIC);
		strategyUnitTakeProfit.setPositionSellType(StrategyUnitTakeProfit.PositionSellType.POSITION_MAP);
		strategyUnitTakeProfit.setTriggerPriceMap(new HashMap<Double, Double>(){{
			put(69.0, 40.0);
			put(75.0, 60.0);
			put(80.0, 80.0);
		}});

		Strategy strategy = new Strategy();
		strategy.getUnits().add(strategyUnitBuy);
		strategy.getUnits().add(strategyUnitTakeProfit);
		account.getPortfolio().setStrategy(strategy, "");

		algoTradeService.trade(account.getPortfolio());
		//printPortfolio(account.getPortfolio());

		Assert.assertEquals(40.0, strategyUnitTakeProfit.getTriggerPriceMap().get(49.0), 0.001);
		Assert.assertEquals(60.0, strategyUnitTakeProfit.getTriggerPriceMap().get(55.0), 0.001);
		Assert.assertEquals(80.0, strategyUnitTakeProfit.getTriggerPriceMap().get(60.0), 0.001);
		Assert.assertEquals(40.0, strategyUnitBuy.getReferenceValue(), 0.001);
		for(Product product: account.getPortfolio().getProducts()){
			if(Ticker.A.equals(product.getTicker())){
				Assert.assertEquals(2, product.getTransactions().size()); //Initial buy + 1 more buy
			}
		}
	}

	/*========================================*/
	/*======== Utility Helper Methods ========*/
	/*========================================*/
	private Account prepareAccountWithEmptyPortfolio(){
		Account account = new Account();

		Portfolio portfolio = new Portfolio();
		account.setPortfolio(portfolio);
		portfolio.setCurrency("$");
		portfolio.setCashBalance(INITIAL_CASH_BALANCE);
		portfolio.setInitialAllocation(new HashMap<Ticker, Double>(){{
			put(Ticker.A, A_INITIAL_ALLOCATION_PERCENT);
			put(Ticker.D, D_INITIAL_ALLOCATION_PERCENT);
			put(Ticker.B, B_INITIAL_ALLOCATION_PERCENT);
			put(Ticker.C, C_INITIAL_ALLOCATION_PERCENT);
		}});

		accountRepository.add(account);
		return account;
	}

	private void printPortfolio(Portfolio portfolio){
		System.out.println("========== Portfolio: ==========");
		System.out.println("Cash balance: " + FormatterUtil.formatDouble(portfolio.getCashBalance()));
		System.out.println("Products in portfolio:");
		portfolio.getProducts().forEach(product ->
				System.out.println(
						product.getTicker().getSymbol() + ": " + product.getPosition() +
								" at " + product.getUnitValue() + " per share. Total: " +
								FormatterUtil.formatDouble(product.getValue())
				));
	}

	private Product getProductFromPortfolio(Ticker ticker, Portfolio portfolio){
		for(Product product: portfolio.getProducts()){
			if(ticker.equals(product.getTicker())){
				return product;
			}
		}

		return null;
	}
}
