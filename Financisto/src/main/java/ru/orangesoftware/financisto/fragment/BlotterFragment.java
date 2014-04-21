package ru.orangesoftware.financisto.fragment;

import android.support.v4.app.FragmentActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.activity.TransactionActivity_;
import ru.orangesoftware.financisto.activity.TransferActivity_;
import ru.orangesoftware.financisto.adapter.BlotterListAdapter;
import ru.orangesoftware.financisto.adapter.BlotterListAdapter_;
import ru.orangesoftware.financisto.adapter.TransactionsListAdapter;
import ru.orangesoftware.financisto.bus.GetTransactionList;
import ru.orangesoftware.financisto.bus.TransactionList;
import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.filter.WhereFilter;

@EFragment(R.layout.blotter)
@OptionsMenu(R.menu.blotter_menu)
public class BlotterFragment extends AbstractListFragment {

    protected WhereFilter blotterFilter = WhereFilter.empty();

    @AfterViews
    public void afterViews() {
        bus.post(new GetTransactionList(blotterFilter));
    }

    public void onEventMainThread(TransactionList event) {
        FragmentActivity context = getActivity();
        DatabaseAdapter db = new DatabaseAdapter(context);
        BlotterListAdapter adapter;
        if (event.accountId != -1) {
            adapter = new TransactionsListAdapter(context, db, event.cursor);
        } else {
            adapter = BlotterListAdapter_.getInstance_(context);
        }
        adapter.initWithCursor(event.cursor);
        setListAdapter(adapter);
    }

    @OptionsItem(R.id.menu_add_transaction)
    public void addTransaction() {
        TransactionActivity_.intent(this).start();
//        Intent intent = new Intent(getActivity(), TransactionActivity.class);
//        long accountId = blotterFilter.getAccountId();
//        if (accountId != -1) {
//            intent.putExtra(TransactionActivity.ACCOUNT_ID_EXTRA, accountId);
//        }
//        intent.putExtra(TransactionActivity.TEMPLATE_EXTRA, blotterFilter.getIsTemplate());
//        startActivity(intent);
    }

    @OptionsItem(R.id.menu_add_transfer)
    public void addTransfer() {
        TransferActivity_.intent(this).start();
//        Intent intent = new Intent(getActivity(), TransactionActivity.class);
//        long accountId = blotterFilter.getAccountId();
//        if (accountId != -1) {
//            intent.putExtra(TransactionActivity.ACCOUNT_ID_EXTRA, accountId);
//        }
//        intent.putExtra(TransactionActivity.TEMPLATE_EXTRA, blotterFilter.getIsTemplate());
//        startActivity(intent);
    }

}
