package com.capco.noc.algo.service;

import com.capco.noc.algo.AlgoTraderApplication;
import com.capco.noc.algo.engine.AlgoTradeService;
import com.capco.noc.algo.repository.AccountRepository;
import com.capco.noc.algo.schema.Account;
import com.capco.noc.algo.engine.TradingDataLoader;
import com.capco.noc.algo.schema.Portfolio;
import com.capco.noc.algo.schema.Product;
import com.capco.noc.algo.strategy.StrategyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/algo")
public class AppRestController {

    private final AccountRepository accountRepository;
    private final AlgoTradeService algoTradeService;
    private final TradingDataLoader tradingDataLoader;
    private final StrategyProvider strategyProvider;

    @Autowired
    AppRestController(AccountRepository accountRepository, AlgoTradeService algoTradeService,
                      TradingDataLoader tradingDataLoader, StrategyProvider strategyProvider){

        this.strategyProvider = strategyProvider;
        this.tradingDataLoader = tradingDataLoader;
        this.algoTradeService = algoTradeService;
        this.accountRepository = accountRepository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/account")
    ResponseEntity<Account> getAccount(){
        Account account = accountRepository.get(AlgoTraderApplication.ACCOUNT_ID);
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/backTest")
    ResponseEntity<Account> tradeWithBackTestData(){
        Account account = accountRepository.get(AlgoTraderApplication.ACCOUNT_ID);
        account.getPortfolio().setCashBalance(AlgoTraderApplication.INITIAL_CASH_BALANCE);
        algoTradeService.setStockPriceMap(tradingDataLoader.loadBackTestData());

        StrategyCreator myStrategyCreator = strategyProvider.getStrategyCreator();

        myStrategyCreator.defineInitialPortfolioAllocation(account.getPortfolio());
        algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

        myStrategyCreator.defineAccountStrategy(account.getPortfolio());
        algoTradeService.trade(account.getPortfolio());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/real")
    ResponseEntity<Account> tradeWithRealData(){
        Account account = accountRepository.get(AlgoTraderApplication.ACCOUNT_ID);
        account.getPortfolio().setCashBalance(AlgoTraderApplication.INITIAL_CASH_BALANCE);
        algoTradeService.setStockPriceMap(tradingDataLoader.loadRealData());

        StrategyCreator myStrategyCreator = strategyProvider.getStrategyCreator();

        myStrategyCreator.defineInitialPortfolioAllocation(account.getPortfolio());
        algoTradeService.performInitialPortfolioAllocation(account.getPortfolio());

        myStrategyCreator.defineAccountStrategy(account.getPortfolio());
        algoTradeService.trade(account.getPortfolio());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/strategyEval")
    ResponseEntity<Account> evaluateStrategies(){
        Account account = accountRepository.get(AlgoTraderApplication.ACCOUNT_ID);
        algoTradeService.setStockPriceMap(tradingDataLoader.loadRealData());

        //Trade with all strategies
        List<Portfolio> portfolios = new ArrayList<>();
        for(StrategyCreator strategyCreator: strategyProvider.getAllStrategyCreators()){
            Portfolio portfolio = new Portfolio();
            portfolio.setCashBalance(AlgoTraderApplication.INITIAL_CASH_BALANCE);

            strategyCreator.defineInitialPortfolioAllocation(portfolio);
            algoTradeService.performInitialPortfolioAllocation(portfolio);

            strategyCreator.defineAccountStrategy(portfolio);
            algoTradeService.trade(portfolio);
            portfolios.add(portfolio);
        }

        //Evaluate the best strategy
        account.setPortfolio(getBestPortfolio(portfolios));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Portfolio getBestPortfolio(List<Portfolio> portfolios){
        Portfolio bestPortfolio = null;

        for(Portfolio portfolio: portfolios){
            if(bestPortfolio == null){
                bestPortfolio = portfolio;
            }

            double bestPortfolioNlv = bestPortfolio.getCashBalance();
            for(Product product: bestPortfolio.getProducts()){
                bestPortfolioNlv += product.getValue();
            }

            double currentPortfolioNlv = portfolio.getCashBalance();
            for(Product product: portfolio.getProducts()){
                currentPortfolioNlv += product.getValue();
            }

            if(currentPortfolioNlv > bestPortfolioNlv){
                bestPortfolio = portfolio;
            }
        }

        return bestPortfolio;
    }
}
