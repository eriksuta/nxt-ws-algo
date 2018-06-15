package com.capco.noc.algo.schema;

import java.util.ArrayList;
import java.util.List;

public class Product {

    private String id;
    private Ticker ticker;
    private Tick initialTick;
    private int position;
    private double unitValue;
    private double value;
    private List<Transaction> transactions = new ArrayList<>();

    public Product() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Ticker getTicker() {
        return ticker;
    }

    public void setTicker(Ticker ticker) {
        this.ticker = ticker;
    }

    public Tick getInitialTick() {
        return initialTick;
    }

    public void setInitialTick(Tick initialTick) {
        this.initialTick = initialTick;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public double getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(double unitValue) {
        this.unitValue = unitValue;
        this.value = this.unitValue * this.position;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
