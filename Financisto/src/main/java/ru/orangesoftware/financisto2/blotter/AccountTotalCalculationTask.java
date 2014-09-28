/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.blotter;

import android.content.Context;
import android.widget.TextView;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.MyEntityManager;
import ru.orangesoftware.financisto2.db.TransactionsTotalCalculator;
import ru.orangesoftware.financisto2.filter.WhereFilter;
import ru.orangesoftware.financisto2.model.Total;

import static ru.orangesoftware.financisto2.db.DatabaseAdapter.enhanceFilterForAccountBlotter;

public class AccountTotalCalculationTask extends TotalCalculationTask {

	private final DatabaseAdapter db;
    private final MyEntityManager em;
	private final WhereFilter filter;

	public AccountTotalCalculationTask(Context context, DatabaseAdapter db, MyEntityManager em, WhereFilter filter, TextView totalText) {
        super(context, totalText);
		this.db = db;
        this.em = em;
		this.filter = enhanceFilterForAccountBlotter(filter);
	}

    @Override
    public Total getTotalInHomeCurrency() {
        TransactionsTotalCalculator calculator = new TransactionsTotalCalculator(db, em, filter);
        return calculator.getAccountTotal();
    }

    @Override
    public Total[] getTotals() {
        TransactionsTotalCalculator calculator = new TransactionsTotalCalculator(db, em, filter);
        return calculator.getTransactionsBalance();
    }

}
