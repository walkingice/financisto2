package ru.orangesoftware.financisto.fragment;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.activity.AccountActivity_;
import ru.orangesoftware.financisto.adapter.AccountListAdapter2;
import ru.orangesoftware.financisto.adapter.AccountListAdapter2_;
import ru.orangesoftware.financisto.bus.AccountList;
import ru.orangesoftware.financisto.bus.GetAccountList;

import static ru.orangesoftware.financisto.utils.MyPreferences.isQuickMenuEnabledForAccount;

@EFragment(R.layout.account_list)
@OptionsMenu(R.menu.account_list_menu)
public class AccountListFragment extends AbstractListFragment implements QuickActionWidget.OnQuickActionClickListener {

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
            //showAccountTransactions(id);
        }
    }

    @Override
    public void onQuickActionClicked(QuickActionWidget widget, int position) {

    }

}
