package com.capco.noc.algo.schema;

public class Tick {

    private Ticker ticker;
    private double value;
    private double avg30;
    private double avg200;

    public Tick() {}

    public Tick(Ticker ticker, double value, double avg30, double avg200) {
        this.ticker = ticker;
        this.value = value;
        this.avg30 = avg30;
        this.avg200 = avg200;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

    public double getAvg30() {
        return avg30;
    }

    public void setAvg30(double avg30) {
        this.avg30 = avg30;
    }

    public double getAvg200() {
        return avg200;
    }

    public void setAvg200(double avg200) {
        this.avg200 = avg200;
    }
}
