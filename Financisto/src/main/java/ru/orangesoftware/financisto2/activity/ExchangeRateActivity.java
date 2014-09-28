/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.datetime.DateUtils;
import ru.orangesoftware.financisto2.model.Currency;
import ru.orangesoftware.financisto2.rates.ExchangeRate;
import ru.orangesoftware.financisto2.widget.AmountInput;
import ru.orangesoftware.financisto2.widget.RateNode;
import ru.orangesoftware.financisto2.widget.RateNodeOwner;

import static ru.orangesoftware.financisto2.utils.Utils.formatRateDate;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 1/19/12 7:41 PM
 */
@EActivity(R.layout.exchange_rate)
public class ExchangeRateActivity extends AbstractActivity implements RateNodeOwner {

    @ViewById(R.id.list)
    protected LinearLayout layout;

    @Extra
    protected long fromCurrencyId;
    @Extra
    protected long toCurrencyId;
    @Extra
    protected long date = -1;
    private long originalDate = -1;
    private double rate = 1;

    private Currency fromCurrency;
    private Currency toCurrency;

    private TextView dateNode;
    private RateNode rateNode;

    @AfterViews
    protected void afterViews() {
        if (validateIntent()) {
            updateUI();
        } else {
            finish();
        }
    }

    @Click(R.id.bOK)
    protected void onOk() {
        ExchangeRate rate = createRateFromUI();
        db.replaceRate(rate, originalDate != -1 ? originalDate : rate.date);
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }

    @Click(R.id.bCancel)
    protected void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private ExchangeRate createRateFromUI() {
        ExchangeRate rate = new ExchangeRate();
        rate.fromCurrencyId = fromCurrency.id;
        rate.toCurrencyId = toCurrency.id;
        rate.date = date;
        rate.rate = rateNode.getRate();
        return rate;
    }

    private void updateUI() {
        x.addInfoNode(layout, 0, R.string.rate_from_currency, fromCurrency.name);
        x.addInfoNode(layout, 0, R.string.rate_to_currency, toCurrency.name);
        dateNode = x.addInfoNode(layout, R.id.date, R.string.date, formatRateDate(this, date));
        rateNode = new RateNode(this, x, layout);
        rateNode.setRate(rate);
        rateNode.updateRateInfo();
    }

    private boolean validateIntent() {
        fromCurrency = em.get(Currency.class, fromCurrencyId);
        if (fromCurrency == null) {
            return false;
        }

        toCurrency = em.get(Currency.class, toCurrencyId);
        if (toCurrency == null) {
            return false;
        }

        if (date == -1) {
            date = DateUtils.atMidnight(System.currentTimeMillis());
        } else {
            originalDate = date;
        }

        ExchangeRate rate = db.findRate(fromCurrency, toCurrency, date);
        if (rate != null) {
            this.rate = rate.rate;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RateNode.EDIT_RATE) {
            String amount = data.getStringExtra(AmountInput.EXTRA_AMOUNT);
            if (amount != null) {
                rateNode.setRate(Float.parseFloat(amount));
                rateNode.updateRateInfo();
            }
        }
    }

    @Override
    protected void onClick(View v, int id) {
        switch (id) {
            case R.id.date:
                editDate();
                break;
        }
    }

    private void editDate() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        DatePickerDialog d = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker arg0, int y, int m, int d) {
                c.set(y, m, d);
                date = c.getTimeInMillis();
                dateNode.setText(formatRateDate(ExchangeRateActivity.this, date));
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        d.show();
    }

    @Override
    public void onBeforeRateDownload() {
        rateNode.disableAll();
    }

    @Override
    public void onAfterRateDownload() {
        rateNode.enableAll();
    }

    @Override
    public void onSuccessfulRateDownload() {
        rateNode.updateRateInfo();
    }

    @Override
    public void onRateChanged() {
        rateNode.updateRateInfo();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Currency getCurrencyFrom() {
        return fromCurrency;
    }

    @Override
    public Currency getCurrencyTo() {
        return toCurrency;
    }

}
