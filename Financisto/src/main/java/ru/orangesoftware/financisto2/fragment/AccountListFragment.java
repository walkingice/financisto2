package ru.orangesoftware.financisto2.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ListView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.activity.AccountActivity_;
import ru.orangesoftware.financisto2.activity.PurgeAccountActivity_;
import ru.orangesoftware.financisto2.activity.TransactionActivity_;
import ru.orangesoftware.financisto2.activity.TransferActivity_;
import ru.orangesoftware.financisto2.adapter.AccountListAdapter2;
import ru.orangesoftware.financisto2.adapter.AccountListAdapter2_;
import ru.orangesoftware.financisto2.bus.AccountList;
import ru.orangesoftware.financisto2.bus.GetAccountList;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.model.Account;

import static ru.orangesoftware.financisto2.utils.MyPreferences.isQuickMenuEnabledForAccount;

@EFragment(R.layout.account_list)
@OptionsMenu(R.menu.account_list_menu)
public class AccountListFragment extends AbstractListFragment implements QuickActionWidget.OnQuickActionClickListener {

    @Bean
    protected DatabaseAdapter db;

    private QuickActionWidget actionGrid;

    private long selectedId = -1;

    @Override
    protected void reload() {
        bus.post(new GetAccountList());
    }

    public void onEventMainThread(AccountList event) {
        AccountListAdapter2 adapter = AccountListAdapter2_.getInstance_(getActivity());
        adapter.initAccounts(event.accounts);
        setListAdapter(adapter);
    }

    @OptionsItem(R.id.menu_add_account)
    public void addAccount() {
        AccountActivity_.intent(this).start();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (isQuickMenuEnabledForAccount(getActivity())) {
            selectedId = id;
            prepareAccountActionGrid();
            actionGrid.show(v);
        } else {
            showAccountTransactions(id);
        }
    }

    private void prepareAccountActionGrid() {
        Context context = getActivity();
        Account a = db.getAccount(selectedId);
        actionGrid = new QuickActionGrid(context);
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_info, R.string.info));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_list, R.string.blotter));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_edit, R.string.edit));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_add, R.string.transaction));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_transfer_thin, R.string.transfer));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_tick, R.string.balance));
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_flash, R.string.delete_old_transactions));
        if (a.isActive) {
            actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_lock_closed, R.string.close_account));
        } else {
            actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_lock_open, R.string.reopen_account));
        }
        actionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_trash, R.string.delete_account));
        actionGrid.setOnQuickActionClickListener(this);
    }

    @Override
    public void onQuickActionClicked(QuickActionWidget widget, int position) {
        switch (position) {
            case 0:
                showAccountInfo();
                break;
            case 1:
                showAccountTransactions(selectedId);
                break;
            case 2:
                editAccount();
                break;
            case 3:
                addTransaction();
                break;
            case 4:
                addTransaction();
                break;
            case 5:
                updateAccountBalance();
                break;
            case 6:
                purgeAccount();
                break;
            case 7:
                flipAccount();
                break;
            case 8:
                deleteAccount();
                break;
        }
    }

    private void showAccountInfo() {

    }

    private void showAccountTransactions(long id) {

    }

    private void editAccount() {
        AccountActivity_.intent(this).accountId(selectedId).start();
    }

    private void addTransaction() {
        TransactionActivity_.intent(this).accountId(selectedId).start();
    }

    private void addTransfer() {
        TransferActivity_.intent(this).accountId(selectedId).start();
    }

    private void updateAccountBalance() {
        Account a = db.getAccount(selectedId);
        if (a != null) {
            TransactionActivity_.intent(this).accountId(selectedId)
                    .currentBalance(a.totalAmount).isUpdateBalanceMode(true).start();
        }
    }

    private void purgeAccount() {
        PurgeAccountActivity_.intent(this).accountId(selectedId).startForResult(0);
    }

    private void flipAccount() {
        Account a = db.getAccount(selectedId);
        a.isActive = !a.isActive;
        db.saveAccount(a);
        reload();
    }

    private void deleteAccount() {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.delete_account_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        db.deleteAccount(selectedId);
                        reload();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

}
