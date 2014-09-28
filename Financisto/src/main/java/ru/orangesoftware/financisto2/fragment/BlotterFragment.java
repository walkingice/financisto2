package ru.orangesoftware.financisto2.fragment;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.activity.AccountWidget;
import ru.orangesoftware.financisto2.activity.BlotterOperations;
import ru.orangesoftware.financisto2.activity.TransactionActivity_;
import ru.orangesoftware.financisto2.activity.TransferActivity_;
import ru.orangesoftware.financisto2.adapter.BlotterListAdapter;
import ru.orangesoftware.financisto2.adapter.BlotterListAdapter_;
import ru.orangesoftware.financisto2.adapter.TransactionsListAdapter;
import ru.orangesoftware.financisto2.bus.GetTransactionList;
import ru.orangesoftware.financisto2.bus.TransactionDeleted;
import ru.orangesoftware.financisto2.bus.TransactionList;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.DatabaseAdapter_;
import ru.orangesoftware.financisto2.filter.WhereFilter;

@EFragment(R.layout.blotter)
@OptionsMenu(R.menu.blotter_menu)
public class BlotterFragment extends AbstractListFragment implements QuickActionWidget.OnQuickActionClickListener {

    @Bean
    protected DatabaseAdapter db;

    @FragmentArg
    protected long accountId = -1;
    private long selectedId = -1;

    private QuickActionWidget actionGrid;
    protected WhereFilter blotterFilter = WhereFilter.empty();

    @Override
    protected void reload() {
        bus.post(new GetTransactionList(blotterFilter));
    }

    public void onEventMainThread(TransactionList event) {
        FragmentActivity context = getActivity();
        DatabaseAdapter db = DatabaseAdapter_.getInstance_(context);
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
        TransactionActivity_.IntentBuilder_ intent = TransactionActivity_.intent(this);
        if (accountId != -1) {
            intent.accountId(accountId);
        }
        intent.template(blotterFilter.getIsTemplate());
        intent.start();
    }

    @OptionsItem(R.id.menu_add_transfer)
    public void addTransfer() {
        TransferActivity_.IntentBuilder_ intent = TransferActivity_.intent(this);
        if (accountId != -1) {
            intent.accountId(accountId);
        }
        intent.template(blotterFilter.getIsTemplate());
        intent.start();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        selectedId = id;
        prepareAccountActionGrid();
        actionGrid.show(v);
    }

    private void prepareAccountActionGrid() {
        Context context = getActivity();
        actionGrid = new QuickActionGrid(context);
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_info, R.string.info));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_edit, R.string.edit));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_trash, R.string.delete));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_copy, R.string.duplicate));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_tick, R.string.clear));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_double_tick, R.string.reconcile));
        actionGrid.setOnQuickActionClickListener(this);
    }

    @Override
    public void onQuickActionClicked(QuickActionWidget widget, int position) {
        switch (position) {
            case 0:
                showTransactionInfo();
                break;
            case 1:
                editTransaction();
                break;
            case 2:
                deleteTransaction();
                break;
            case 3:
                duplicateTransaction(1);
                break;
            case 4:
                clearTransaction();
                break;
            case 5:
                reconcileTransaction();
                break;
        }

    }

    private void showTransactionInfo() {

    }

    private void editTransaction() {
        new BlotterOperations(getActivity(), db, selectedId, bus).editTransaction();
    }

    private void deleteTransaction() {
        new BlotterOperations(getActivity(), db, selectedId, bus).deleteTransaction();
    }

    private long duplicateTransaction(int multiplier) {
        Context context = getActivity();
        long newId = new BlotterOperations(context, db, selectedId, bus).duplicateTransaction(multiplier);
        String toastText;
        if (multiplier > 1) {
            toastText = getString(R.string.duplicate_success_with_multiplier, multiplier);
        } else {
            toastText = getString(R.string.duplicate_success);
        }
        Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
        AccountWidget.updateWidgets(context);
        reload();
        return newId;
    }

    private void clearTransaction() {
        new BlotterOperations(getActivity(), db, selectedId, bus).clearTransaction();
        reload();
    }

    private void reconcileTransaction() {
        new BlotterOperations(getActivity(), db, selectedId, bus).reconcileTransaction();
        reload();
    }

    public void onEventMainThread(TransactionDeleted event) {
        AccountWidget.updateWidgets(getActivity());
        reload();
    }

}
