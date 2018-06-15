package com.capco.noc.algo;

import com.capco.noc.algo.engine.AlgoTradeService;
import com.capco.noc.algo.repository.AccountRepository;
import com.capco.noc.algo.schema.Account;
import com.capco.noc.algo.schema.Portfolio;
import com.capco.noc.algo.util.FormatterUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
public class AlgoTraderApplication {

	//Constants
	public static final String ACCOUNT_ID = UUID.randomUUID().toString();
	private static final String USER_NAME = "johndoe";
	private static final String PASSWORD = "12345";
	public static final Double INITIAL_CASH_BALANCE = 250000.00;

	private AccountRepository accountRepository;

	public static void main(String[] args) {
		SpringApplication.run(AlgoTraderApplication.class, args);
    }

	/*
	* Initialise embedded H2 database with some initial data
	* */
	@Bean
	CommandLineRunner loadData(AlgoTradeService algoTradeService, AccountRepository accountRepository) {
		this.accountRepository = accountRepository;

		return (args) -> {
			//Init the account
			initAccount();
			//prepareFiles();
			//fixFiles();
		};
	}

	private void initAccount(){
		//Initialize the dummy account
		Account account = new Account();
		account.setId(ACCOUNT_ID);
		account.setUsername(USER_NAME);
		account.setPassword(PASSWORD);

		//Initialize the portfolio
		Portfolio portfolio = new Portfolio();
		account.setPortfolio(portfolio);
		portfolio.setCurrency("$");
		portfolio.setCashBalance(INITIAL_CASH_BALANCE);

		accountRepository.add(account);
	}

	/*==================================================*/
	/* Helper Methods                                   */
	/*==================================================*/
	private static void printPortfolio(Portfolio portfolio){
		AtomicReference<Double> nlv = new AtomicReference<>(portfolio.getCashBalance());

		System.out.println("========== Portfolio: ==========");
		System.out.println("Cash balance: " + FormatterUtil.formatDouble(portfolio.getCashBalance()));
		System.out.println("Products in portfolio:");
		portfolio.getProducts().forEach(product -> {
				System.out.println(
						product.getTicker().getSymbol() + ": " + product.getPosition() +
						" at " + product.getUnitValue() + " per share. Total: " +
						FormatterUtil.formatDouble(product.getValue())
				);

				nlv.updateAndGet(v -> ((v + product.getValue())));
		});
		System.out.println("Net Liquidation Value: " + FormatterUtil.formatDouble(nlv.get()));
	}

	/*
	private static void prepareFiles(){
		String[] files = {"IBM_1.1.2002-30.5.2018.csv", "MSFT_1.1.2002-30.5.2018.csv", "T_1.1.2002-30.5.2018.csv", "JPM_1.1.2002-30.5.2018.csv"};

		for(String fileName: files){
			double[] last30 = new double[30];
			double[] last200 = new double[200];
			File inputFile = new File(AlgoTraderApplication.class.getClassLoader().getResource("real/" + fileName).getFile());
			Path outputFile = Paths.get("out-" + fileName);

			try (Scanner scanner = new Scanner(inputFile)) {

				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					StringBuilder newLine = new StringBuilder();

					if(line.startsWith("Date")){
						Files.write(outputFile, ("Date,Close,Avg30,Avg200" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
						continue;
					}

					String[] fields = line.split(",");
					String date = fields[0];
					String close = fields[4];
					updateAvgArrays(last30, last200, close);
					String avg_30 = prepareAvg(last30);
					String avg_200 = prepareAvg(last200);
					newLine.append(date).append(",")
							.append(new BigDecimal(Double.parseDouble(close)).setScale(3, BigDecimal.ROUND_HALF_UP)).append(",")
							.append(avg_30).append(",")
							.append(avg_200).append(System.lineSeparator());
					Files.write(outputFile, newLine.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
				}

				scanner.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void fixFiles(){
		String[] backtestFiles = {"A_IBM_2003-2013.csv", "B_MSFT_2003-2013.csv", "C_T_2003-2013.csv", "D_JPM_2003-2013.csv"};
		String[] realFiles = {"A_IBM_2003-2018.csv", "B_MSFT_2003-2018.csv", "C_T_2003-2018.csv", "D_JPM_2003-2018.csv"};

		for(String fileName: backtestFiles){
			File inputFile = new File(AlgoTraderApplication.class.getClassLoader().getResource("backtest/" + fileName).getFile());
			Path outputFile = Paths.get("out-" + fileName);

			try (Scanner scanner = new Scanner(inputFile)) {

				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					StringBuilder newLine = new StringBuilder(line);

					if(line.startsWith("Date")){
						Files.write(outputFile, ("Date,Close,Avg30,Avg200" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
						continue;
					}

					int firstIndex = newLine.indexOf(".");
					int secondIndex = newLine.indexOf(".", ++firstIndex);
					newLine.setCharAt(secondIndex, ',');
					newLine.append(System.lineSeparator());

					Files.write(outputFile, newLine.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
				}

				scanner.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void updateAvgArrays(double[] array30, double[] array200, String current){
		Double currentPrice = Double.parseDouble(current);

		//Update array of last 30 values
		int lastEmptyIndex = -1;
		for(int i = 0; i < array30.length; i++){
			if(array30[i] == 0){
				lastEmptyIndex = i;
			}
		}

		if(lastEmptyIndex == -1){
			for(int i = 1; i < array30.length; i++){
				array30[i - 1] = array30[i];
			}

			array30[array30.length-1] = currentPrice;
		} else {
			array30[lastEmptyIndex] = currentPrice;
		}

		//Update array of last 200 values
		lastEmptyIndex = -1;
		for(int i = 0; i < array200.length; i++){
			if(array200[i] == 0){
				lastEmptyIndex = i;
			}
		}

		if(lastEmptyIndex == -1){
			for(int i = 1; i < array200.length; i++){
				array200[i - 1] = array200[i];
			}

			array200[array200.length-1] = currentPrice;
		} else {
			array200[lastEmptyIndex] = currentPrice;
		}
	}

	private static String prepareAvg(double[] array){
		for(int i = 0; i < array.length; i++){
			if(array[i] == 0){
				return Double.toString(0.0);
			}
		}

		double avg;
		double sum = 0.0;
		for(int i = 0;i < array.length; i++){
			sum += array[i];
		}

		avg = sum/array.length;

		return BigDecimal.valueOf(avg).setScale(3, BigDecimal.ROUND_HALF_UP).toString();
	}
	*/
}
