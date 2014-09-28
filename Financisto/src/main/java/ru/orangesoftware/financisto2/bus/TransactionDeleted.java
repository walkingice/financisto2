package ru.orangesoftware.financisto2.bus;

public class TransactionDeleted {

    public final long id;

    public TransactionDeleted(long id) {
        this.id = id;
    }

}
