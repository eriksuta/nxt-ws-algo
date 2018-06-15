package com.capco.noc.algo.engine;

import com.capco.noc.algo.repository.AccountRepository;
import com.capco.noc.algo.schema.Portfolio;
import com.capco.noc.algo.schema.Product;
import com.capco.noc.algo.schema.Ticker;
import com.capco.noc.algo.schema.Transaction;
import com.capco.noc.algo.util.FormatterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BrokerServiceImpl implements BrokerService {

    @Autowired
    private AccountRepository accountService;

    public void buy(Portfolio portfolio, Ticker ticker, int position, double unitPrice, String date){
        if(position <= 0){
            return;
        }

        double transactionValue = ((double)position)*unitPrice;

        Product product = getProductFromPortfolio(portfolio, ticker);

        product.setPosition(product.getPosition() + position);
        product.setUnitValue(unitPrice);
        product.setValue(unitPrice * product.getPosition());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setTicker(ticker);
        transaction.setPosition(position);
        transaction.setUnitPrice(unitPrice);
        transaction.setValue(transactionValue);
        transaction.setDate(date);
        transaction.setType(Transaction.Type.BUY);
        product.getTransactions().add(transaction);

        System.out.println("BUY: " + date + " - " + position + " of " + ticker.getSymbol() + " at "
                + unitPrice + " per share. Total: " + FormatterUtil.formatDouble(transactionValue));

        //Update portfolio cash balance
        portfolio.setCashBalance(portfolio.getCashBalance() - transactionValue);
    }

    public void sell(Portfolio portfolio, Ticker ticker, int position, double unitPrice, String date){
        if(position <= 0){
            return;
        }

        double transactionValue = ((double)position)*unitPrice;

        Product product = getProductFromPortfolio(portfolio, ticker);

        product.setPosition(product.getPosition() - position);
        product.setUnitValue(unitPrice);
        product.setValue(unitPrice * product.getPosition());

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setTicker(ticker);
        transaction.setPosition(position);
        transaction.setUnitPrice(unitPrice);
        transaction.setValue(transactionValue);
        transaction.setDate(date);
        transaction.setType(Transaction.Type.SELL);
        product.getTransactions().add(transaction);

        System.out.println("SELL: " + date + " - " + position + " of " + ticker.getSymbol() + " at "
                + unitPrice + " per share. Total: " + FormatterUtil.formatDouble(transactionValue));

        //Update portfolio cash balance
        portfolio.setCashBalance(portfolio.getCashBalance() + transactionValue);
    }

    private Product getProductFromPortfolio(Portfolio portfolio, Ticker ticker){
        for(Product product: portfolio.getProducts()){
            if(ticker.equals(product.getTicker())){
                return product;
            }
        }

        return null;
    }
}
