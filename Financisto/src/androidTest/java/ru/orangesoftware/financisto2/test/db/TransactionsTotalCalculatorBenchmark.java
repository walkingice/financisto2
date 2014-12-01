/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.test.db;

import android.util.Log;

import java.util.Calendar;

import ru.orangesoftware.financisto2.db.TransactionsTotalCalculator;
import ru.orangesoftware.financisto2.filter.WhereFilter;
import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.Currency;
import ru.orangesoftware.financisto2.test.builders.AccountBuilder;
import ru.orangesoftware.financisto2.test.builders.CurrencyBuilder;
import ru.orangesoftware.financisto2.test.builders.DateTime;
import ru.orangesoftware.financisto2.test.builders.RateBuilder;
import ru.orangesoftware.financisto2.test.builders.TransactionBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 1/31/12 8:19 PM
 */
public class TransactionsTotalCalculatorBenchmark extends AbstractDbTest {

    Currency c1;
    Currency c2;

    Account a1;

    TransactionsTotalCalculator c;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        c1 = CurrencyBuilder.withDb(em).name("USD").title("Dollar").symbol("$").create();
        c2 = CurrencyBuilder.withDb(em).name("EUR").title("Euro").symbol("€").create();

        c = new TransactionsTotalCalculator(db, em, WhereFilter.empty());

        a1 = AccountBuilder.withDb(em).title("Cash").currency(c1).create();
    }

    public void test_should_benchmark_blotter_total_in_home_currency() {
        long t0 = System.currentTimeMillis();
        int count = 366;
        Calendar calendar = Calendar.getInstance();
        while (--count > 0) {
            DateTime date = DateTime.fromTimestamp(calendar.getTimeInMillis());
            RateBuilder.withDb(db).from(c1).to(c2).at(date).rate(1f/count).create();
            TransactionBuilder.withDb(db).account(a1).dateTime(date.atMidnight()).amount(1000).create();
            TransactionBuilder.withDb(db).account(a1).dateTime(date.atNoon()).amount(2000).create();
            TransactionBuilder.withDb(db).account(a1).dateTime(date.atDayEnd()).amount(3000).create();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        long t1 = System.currentTimeMillis();
        Log.d("TransactionsTotalCalculatorBenchmark", "Time to create a year amount of data: " + (t1 - t0) + "ms");
        c.getAccountBalance(c2, a1.id);
        long t2 = System.currentTimeMillis();
        Log.d("TransactionsTotalCalculatorBenchmark", "Time to get account total: " + (t2 - t1) + "ms");
    }
    
}
