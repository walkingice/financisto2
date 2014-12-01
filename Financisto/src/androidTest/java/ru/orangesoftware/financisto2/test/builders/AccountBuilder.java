package ru.orangesoftware.financisto2.test.builders;

import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.MyEntityManager;
import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.Currency;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 3/2/11 9:07 PM
 */
public class AccountBuilder {

    private final MyEntityManager em;
    private final Account a = new Account();

    public static Account createDefault(MyEntityManager em) {
        Currency c = CurrencyBuilder.createDefault(em);
        return createDefault(em, c);
    }

    public static Account createDefault(MyEntityManager em, Currency c) {
        return withDb(em).title("Cash").currency(c).create();
    }

    public static AccountBuilder withDb(MyEntityManager em) {
        return new AccountBuilder(em);
    }

    private AccountBuilder(MyEntityManager em) {
        this.em = em;
    }

    public AccountBuilder title(String title) {
        a.title = title;
        return this;
    }

    public AccountBuilder currency(Currency c) {
        a.currency = c;
        return this;
    }
    
    public AccountBuilder total(long amount) {
        a.totalAmount = amount;
        return this;
    }

    public AccountBuilder doNotIncludeIntoTotals() {
        a.isIncludeIntoTotals = false;
        return this;
    }

    public AccountBuilder inactive() {
        a.isActive = false;
        return this;
    }

    public Account create() {
        em.saveAccount(a);
        return a;
    }
}
