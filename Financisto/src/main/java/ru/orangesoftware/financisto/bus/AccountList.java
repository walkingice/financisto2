package ru.orangesoftware.financisto.bus;

import java.util.List;

import ru.orangesoftware.financisto.model.Account;

public class AccountList {

    public final List<Account> accounts;

    public AccountList(List<Account> accounts) {
        this.accounts = accounts;
    }

}
