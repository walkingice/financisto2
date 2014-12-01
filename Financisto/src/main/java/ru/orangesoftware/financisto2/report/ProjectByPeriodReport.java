package ru.orangesoftware.financisto2.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.DatabaseHelper.TransactionColumns;
import ru.orangesoftware.financisto2.graph.Report2DChart;
import ru.orangesoftware.financisto2.model.Currency;
import ru.orangesoftware.financisto2.model.Project;
import ru.orangesoftware.financisto2.utils.MyPreferences;
import android.content.Context;

/**
 * 2D Chart Report to display monthly results by Projects.
 * @author Abdsandryk
 */
public class ProjectByPeriodReport extends Report2DChart {
	
	/**
	 * Default constructor.
	 * @param dbAdapter
	 * @param context
	 * @param periodLength
	 * @param currency
	 */
	public ProjectByPeriodReport(Context context, DatabaseAdapter db, int periodLength, Currency currency) {
		super(context, db, periodLength, currency);
	}
	
	/**
	 * Default constructor.
	 * @param context
	 * @param dbAdapter
	 * @param startPeriod
	 * @param periodLength
	 * @param currency
	 */
	public ProjectByPeriodReport(Context context, DatabaseAdapter db, Calendar startPeriod, int periodLength, Currency currency) {
		super(context, db, startPeriod, periodLength, currency);
	}

	/* (non-Javadoc)
	 * @see ru.orangesoftware.financisto2.graph.ReportGraphic2D#getFilterName()
	 */
	@Override
	public String getFilterName() {
		if (filterIds.size()>0) {
			long projectId = filterIds.get(currentFilterOrder);
			Project project = db.getProject(projectId);
			if (project!=null) {
				return project.getTitle();
			} else {
				return context.getString(R.string.no_project);
			}
		} else {
			// no project
			return context.getString(R.string.no_project);
		}
	}

	@Override
	public List<Report2DChart> getChildrenCharts() {
		return null;
	}

	/* (non-Javadoc)
	 * @see ru.orangesoftware.financisto2.graph.ReportGraphic2D#setFilterIds()
	 */
	@Override
	public void setFilterIds() {
		boolean includeNoProject = MyPreferences.includeNoFilterInReport(context);
		filterIds = new ArrayList<Long>();
		currentFilterOrder = 0;
		ArrayList<Project> projects = db.getAllProjectsList(includeNoProject);
		if (projects.size()>0) {
			Project p;
			for (int i=0; i<projects.size(); i++) {
				p = projects.get(i);
				filterIds.add(p.getId());
			}
		}
	}

	@Override
	protected void setColumnFilter() {
		columnFilter = TransactionColumns.project_id.name();
	}

	@Override
	public String getNoFilterMessage(Context context) {
		return context.getString(R.string.report_no_project);
	}
}
