package ru.orangesoftware.financisto2.bus;

import android.database.Cursor;

public class TransactionList {

    public final long accountId;
    public final Cursor cursor;

    public TransactionList(long accountId, Cursor cursor) {
        this.accountId = accountId;
        this.cursor = cursor;
    }
}
