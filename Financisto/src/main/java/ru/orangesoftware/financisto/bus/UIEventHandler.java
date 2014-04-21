package ru.orangesoftware.financisto.bus;

import android.content.Context;
import android.database.Cursor;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.filter.WhereFilter;
import ru.orangesoftware.financisto.model.Account;
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
