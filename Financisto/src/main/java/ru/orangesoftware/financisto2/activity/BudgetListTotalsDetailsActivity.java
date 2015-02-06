/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.activity;

import android.content.Intent;
import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.filter.WhereFilter;
import ru.orangesoftware.financisto2.db.BudgetsTotalCalculator;
import ru.orangesoftware.financisto2.model.Budget;
import ru.orangesoftware.financisto2.model.Total;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 3/15/12 16:40 PM
 */
public class BudgetListTotalsDetailsActivity extends AbstractTotalsDetailsActivity  {

    private WhereFilter filter = WhereFilter.empty();
    private BudgetsTotalCalculator calculator;
    
    public BudgetListTotalsDetailsActivity() {
        super(R.string.budget_total_in_currency);
    }

    @Override
    protected void internalOnCreate() {
        Intent intent = getIntent();
        if (intent != null) {
            filter = WhereFilter.fromIntent(intent);
        }
    }

    @Override
    protected void prepareInBackground() {
        List<Budget> budgets = db.getAllBudgets(filter);
        calculator = new BudgetsTotalCalculator(db, categoryRepository, budgets);
        calculator.updateBudgets(null);
    }

    protected Total getTotalInHomeCurrency() {
        return calculator.calculateTotalInHomeCurrency();
    }

    protected Total[] getTotals() {
        return calculator.calculateTotals();
    }

}
