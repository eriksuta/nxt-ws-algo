package com.capco.noc.algo.schema;

public class Account {

    private String id;
    private String username;
    private String password;

    private Portfolio portfolio;

    public Account() {}

    public Account(String id, String username, String password, Portfolio portfolio) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.portfolio = portfolio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }
}
