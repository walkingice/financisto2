package ru.orangesoftware.financisto.fragment;

import android.content.Intent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.activity.AccountActivity;
import ru.orangesoftware.financisto.adapter.AccountListAdapter2;
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
        AccountListAdapter2 adapter = new AccountListAdapter2(getActivity(), event.accounts);
        setListAdapter(adapter);
    }

    @OptionsItem(R.id.menu_add_account)
    public void addAccount() {
        Intent intent = new Intent(getActivity(), AccountActivity.class);
        startActivity(intent);
    }

}
