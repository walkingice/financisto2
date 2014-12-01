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

import android.app.Activity;
import android.content.Intent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import java.text.DecimalFormatSymbols;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.model.Currency;
import ru.orangesoftware.financisto2.model.SymbolFormat;
import ru.orangesoftware.financisto2.utils.CurrencyCache;
import ru.orangesoftware.financisto2.utils.PinProtection;

import static ru.orangesoftware.financisto2.utils.Utils.checkEditText;
import static ru.orangesoftware.financisto2.utils.Utils.text;

@EActivity(R.layout.currency)
public class CurrencyActivity extends Activity {

    public static final String CURRENCY_ID_EXTRA = "currencyId";
    private static final DecimalFormatSymbols s = new DecimalFormatSymbols();

    @Bean
    protected DatabaseAdapter db;

    @Extra
    protected long currencyId = -1;

    @StringArrayRes(R.array.decimal_separators)
    protected String[] decimalSeparatorsItems;
    @StringArrayRes(R.array.group_separators)
    protected String[] groupSeparatorsItems;
    private SymbolFormat[] symbolFormats = SymbolFormat.values();

    @ViewById(R.id.name)
    protected EditText name;
    @ViewById(R.id.title)
    protected EditText title;
    @ViewById(R.id.symbol)
    protected EditText symbol;
    @ViewById(R.id.is_default)
    protected CheckBox isDefault;
    @ViewById(R.id.spinnerDecimals)
    protected Spinner decimals;
    @ViewById(R.id.spinnerDecimalSeparators)
    protected Spinner decimalSeparators;
    @ViewById(R.id.spinnerGroupSeparators)
    protected Spinner groupSeparators;
    @ViewById(R.id.spinnerSymbolFormat)
    protected Spinner symbolFormat;

    private int maxDecimals;

    private Currency currency = new Currency();

    @AfterViews
    protected void afterViews() {
        groupSeparators.setSelection(1);
        symbolFormat.setSelection(0);
        maxDecimals = decimals.getCount() - 1;

        if (currencyId != -1) {
            currency = db.load(Currency.class, currencyId);
            editCurrency();
        } else {
            makeDefaultIfNecessary();
        }
    }

    @Click(R.id.bOK)
    protected void onOK() {
        if (checkEditText(title, "title", true, 100)
                && checkEditText(name, "code", true, 3)
                && checkEditText(symbol, "symbol", true, 3)) {
            currency.title = text(title);
            currency.name = text(name);
            currency.symbol = text(symbol);
            currency.isDefault = isDefault.isChecked();
            currency.decimals = maxDecimals - decimals.getSelectedItemPosition();
            currency.decimalSeparator = decimalSeparators.getSelectedItem().toString();
            currency.groupSeparator = groupSeparators.getSelectedItem().toString();
            currency.symbolFormat = symbolFormats[symbolFormat.getSelectedItemPosition()];
            long id = db.saveOrUpdate(currency);
            CurrencyCache.initialize(db);
            Intent data = new Intent();
            data.putExtra(CURRENCY_ID_EXTRA, id);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Click(R.id.bCancel)
    protected void onCancel() {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    private void makeDefaultIfNecessary() {
        isDefault.setChecked(db.getAllCurrenciesList().isEmpty());
    }

    private void editCurrency() {
        Currency currency = this.currency;
        EditText name = (EditText) findViewById(R.id.name);
        name.setText(currency.name);
        EditText title = (EditText) findViewById(R.id.title);
        title.setText(currency.title);
        EditText symbol = (EditText) findViewById(R.id.symbol);
        symbol.setText(currency.symbol);
        CheckBox isDefault = (CheckBox) findViewById(R.id.is_default);
        isDefault.setChecked(currency.isDefault);
        decimals.setSelection(maxDecimals - currency.decimals);
        decimalSeparators.setSelection(indexOf(decimalSeparatorsItems, currency.decimalSeparator, s.getDecimalSeparator()));
        groupSeparators.setSelection(indexOf(groupSeparatorsItems, currency.groupSeparator, s.getGroupingSeparator()));
        symbolFormat.setSelection(currency.symbolFormat.ordinal());
    }

    private int indexOf(String[] a, String v, char c) {
        int count = a.length;
        int d = -1;
        for (int i = 0; i < count; i++) {
            String s = a[i];
            if (v != null && s.charAt(1) == v.charAt(1)) {
                return i;
            }
            if (s.charAt(1) == c) {
                d = i;
            }
        }
        return d;
    }

    @Override
    protected void onPause() {
        super.onPause();
        PinProtection.lock(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PinProtection.unlock(this);
    }
}
