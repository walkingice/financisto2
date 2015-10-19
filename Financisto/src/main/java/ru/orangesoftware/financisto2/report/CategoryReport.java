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
package ru.orangesoftware.financisto2.report;

import android.content.Context;
import android.content.Intent;
import ru.orangesoftware.financisto2.activity.ReportActivity;
import ru.orangesoftware.financisto2.activity.ReportsListActivity;
import ru.orangesoftware.financisto2.blotter.BlotterFilter;
import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.db.CategoryRepository_;
import ru.orangesoftware.financisto2.filter.WhereFilter;
import ru.orangesoftware.financisto2.filter.Criteria;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.Currency;

import static ru.orangesoftware.financisto2.db.DatabaseHelper.V_REPORT_CATEGORY;

public class CategoryReport extends Report {

	public CategoryReport(Context context, Currency currency) {
		super(ReportType.BY_CATEGORY, context, currency);
    }

	@Override
	public ReportData getReport(DatabaseAdapter db, WhereFilter filter) {
        cleanupFilter(filter);
		filter.eq("parent_id", "0");
		return queryReport(db, V_REPORT_CATEGORY, filter);
	}

	@Override
	public Intent createActivityIntent(Context context, CategoryRepository categoryRepository, WhereFilter parentFilter, long id) {
        WhereFilter filter = createFilterForSubCategory(categoryRepository, parentFilter, id);
		Intent intent = new Intent(context, ReportActivity.class);
		filter.toIntent(intent);
		intent.putExtra(ReportsListActivity.EXTRA_REPORT_TYPE, ReportType.BY_SUB_CATEGORY.name());
        intent.putExtra(ReportActivity.FILTER_INCOME_EXPENSE, incomeExpense.name());
		return intent;
	}

    public WhereFilter createFilterForSubCategory(CategoryRepository categoryRepository, WhereFilter parentFilter, long id) {
        WhereFilter filter = WhereFilter.empty();
        Criteria c = parentFilter.get(BlotterFilter.DATETIME);
        if (c != null) {
            filter.put(c);
        }
        filterTransfers(filter);
        Category category = categoryRepository.getCategoryById(id);
        filter.put(Criteria.gte("left", String.valueOf(category.left)));
        filter.put(Criteria.lte("right", String.valueOf(category.right)));
        return filter;
    }

    @Override
	public Criteria getCriteriaForId(CategoryRepository categoryRepository, long id) {
		Category c = categoryRepository.getCategoryById(id);
		return Criteria.btw(BlotterFilter.CATEGORY_LEFT, String.valueOf(c.left), String.valueOf(c.right));
	}
}

