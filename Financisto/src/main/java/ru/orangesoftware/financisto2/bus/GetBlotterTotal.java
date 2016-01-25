package ru.orangesoftware.financisto2.bus;

import ru.orangesoftware.financisto2.filter.WhereFilter;

public class GetBlotterTotal {

    public final WhereFilter filter;

    public GetBlotterTotal(WhereFilter filter) {
        this.filter = filter;
    }

}
