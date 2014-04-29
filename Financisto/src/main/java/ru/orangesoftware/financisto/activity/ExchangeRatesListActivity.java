/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.SystemService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.db.DatabaseAdapter;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.model.Currency;
import ru.orangesoftware.financisto.rates.ExchangeRate;
import ru.orangesoftware.financisto.rates.ExchangeRateProvider;
import ru.orangesoftware.financisto.utils.CurrencyCache;
import ru.orangesoftware.financisto.utils.MyPreferences;

import static ru.orangesoftware.financisto.utils.Utils.formatRateDate;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 1/18/12 11:10 PM
 */
@EActivity(R.layout.exchange_rate_list)
@OptionsMenu(R.menu.exchange_rate_list_menu)
public class ExchangeRatesListActivity extends ListActivity {

    private static final int ADD_RATE = 1;
    private static final int EDIT_RATE = 1;

    private static final DecimalFormat nf = new DecimalFormat("0.00000");
    private static final String NEW_LINE = String.format("%n");

    @Bean
    protected MyEntityManager em;
    @Bean
    protected DatabaseAdapter db;

    @SystemService
    protected LayoutInflater inflater;

    private List<Currency> currencies;
    private List<CurrencyPair> currencyPairs;

    private CurrencyPair selectedPair;

    @AfterViews
    protected void afterViews() {
        ActionBar actionBar = getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        currencies = em.getAllCurrenciesList("name");
        currencyPairs = collectPairs();

        actionBar.setListNavigationCallbacks(createNavigationAdapter(), new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                selectedPair = currencyPairs.get(itemPosition);
                updateAdapter();
                return true;
            }
        });

        updateAdapter();
    }

    private List<CurrencyPair> collectPairs() {
        List<CurrencyPair> list = new ArrayList<CurrencyPair>();
        for (Currency from : currencies) {
            for (Currency to : currencies) {
                if (from != to) {
                    list.add(new CurrencyPair(from, to));
                }
            }
        }
        return list;
    }

    private SpinnerAdapter createNavigationAdapter() {
        return new ArrayAdapter<CurrencyPair>(getActionBar().getThemedContext(),
                android.R.layout.simple_spinner_dropdown_item, currencyPairs);
    }

    private void updateAdapter() {
        if (selectedPair != null) {
            List<ExchangeRate> rates = db.findRates(selectedPair.from, selectedPair.to);
            ListAdapter adapter = new ExchangeRateListAdapter(this, rates);
            setListAdapter(adapter);
        } else {
            setListAdapter(null);
        }
    }

    @OptionsItem(R.id.menu_add)
    protected void addItem() {
        if (selectedPair != null) {
            ExchangeRateActivity_.intent(this)
                    .fromCurrencyId(selectedPair.from.id)
                    .toCurrencyId(selectedPair.to.id)
                    .startForResult(ADD_RATE);
        }
    }

    @OptionsItem(R.id.menu_swap)
    protected void onSwapCurrencies() {
        if (selectedPair != null) {
            for (int i=0; i<currencyPairs.size(); i++) {
                CurrencyPair pair = currencyPairs.get(i);
                if (pair.from == selectedPair.to && pair.to == selectedPair.from) {
                    getActionBar().setSelectedNavigationItem(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            updateAdapter();
        }
    }

    private void deleteRate(ExchangeRate rate) {
        db.deleteRate(rate);
        updateAdapter();
    }

    @ItemClick(android.R.id.list)
    protected void editItem(ExchangeRate rate) {
        editRate(rate);
    }

    private void editRate(ExchangeRate rate) {
        ExchangeRateActivity_.intent(this)
                .fromCurrencyId(rate.fromCurrencyId)
                .toCurrencyId(rate.toCurrencyId)
                .date(rate.date)
                .startForResult(EDIT_RATE);
    }

    @OptionsItem(R.id.menu_download_all)
    protected void onDownloadAllRates() {
        new RatesDownloadTask(this).execute();
    }

    private class RatesDownloadTask extends AsyncTask<Void, Void, List<ExchangeRate>> {

        private final Context context;
        private ProgressDialog progressDialog;

        private RatesDownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected List<ExchangeRate> doInBackground(Void... args) {
            List<ExchangeRate> rates = getProvider().getRates(currencies);
            if (isCancelled()) {
                return null;
            } else {
                db.saveDownloadedRates(rates);
                return rates;
            }
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        private void showProgressDialog() {
            String message = context.getString(R.string.downloading_rates, asString(currencies));
            progressDialog = ProgressDialog.show(context, null, message, true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            });
        }

        private String asString(List<Currency> currencies) {
            StringBuilder sb = new StringBuilder();
            for (Currency currency : currencies) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(currency.name);
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(List<ExchangeRate> result) {
            progressDialog.dismiss();
            if (result != null) {
                showResult(result);
                updateAdapter();
            }
        }

        private void showResult(List<ExchangeRate> result) {
            StringBuilder sb = new StringBuilder();
            for (ExchangeRate rate : result) {
                if (sb.length() > 0) sb.append(NEW_LINE);
                Currency fromCurrency = CurrencyCache.getCurrency(em, rate.fromCurrencyId);
                Currency toCurrency = CurrencyCache.getCurrency(em, rate.toCurrencyId);
                sb.append(fromCurrency.name).append("\u2192").append(toCurrency.name);
                if (rate.isOk()) {
                    sb.append(" = ").append(nf.format(rate.rate));
                } else {
                    sb.append(" = ").append(rate.getErrorMessage());
                }
                sb.append(NEW_LINE);
            }
            new AlertDialog.Builder(context)
                    .setTitle(R.string.downloading_rates_result)
                    .setMessage(sb.toString())
                    .setNeutralButton(R.string.ok, null)
                    .create().show();
        }

        private ExchangeRateProvider getProvider() {
            return MyPreferences.createExchangeRatesProvider(context);
        }

    }

    private class ExchangeRateListAdapter extends BaseAdapter {

        private final Context context;
        private final List<ExchangeRate> rates;

        private ExchangeRateListAdapter(Context context, List<ExchangeRate> rates) {
            this.context = context;
            this.rates = rates;
        }

        @Override
        public int getCount() {
            return rates.size();
        }

        @Override
        public ExchangeRate getItem(int i) {
            return rates.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RateViewHolder v;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.exchange_rate_list_item, parent, false);
                v = RateViewHolder.createAndTag(convertView);
            } else {
                v = (RateViewHolder)convertView.getTag();
            }
            final ExchangeRate rate = getItem(position);
            v.date.setText(formatRateDate(context, rate.date));
            v.rate.setText(nf.format(rate.rate));
            v.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteRate(rate);
                }
            });
            return convertView;
        }
    }

    private static class RateViewHolder {

        public TextView date;
        public TextView rate;
        public ImageView delete;

        public static RateViewHolder createAndTag(View view) {
            RateViewHolder views = new RateViewHolder();
            views.date = (TextView)view.findViewById(R.id.date);
            views.rate = (TextView)view.findViewById(R.id.rate);
            views.delete = (ImageView) view.findViewById(R.id.delete);
            view.setTag(views);
            return views;
        }
    }

    private static class CurrencyPair {

        public final Currency from;
        public final Currency to;

        private CurrencyPair(Currency from, Currency to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            return from+"\u2192"+to;
        }
    }

}
