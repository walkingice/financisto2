package ru.orangesoftware.financisto2.test.builders;

import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.MyEntityManager;
import ru.orangesoftware.financisto2.model.Currency;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 3/2/11 9:11 PM
 */
public class CurrencyBuilder {

    private final MyEntityManager em;
    private final Currency c = new Currency();

    public static Currency createDefault(MyEntityManager em) {
        return withDb(em).title("Singapore Dollar").name("SGD").symbol("S$").create();
    }

    public static CurrencyBuilder withDb(MyEntityManager em) {
        return new CurrencyBuilder(em);
    }

    private CurrencyBuilder(MyEntityManager em) {
        this.em = em;
    }

    public CurrencyBuilder title(String title) {
        c.title = title;
        return this;
    }

    public CurrencyBuilder name(String name) {
        c.name = name;
        return this;
    }

    public CurrencyBuilder symbol(String symbol) {
        c.symbol = symbol;
        return this;
    }

    public CurrencyBuilder separators(String groupSeparator, String decimalSeparator) {
        c.groupSeparator = groupSeparator;
        c.decimalSeparator = decimalSeparator;
        return this;
    }
    
    public CurrencyBuilder makeDefault() {
        c.isDefault = true;
        return this;
    }

    public Currency create() {
        em.saveOrUpdate(c);
        return c;
    }

}
