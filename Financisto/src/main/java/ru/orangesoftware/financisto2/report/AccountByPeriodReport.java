package ru.orangesoftware.financisto2.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.DatabaseHelper.TransactionColumns;
import ru.orangesoftware.financisto2.graph.Report2DChart;
import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.Currency;
import android.content.Context;

/**
 * 2D Chart Report to display monthly account results.
 * @author Abdsandryk
 */
public class AccountByPeriodReport extends Report2DChart {
	
	/**
	 * Default constructor.
	 * @param dbAdapter
	 * @param context
	 * @param periodLength
	 * @param currency
	 */
	public AccountByPeriodReport(Context context, DatabaseAdapter db, int periodLength, Currency currency) {
		super(context, db, periodLength, currency);
	}
	
	/**
	 * Default constructor.
	 * @param dbAdapter
	 * @param context
	 * @param periodLength
	 * @param currency
	 */
	public AccountByPeriodReport(Context context, DatabaseAdapter db, Calendar startPeriod, int periodLength, Currency currency) {
		super(context, db, startPeriod, periodLength, currency);
	}

	/* (non-Javadoc)
	 * @see ru.orangesoftware.financisto2.graph.ReportGraphic2D#getChildrenGraphics()
	 */
	@Override
	public List<Report2DChart> getChildrenCharts() {
		return null;
	}

	/* (non-Javadoc)
	 * @see ru.orangesoftware.financisto2.graph.ReportGraphic2D#getFilterName()
	 */
	@Override
	public String getFilterName() {
		if (filterIds.size()>0) {
			long accountId = filterIds.get(currentFilterOrder);
			Account a = db.getAccount(accountId);
			if (a != null) {
				return a.title;
			} else {
				return context.getString(R.string.no_account);
			}
		} else {
			// no category
			return context.getString(R.string.no_account);
		}
	}

	/* (non-Javadoc)
	 * @see ru.orangesoftware.financisto2.graph.ReportGraphic2D#setFilterIds()
	 */
	@Override
	public void setFilterIds() {
		filterIds = new ArrayList<Long>();
		currentFilterOrder = 0;
		List<Account> accounts = db.getAllAccountsList();
		if (accounts.size() > 0) {
			Account a;
			for (int i=0; i<accounts.size(); i++) {
				a = accounts.get(i);
				filterIds.add(a.id);
			}
		}
	}

	@Override
	protected void setColumnFilter() {
		columnFilter = TransactionColumns.from_account_id.name();
	}
	
	@Override
	public String getNoFilterMessage(Context context) {
		return context.getString(R.string.report_no_account);
	}

}
