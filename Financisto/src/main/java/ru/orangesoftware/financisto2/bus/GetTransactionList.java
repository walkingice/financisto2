package ru.orangesoftware.financisto2.bus;

import ru.orangesoftware.financisto2.filter.WhereFilter;

public class GetTransactionList {

    public final WhereFilter filter;

    public GetTransactionList(WhereFilter filter) {
        this.filter = filter;
    }

}
