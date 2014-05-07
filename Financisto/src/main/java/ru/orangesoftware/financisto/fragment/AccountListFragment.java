package ru.orangesoftware.financisto.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.activity.AccountActivity_;
import ru.orangesoftware.financisto.activity.TransactionActivity;
import ru.orangesoftware.financisto.activity.TransactionActivity_;
import ru.orangesoftware.financisto.activity.TransferActivity_;
import ru.orangesoftware.financisto.adapter.AccountListAdapter2;
import ru.orangesoftware.financisto.adapter.AccountListAdapter2_;
import ru.orangesoftware.financisto.bus.AccountList;
import ru.orangesoftware.financisto.bus.GetAccountList;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.model.Account;

import static ru.orangesoftware.financisto.utils.MyPreferences.isQuickMenuEnabledForAccount;

@EFragment(R.layout.account_list)
@OptionsMenu(R.menu.account_list_menu)
public class AccountListFragment extends AbstractListFragment implements QuickActionWidget.OnQuickActionClickListener {

    @Bean
    protected MyEntityManager em;

    private QuickActionWidget accountActionGrid;

    private long selectedId = -1;

    @AfterViews
    public void afterViews() {
        bus.post(new GetAccountList());
        prepareAccountActionGrid();
    }

    private void prepareAccountActionGrid() {
        Context context = getActivity();
        accountActionGrid = new QuickActionGrid(context);
        accountActionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_info, R.string.info));
        accountActionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_list, R.string.blotter));
        accountActionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_edit, R.string.edit));
        accountActionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_add, R.string.transaction));
        accountActionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_transfer_thin, R.string.transfer));
        accountActionGrid.addQuickAction(new QuickAction(context, R.drawable.ic_action_tick, R.string.balance));
        accountActionGrid.setOnQuickActionClickListener(this);
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
            accountActionGrid.show(v);
        } else {
            showAccountTransactions(id);
        }
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
        Account a = em.getAccount(selectedId);
        if (a != null) {
            TransactionActivity_.intent(this).accountId(selectedId)
                    .currentBalance(a.totalAmount).isUpdateBalanceMode(true).start();
        }
    }

}
