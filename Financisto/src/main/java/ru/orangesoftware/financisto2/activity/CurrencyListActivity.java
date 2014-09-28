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
package ru.orangesoftware.financisto2.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.support.v4.app.NavUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.List;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.adapter.CurrencyListAdapter;
import ru.orangesoftware.financisto2.bus.DeleteEntity;
import ru.orangesoftware.financisto2.bus.GreenRobotBus;
import ru.orangesoftware.financisto2.db.MyEntityManager;
import ru.orangesoftware.financisto2.model.Currency;

@EActivity(R.layout.currency_list)
@OptionsMenu(R.menu.currency_list_menu)
public class CurrencyListActivity extends ListActivity {
	
	private static final int NEW_CURRENCY_REQUEST = 1;
	private static final int EDIT_CURRENCY_REQUEST = 2;

    @Bean
    protected MyEntityManager em;

    @Bean
    protected GreenRobotBus bus;

    @Override
    protected void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @AfterViews
    protected void afterViews() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        reload();
    }

    private void reload() {
        List<Currency> currencies = em.getAllCurrenciesList("name");
        CurrencyListAdapter adapter = new CurrencyListAdapter(bus, currencies);
        setListAdapter(adapter);
    }

    @OptionsItem(R.id.menu_add)
    protected void onAdd() {
        new CurrencySelector(this, em, new CurrencySelector.OnCurrencyCreatedListener() {
            @Override
            public void onCreated(long currencyId) {
                if (currencyId == 0) {
                    CurrencyActivity_.intent(CurrencyListActivity.this).startForResult(NEW_CURRENCY_REQUEST);
                } else {
                    reload();
                }
            }
        }).show();
    }

    @OptionsItem(R.id.menu_rates)
    protected void onShowRates() {
        ExchangeRatesListActivity_.intent(this).start();
    }

    @ItemClick(android.R.id.list)
    protected void onItemClick(Currency currency) {
        CurrencyActivity_.intent(CurrencyListActivity.this)
                .currencyId(currency.id)
                .startForResult(EDIT_CURRENCY_REQUEST);
    }

    @OptionsItem(android.R.id.home)
    public void onHome() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DeleteEntity event) {
		if (em.deleteCurrency(event.id) == 1) {
			reload();
		} else {
			new AlertDialog.Builder(this)
				.setTitle(R.string.delete)
				.setMessage(R.string.currency_delete_alert)
				.setNeutralButton(R.string.ok, null).show();
		}
    }

//    private void makeCurrencyDefault(long id) {
//        Currency c = em.get(Currency.class, id);
//        c.isDefault = true;
//        em.saveOrUpdate(c);
//        recreateCursor();
//    }
//
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			reload();
		}
	}

}

