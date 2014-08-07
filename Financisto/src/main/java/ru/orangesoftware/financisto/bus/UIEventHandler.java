package ru.orangesoftware.financisto.bus;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.db.DatabaseHelper;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.filter.WhereFilter;
import ru.orangesoftware.financisto.model.Account;
import ru.orangesoftware.financisto.utils.CurrencyCache;
import ru.orangesoftware.financisto.utils.MyPreferences;

@EBean(scope = EBean.Scope.Singleton)
public class UIEventHandler {

    private final Context context;

    @Bean
    public DatabaseAdapter db;

    @Bean
    public MyEntityManager em;

    @Bean
    public GreenRobotBus bus;

    public UIEventHandler(Context context) {
        this.context = context;
    }

    public void onEventBackgroundThread(InitialLoad event) {
        initialLoad();
    }

    private void initialLoad() {
        long t3, t2, t1, t0 = System.currentTimeMillis();
        SQLiteDatabase x = db.db();
        x.beginTransaction();
        t1 = System.currentTimeMillis();
        try {
            updateFieldInTable(x, DatabaseHelper.CATEGORY_TABLE, 0, "title", context.getString(R.string.no_category));
            updateFieldInTable(x, DatabaseHelper.CATEGORY_TABLE, -1, "title", context.getString(R.string.split));
            updateFieldInTable(x, DatabaseHelper.PROJECT_TABLE, 0, "title", context.getString(R.string.no_project));
            updateFieldInTable(x, DatabaseHelper.LOCATIONS_TABLE, 0, "name", context.getString(R.string.current_location));
            x.setTransactionSuccessful();
        } finally {
            x.endTransaction();
        }
        t2 = System.currentTimeMillis();
        if (MyPreferences.shouldUpdateHomeCurrency(context)) {
            db.setDefaultHomeCurrency();
        }
        CurrencyCache.initialize(em);
        t3 = System.currentTimeMillis();
        if (MyPreferences.shouldRebuildRunningBalance(context)) {
            db.rebuildRunningBalances();
        }
        if (MyPreferences.shouldUpdateAccountsLastTransactionDate(context)) {
            db.updateAccountsLastTransactionDate();
        }
        long t4 = System.currentTimeMillis();
        Log.d("Financisto", "Load time = " + (t4 - t0) + "ms = " + (t2 - t1) + "ms+" + (t3 - t2) + "ms+" + (t4 - t3) + "ms");
    }

    private void updateFieldInTable(SQLiteDatabase db, String table, long id, String field, String value) {
        db.execSQL("update " + table + " set " + field + "=? where _id=?", new Object[]{value, id});
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
