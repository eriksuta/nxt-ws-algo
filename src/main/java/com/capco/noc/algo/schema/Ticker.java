package com.capco.noc.algo.schema;

public enum Ticker {

    D("D", "D_2003-2013.csv", "D_2003-2018.csv"),
    A("A", "A_2003-2013.csv", "A_2003-2018.csv"),
    B("B", "B_2003-2013.csv", "B_2003-2018.csv"),
    C("C", "C_2003-2013.csv", "C_2003-2018.csv");

    private String symbol;
    private String backTestFilePath;
    private String realDataFilePath;

    Ticker(String symbol, String backTestFilePath, String realDataFilePath) {
        this.symbol = symbol;
        this.backTestFilePath = backTestFilePath;
        this.realDataFilePath = realDataFilePath;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getBackTestFilePath() {
        return backTestFilePath;
    }

    public String getRealDataFilePath() {
        return realDataFilePath;
    }
}
