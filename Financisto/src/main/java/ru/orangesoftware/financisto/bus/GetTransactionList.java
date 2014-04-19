package ru.orangesoftware.financisto.bus;

import ru.orangesoftware.financisto.filter.WhereFilter;

public class GetTransactionList {

    public final WhereFilter filter;

    public GetTransactionList(WhereFilter filter) {
        this.filter = filter;
    }

}
