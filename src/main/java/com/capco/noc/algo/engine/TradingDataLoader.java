package com.capco.noc.algo.engine;

import com.capco.noc.algo.schema.Tick;
import com.capco.noc.algo.schema.Ticker;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class TradingDataLoader {

    private static final String BACKTEST_FOLDER = "backtest/";
    private static final String REAL_FOLDER = "real/";

    public Map<String, List<Tick>> loadBackTestData(){
        Map<String, List<Tick>> backTestPriceMap = new LinkedHashMap<>();

        for(Ticker ticker: Ticker.values()){
            String fileName = ticker.getBackTestFilePath();
            fillPriceMap(BACKTEST_FOLDER + fileName, ticker, backTestPriceMap);
        }

        return backTestPriceMap;
    }

    public Map<String, List<Tick>> loadRealData(){
        Map<String, List<Tick>> realPriceMap = new LinkedHashMap<>();

        for(Ticker ticker: Ticker.values()){
            String fileName = ticker.getRealDataFilePath();
            fillPriceMap(REAL_FOLDER + fileName, ticker, realPriceMap);
        }

        return realPriceMap;
    }

    private void fillPriceMap(String fileName, Ticker ticker, Map<String, List<Tick>> priceMap){
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.startsWith("Date")){
                    continue;
                }

                String[] fields = line.split(",");
                String date = fields[0];
                double unitPrice = round(Double.parseDouble(fields[1]), 3);
                double avg30 = round(Double.parseDouble(fields[2]), 3);
                double avg200 = round(Double.parseDouble(fields[3]), 3);

                priceMap.computeIfAbsent(date, k -> new ArrayList<>());
                priceMap.get(date).add(new Tick(ticker, unitPrice, avg30, avg200));
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
