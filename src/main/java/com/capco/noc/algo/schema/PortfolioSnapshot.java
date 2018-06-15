package com.capco.noc.algo.schema;

import java.util.ArrayList;
import java.util.List;

public class PortfolioSnapshot {

    private double cashBalance;
    private List<ProductSnapshot> productListSnapshot = new ArrayList<>();

    public PortfolioSnapshot() {}

    public PortfolioSnapshot(double cashBalance, List<ProductSnapshot> productListSnapshot) {
        this.cashBalance = cashBalance;
        this.productListSnapshot = productListSnapshot;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }

    public List<ProductSnapshot> getProductListSnapshot() {
        return productListSnapshot;
    }

    public void setProductListSnapshot(List<ProductSnapshot> productListSnapshot) {
        this.productListSnapshot = productListSnapshot;
    }

    public static class ProductSnapshot {
        private Ticker ticker;
        private double unitValue;
        private int position;

        public ProductSnapshot() {}

        public ProductSnapshot(Ticker ticker, double unitValue, int position) {
            this.ticker = ticker;
            this.unitValue = unitValue;
            this.position = position;
        }

        public Ticker getTicker() {
            return ticker;
        }

        public void setTicker(Ticker ticker) {
            this.ticker = ticker;
        }

        public double getUnitValue() {
            return unitValue;
        }

        public void setUnitValue(double unitValue) {
            this.unitValue = unitValue;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}
