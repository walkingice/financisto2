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
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.blotter.BlotterFilter;
import ru.orangesoftware.financisto2.datetime.DateUtils;
import ru.orangesoftware.financisto2.datetime.Period;
import ru.orangesoftware.financisto2.datetime.PeriodType;
import ru.orangesoftware.financisto2.filter.DateTimeCriteria;
import ru.orangesoftware.financisto2.filter.WhereFilter;

import java.text.DateFormat;
import java.util.Calendar;

import static ru.orangesoftware.financisto2.datetime.DateUtils.is24HourFormat;
import static ru.orangesoftware.financisto2.utils.EnumUtils.createSpinnerAdapter;

@EActivity(R.layout.date_filter)
@WindowFeature(Window.FEATURE_NO_TITLE)
public class DateFilterActivity extends Activity {
	
	public static final String EXTRA_FILTER_PERIOD_TYPE = "filter_period_type";
	public static final String EXTRA_FILTER_PERIOD_FROM = "filter_period_from";
	public static final String EXTRA_FILTER_PERIOD_TO = "filter_period_to";
	public static final String EXTRA_FILTER_DONT_SHOW_NO_FILTER = "filter_dont_show_no_filter";
    public static final String EXTRA_FILTER_SHOW_PLANNER = "filter_show_planner";

	private final Calendar cFrom = Calendar.getInstance(); 
	private final Calendar cTo = Calendar.getInstance();

    private DateFormat df;
    private PeriodType[] periods = PeriodType.allRegular();


    @ViewById(R.id.period)
    Spinner spinnerPeriodType;

    @ViewById(R.id.bPeriodFrom)
    Button buttonPeriodFrom;

    @ViewById(R.id.bPeriodTo)
	Button buttonPeriodTo;

    @ViewById(R.id.bNoFilter)
    ImageButton bNoFilter;

    @AfterViews
	protected void init() {

		df = DateUtils.getShortDateFormat(this);

        Intent intent = getIntent();
        setCorrectPeriods(intent);
        createPeriodsSpinner();

		if (intent == null) {
			reset();
		} else {
			WhereFilter filter = WhereFilter.fromIntent(intent);
			DateTimeCriteria c = (DateTimeCriteria)filter.get(BlotterFilter.DATETIME);
			if (c != null) {
				if (c.getPeriod() == null || c.getPeriod().type == PeriodType.CUSTOM) {
					selectPeriod(c.getLongValue1(), c.getLongValue2());					
				} else {
					selectPeriod(c.getPeriod());
				}
				
			}
			if (intent.getBooleanExtra(EXTRA_FILTER_DONT_SHOW_NO_FILTER, false)) {
				bNoFilter.setVisibility(View.GONE);
			}
		}
	}

    @Click(R.id.bOK)
    protected void onOk() {
        Intent data = new Intent();
        PeriodType period = periods[spinnerPeriodType.getSelectedItemPosition()];
        data.putExtra(EXTRA_FILTER_PERIOD_TYPE, period.name());
        data.putExtra(EXTRA_FILTER_PERIOD_FROM, cFrom.getTimeInMillis());
        data.putExtra(EXTRA_FILTER_PERIOD_TO, cTo.getTimeInMillis());
        setResult(RESULT_OK, data);
        finish();
    }

    @Click(R.id.bCancel)
    protected void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Click(R.id.bNoFilter)
    protected void onNoFilter() {
        setResult(RESULT_FIRST_USER);
        finish();
    }

    @Click(R.id.bPeriodFrom)
    protected void showFromDialog() {
        showDialog(1);
    }

    @Click(R.id.bPeriodTo)
    protected void showToDialog() {
        showDialog(2);
    }

    private void setCorrectPeriods(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_FILTER_SHOW_PLANNER, false)) {
            periods = PeriodType.allPlanner();
        }
    }

    private void createPeriodsSpinner() {
        spinnerPeriodType.setAdapter(createSpinnerAdapter(this, periods));
        spinnerPeriodType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PeriodType period = periods[position];
                if (period == PeriodType.CUSTOM) {
                    selectCustom();
                } else {
                    selectPeriod(period);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void selectPeriod(Period p) {
		spinnerPeriodType.setSelection(indexOf(p.type));
	}

	private void selectPeriod(long from, long to) {
		cFrom.setTimeInMillis(from);
		cTo.setTimeInMillis(to);			
		spinnerPeriodType.setSelection(indexOf(PeriodType.CUSTOM));
	}

    private int indexOf(PeriodType type) {
        for (int i = 0; i < periods.length; i++) {
            if (periods[i] == type) {
                return i;
            }
        }
        return 0;
    }

    @Override
	protected Dialog onCreateDialog(final int id) {
		final Dialog d = new Dialog(this);
		d.setCancelable(true);
		d.setTitle(id == 1 ? R.string.period_from : R.string.period_to);
		d.setContentView(R.layout.filter_period_select);
		Button bOk = (Button)d.findViewById(R.id.bOK);
		bOk.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				setDialogResult(d, id == 1 ? cFrom : cTo);
				d.dismiss();
			}
		});		
		Button bCancel = (Button)d.findViewById(R.id.bCancel);
		bCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				d.cancel();
			}
		});
		return d;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		prepareDialog(dialog, id == 1 ? cFrom : cTo);
	}

	private void prepareDialog(Dialog dialog, Calendar c) {
		DatePicker dp = (DatePicker)dialog.findViewById(R.id.date);
		dp.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
		TimePicker tp = (TimePicker)dialog.findViewById(R.id.time);
        tp.setIs24HourView(is24HourFormat(this));
        tp.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
		tp.setCurrentMinute(c.get(Calendar.MINUTE));
	}

	private void setDialogResult(Dialog d, Calendar c) {
		DatePicker dp = (DatePicker)d.findViewById(R.id.date);
		c.set(Calendar.YEAR, dp.getYear());
		c.set(Calendar.MONTH, dp.getMonth());
		c.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
		TimePicker tp = (TimePicker)d.findViewById(R.id.time);
		c.set(Calendar.HOUR_OF_DAY, tp.getCurrentHour());
		c.set(Calendar.MINUTE, tp.getCurrentMinute());
		updateDate();
	}

	private void enableButtons() {
		buttonPeriodFrom.setEnabled(true);
		buttonPeriodTo.setEnabled(true);
	}

	private void disableButtons() {
		buttonPeriodFrom.setEnabled(false);
		buttonPeriodTo.setEnabled(false);
	}

	private void updateDate(Period p) {
		cFrom.setTimeInMillis(p.start);
		cTo.setTimeInMillis(p.end);
		updateDate();
	}
	
	private void updateDate() {
		buttonPeriodFrom.setText(df.format(cFrom.getTime()));
		buttonPeriodTo.setText(df.format(cTo.getTime()));
	}

    private void selectPeriod(PeriodType periodType) {
        disableButtons();
        updateDate(periodType.calculatePeriod());
    }

	protected void selectCustom() {
		updateDate();
		enableButtons();				
	}

	private void reset() {
		spinnerPeriodType.setSelection(0);		
	}

}
