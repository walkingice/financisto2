package ru.orangesoftware.financisto2.report;

import android.content.Context;
import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.db.DatabaseHelper.TransactionColumns;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.graph.Report2DChart;
import ru.orangesoftware.financisto2.model.Currency;
import ru.orangesoftware.financisto2.model.Payee;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 2D Chart Report to display monthly results by Payees.
 * @author Denis Solonenko
 */
public class PayeeByPeriodReport extends Report2DChart {

	public PayeeByPeriodReport(Context context, DatabaseAdapter db, Calendar startPeriod, int periodLength, Currency currency) {
		super(context, db, startPeriod, periodLength, currency);
	}

	@Override
	public String getFilterName() {
		if (filterIds.size()>0) {
			long payeeId = filterIds.get(currentFilterOrder);
			Payee payee = db.get(Payee.class, payeeId);
			if (payee != null) {
				return payee.getTitle();
			} else {
				return context.getString(R.string.no_payee);
			}
		} else {
			// no payee
			return context.getString(R.string.no_payee);
		}
	}

	@Override
	public List<Report2DChart> getChildrenCharts() {
		return null;
	}

	@Override
	public void setFilterIds() {
		filterIds = new ArrayList<Long>();
		currentFilterOrder = 0;
		List<Payee> payees = db.getAllPayeeList();
		if (payees.size() > 0) {
            for (Payee p : payees) {
                filterIds.add(p.getId());
            }
		}
	}

	@Override
	protected void setColumnFilter() {
		columnFilter = TransactionColumns.payee_id.name();
	}

	@Override
	public String getNoFilterMessage(Context context) {
		return context.getString(R.string.report_no_payee);
	}
}
