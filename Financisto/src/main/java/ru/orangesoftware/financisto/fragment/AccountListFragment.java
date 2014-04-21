package ru.orangesoftware.financisto.fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.activity.AccountActivity_;
import ru.orangesoftware.financisto.adapter.AccountListAdapter2;
import ru.orangesoftware.financisto.adapter.AccountListAdapter2_;
import ru.orangesoftware.financisto.bus.AccountList;
import ru.orangesoftware.financisto.bus.GetAccountList;

@EFragment(R.layout.account_list)
@OptionsMenu(R.menu.account_list_menu)
public class AccountListFragment extends AbstractListFragment {

    @AfterViews
    public void afterViews() {
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

}
