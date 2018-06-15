package com.capco.noc.algo.schema;

import com.capco.noc.algo.schema.strategy.StrategyUnit;

import java.util.ArrayList;
import java.util.List;

public class Strategy {

    private List<StrategyUnit> units = new ArrayList<>();

    public Strategy() {}

    public List<StrategyUnit> getUnits() {
        return units;
    }

    public void setUnits(List<StrategyUnit> units) {
        this.units = units;
    }
}
