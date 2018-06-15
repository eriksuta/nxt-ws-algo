package com.capco.noc.algo.repository;

import com.capco.noc.algo.schema.Account;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class AccountRepository {

    private Map<String, Account> accounts = new HashMap<>();

    public void add(Account account){
        accounts.put(account.getId(), account);
    }

    public Account get(String id){
        return accounts.get(id);
    }

    public void update(Account account){
        String accountId = account.getId();
        accounts.remove(accountId);
        accounts.put(accountId, account);
    }

    public void delete(String id){
        accounts.remove(id);
    }
}
