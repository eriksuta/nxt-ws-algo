package com.capco.noc.algo.service;

import com.capco.noc.algo.strategy.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StrategyProvider {

    //TODO - change this method with YOUR trading strategy
    public StrategyCreator getStrategyCreator(){
        return new StrategyCreatorExample();
    }

    public List<StrategyCreator> getAllStrategyCreators() {
        List<StrategyCreator> strategyCreatorList = new ArrayList<>();

        strategyCreatorList.add(new StrategyCreatorEmpty());
        strategyCreatorList.add(new StrategyCreatorExample());

        return strategyCreatorList;
    }
}
