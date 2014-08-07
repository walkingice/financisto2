/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.blotter.BlotterFilter;
import ru.orangesoftware.financisto.blotter.TotalCalculationTask;
import ru.orangesoftware.financisto.dialog.AccountInfoDialog;
import ru.orangesoftware.financisto.filter.Criteria;
import ru.orangesoftware.financisto.model.Account;
import ru.orangesoftware.financisto.model.Total;
import ru.orangesoftware.financisto.utils.MenuItemInfo;
import ru.orangesoftware.financisto.view.NodeInflater;

import static ru.orangesoftware.financisto.utils.AndroidUtils.isGreenDroidSupported;
import static ru.orangesoftware.financisto.utils.MyPreferences.isQuickMenuEnabledForAccount;

public class AccountListActivity extends AbstractListActivity {
	
	private static final int NEW_ACCOUNT_REQUEST = 1;

    public static final int EDIT_ACCOUNT_REQUEST = 2;
    private static final int VIEW_ACCOUNT_REQUEST = 3;
    private static final int PURGE_ACCOUNT_REQUEST = 4;

	private static final int MENU_UPDATE_BALANCE = MENU_ADD+1;
    private static final int MENU_CLOSE_OPEN_ACCOUNT = MENU_ADD+2;
    private static final int MENU_PURGE_ACCOUNT = MENU_ADD+3;

    private QuickActionWidget accountActionGrid;

    public AccountListActivity() {
        super(R.layout.account_list);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		calculateTotals();
        integrityCheck();
	}

    @Override
    public void recreateCursor() {
        super.recreateCursor();
        calculateTotals();
    }

    private AccountTotalsCalculationTask totalCalculationTask;

	private void calculateTotals() {
		if (totalCalculationTask != null) {
			totalCalculationTask.stop();
			totalCalculationTask.cancel(true);
		}		
		TextView totalText = (TextView)findViewById(R.id.total);
        totalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTotals();
            }
        });
        totalCalculationTask = new AccountTotalsCalculationTask(this, totalText);
		totalCalculationTask.execute();
	}

    private void showTotals() {
        Intent intent = new Intent(this, AccountListTotalsDetailsActivity.class);
        startActivityForResult(intent, -1);
    }
	
	public class AccountTotalsCalculationTask extends TotalCalculationTask {

        public AccountTotalsCalculationTask(Context context, TextView totalText) {
            super(context, totalText);
        }

        @Override
        public Total getTotalInHomeCurrency() {
            return db.getAccountsTotalInHomeCurrency();
        }

        @Override
        public Total[] getTotals() {
            return new Total[0];
        }

    }

	@Override
	protected ListAdapter createAdapter(Cursor cursor) {
		return null; //new AccountListAdapter2(this, cursor);
	}

	@Override
	protected Cursor createCursor() {
        return null;
//        if (MyPreferences.isHideClosedAccounts(this)) {
//            return em.getAllActiveAccounts();
//        } else {
//            return em.getAllAccounts();
//        }
	}

    @Override
	protected void addItem() {		
		Intent intent = new Intent(AccountListActivity.this, AccountActivity.class);
		startActivityForResult(intent, NEW_ACCOUNT_REQUEST);
	}

	@Override
	protected void deleteItem(View v, int position, final long id) {
	}

	@Override
	public void editItem(View v, int position, long id) {
        editAccount(id);
	}

    private void editAccount(long id) {
        AccountActivity_.intent(this).accountId(id).startForResult(EDIT_ACCOUNT_REQUEST);
    }

    private long selectedId = -1;

    private void showAccountInfo(long id) {
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        NodeInflater inflater = new NodeInflater(layoutInflater);
        AccountInfoDialog accountInfoDialog = new AccountInfoDialog(this, id, db, em, inflater);
        accountInfoDialog.show();
    }

    @Override
    protected void onItemClick(View v, int position, long id) {
        if (isQuickMenuEnabledForAccount(this)) {
            selectedId = id;
            accountActionGrid.show(v);
        } else {
            showAccountTransactions(id);
        }
    }

    @Override
	protected void viewItem(View v, int position, long id) {
        showAccountTransactions(id);
	}

    private void showAccountTransactions(long id) {
        Account account = em.getAccount(id);
        if (account != null) {
            Intent intent = new Intent(AccountListActivity.this, BlotterActivity.class);
            Criteria.eq(BlotterFilter.FROM_ACCOUNT_ID, String.valueOf(id))
                .toIntent(account.title, intent);
            intent.putExtra(BlotterFilterActivity.IS_ACCOUNT_FILTER, true);
            startActivityForResult(intent, VIEW_ACCOUNT_REQUEST);
        }
    }

    @Override
	protected String getContextMenuHeaderTitle(int position) {
		return getString(R.string.account);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == VIEW_ACCOUNT_REQUEST || requestCode == PURGE_ACCOUNT_REQUEST) {
			recreateCursor();
		}
	}

}
