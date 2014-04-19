package ru.orangesoftware.financisto.bus;

import android.content.Context;
import android.database.Cursor;
import android.widget.ListAdapter;

import java.util.List;

import ru.orangesoftware.financisto.adapter.BlotterListAdapter;
import ru.orangesoftware.financisto.adapter.TransactionsListAdapter;
import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.filter.WhereFilter;
import ru.orangesoftware.financisto.model.Account;
import ru.orangesoftware.financisto.utils.MyPreferences;

public class UIEventHandler {

    private final Context context;
    private final DatabaseAdapter db;
    private final MyEntityManager em;
    private final GreenRobotBus bus;

    public UIEventHandler(Context context, DatabaseAdapter db, GreenRobotBus bus) {
        this.context = context;
        this.db = db;
        this.bus = bus;
        this.em = db.em();
    }

    public void onEventBackgroundThread(GetAccountList event) {
        List<Account> accounts = em.getAllAccountsList(MyPreferences.isHideClosedAccounts(context));
        bus.post(new AccountList(accounts));
    }

    public void onEventBackgroundThread(GetTransactionList event) {
        Cursor c;
        WhereFilter filter = event.filter;
        long accountId = filter.getAccountId();
        if (accountId != -1) {
            c = db.getBlotterForAccount(filter);
        } else {
            c = db.getBlotter(filter);
        }
        bus.post(new TransactionList(accountId, c));
    }

}
