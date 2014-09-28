package ru.orangesoftware.financisto2.bus;

import java.util.List;

import ru.orangesoftware.financisto2.model.Account;

public class AccountList {

    public final List<Account> accounts;

    public AccountList(List<Account> accounts) {
        this.accounts = accounts;
    }

}
