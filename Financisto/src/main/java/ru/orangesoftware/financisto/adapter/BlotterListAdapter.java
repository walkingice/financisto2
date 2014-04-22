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
package ru.orangesoftware.financisto.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;
import org.androidannotations.annotations.res.DrawableRes;

import java.util.Date;
import java.util.HashMap;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.db.DatabaseHelper.BlotterColumns;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.model.CategoryEntity;
import ru.orangesoftware.financisto.model.Currency;
import ru.orangesoftware.financisto.model.TransactionStatus;
import ru.orangesoftware.financisto.recur.Recurrence;
import ru.orangesoftware.financisto.utils.CurrencyCache;
import ru.orangesoftware.financisto.utils.MyPreferences;
import ru.orangesoftware.financisto.utils.Utils;

import static ru.orangesoftware.financisto.model.Category.isSplit;
import static ru.orangesoftware.financisto.utils.TransactionTitleUtils.generateTransactionTitle;

@EBean
public class BlotterListAdapter extends ResourceCursorAdapter {

    private final Date dt = new Date();

    protected final StringBuilder sb = new StringBuilder();

    @Bean
    public Utils u;

    @Bean
    protected MyEntityManager em;

    @DrawableRes(R.drawable.ic_action_arrow_left_bottom)
    public Drawable icBlotterIncome;

    @DrawableRes(R.drawable.ic_action_arrow_right_top)
    public Drawable icBlotterExpense;

    @DrawableRes(R.drawable.ic_action_arrow_top_down)
    public Drawable icBlotterTransfer;

    @DrawableRes(R.drawable.ic_action_share)
    public Drawable icBlotterSplit;

    @DimensionPixelSizeRes(R.dimen.transaction_icon_padding)
    public int topPadding;

    private final int colors[];

    private boolean allChecked = true;
    private final HashMap<Long, Boolean> checkedItems = new HashMap<Long, Boolean>();

    public boolean showRunningBalance;

    public BlotterListAdapter(Context context) {
        super(context, R.layout.blotter_list_item, null, false);
        this.colors = initializeColors(context);
        this.showRunningBalance = MyPreferences.isShowRunningBalance(context);
    }

    public void initWithCursor(Cursor cursor) {
        changeCursor(cursor);
    }

    public void initWithItemLayout(int layoutId) {
        setViewResource(layoutId);
    }

    private int[] initializeColors(Context context) {
        Resources r = context.getResources();
        TransactionStatus[] statuses = TransactionStatus.values();
        int count = statuses.length;
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            colors[i] = r.getColor(statuses[i].colorId);
        }
        return colors;
    }

    protected boolean isShowRunningBalance() {
        return showRunningBalance;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        createHolder(view);
        return view;
    }

    protected void createHolder(View view) {
        BlotterViewHolder h = new BlotterViewHolder(view);
        view.setTag(h);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final BlotterViewHolder v = (BlotterViewHolder) view.getTag();
        bindView(v, context, cursor);
    }

    protected void bindView(final BlotterViewHolder v, Context context, Cursor cursor) {
        long toAccountId = cursor.getLong(BlotterColumns.to_account_id.ordinal());
        int isTemplate = cursor.getInt(BlotterColumns.is_template.ordinal());
        TextView noteView = isTemplate == 1 ? v.bottomView : v.centerView;
        if (toAccountId > 0) {
            v.topView.setText(R.string.transfer);

            String fromAccountTitle = cursor.getString(BlotterColumns.from_account_title.ordinal());
            String toAccountTitle = cursor.getString(BlotterColumns.to_account_title.ordinal());
            u.setTransferTitleText(noteView, fromAccountTitle, toAccountTitle);

            long fromCurrencyId = cursor.getLong(BlotterColumns.from_account_currency_id.ordinal());
            Currency fromCurrency = CurrencyCache.getCurrency(em, fromCurrencyId);
            long toCurrencyId = cursor.getLong(BlotterColumns.to_account_currency_id.ordinal());
            Currency toCurrency = CurrencyCache.getCurrency(em, toCurrencyId);

            long fromAmount = cursor.getLong(BlotterColumns.from_amount.ordinal());
            long toAmount = cursor.getLong(BlotterColumns.to_amount.ordinal());
            long fromBalance = cursor.getLong(BlotterColumns.from_account_balance.ordinal());
            long toBalance = cursor.getLong(BlotterColumns.to_account_balance.ordinal());
            u.setTransferAmountText(v.rightView, fromCurrency, fromAmount, toCurrency, toAmount);
            if (v.rightCenterView != null) {
                u.setTransferBalanceText(v.rightCenterView, fromCurrency, fromBalance, toCurrency, toBalance);
            }
            v.iconView.setImageDrawable(icBlotterTransfer);
            v.iconView.setColorFilter(u.transferColor);
        } else {
            String fromAccountTitle = cursor.getString(BlotterColumns.from_account_title.ordinal());
            v.topView.setText(fromAccountTitle);
            setTransactionTitleText(cursor, noteView);
            sb.setLength(0);
            long fromCurrencyId = cursor.getLong(BlotterColumns.from_account_currency_id.ordinal());
            Currency fromCurrency = CurrencyCache.getCurrency(em, fromCurrencyId);
            long amount = cursor.getLong(BlotterColumns.from_amount.ordinal());
            long originalCurrencyId = cursor.getLong(BlotterColumns.original_currency_id.ordinal());
            if (originalCurrencyId > 0) {
                Currency originalCurrency = CurrencyCache.getCurrency(em, originalCurrencyId);
                long originalAmount = cursor.getLong(BlotterColumns.original_from_amount.ordinal());
                u.setAmountText(sb, v.rightView, originalCurrency, originalAmount, fromCurrency, amount, true);
            } else {
                u.setAmountText(sb, v.rightView, fromCurrency, amount, true);
            }
            long categoryId = cursor.getLong(BlotterColumns.category_id.ordinal());
            if (isSplit(categoryId)) {
                v.iconView.setImageDrawable(icBlotterSplit);
                v.iconView.setColorFilter(u.splitColor);
            } else if (amount == 0) {
                int categoryType = cursor.getInt(BlotterColumns.category_type.ordinal());
                if (categoryType == CategoryEntity.TYPE_INCOME) {
                    v.iconView.setImageDrawable(icBlotterIncome);
                    v.iconView.setColorFilter(u.positiveColor);
                } else if (categoryType == CategoryEntity.TYPE_EXPENSE) {
                    v.iconView.setImageDrawable(icBlotterExpense);
                    v.iconView.setColorFilter(u.negativeColor);
                }
            } else {
                if (amount > 0) {
                    v.iconView.setImageDrawable(icBlotterIncome);
                    v.iconView.setColorFilter(u.positiveColor);
                } else if (amount < 0) {
                    v.iconView.setImageDrawable(icBlotterExpense);
                    v.iconView.setColorFilter(u.negativeColor);
                }
            }
            if (v.rightCenterView != null) {
                long balance = cursor.getLong(BlotterColumns.from_account_balance.ordinal());
                v.rightCenterView.setText(Utils.amountToString(fromCurrency, balance, false));
            }
        }
        if (isTemplate == 1) {
            String templateName = cursor.getString(BlotterColumns.template_name.ordinal());
            v.centerView.setText(templateName);
        } else {
            String recurrence = cursor.getString(BlotterColumns.recurrence.ordinal());
            if (isTemplate == 2 && recurrence != null) {
                Recurrence r = Recurrence.parse(recurrence);
                v.bottomView.setText(r.toInfoString(context));
            } else {
                setIndicatorColor(v, cursor);
                long date = cursor.getLong(BlotterColumns.datetime.ordinal());
                dt.setTime(date);
                v.bottomView.setText(DateUtils.formatDateTime(context, dt.getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH));

                if (isTemplate == 0 && date > System.currentTimeMillis()) {
                    u.setFutureTextColor(v.bottomView);
                } else {
                }
            }
        }
        removeRightCenterViewIfNeeded(v);
        if (v.checkBox != null) {
            final long id = cursor.getLong(BlotterColumns._id.ordinal());
            v.checkBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    updateCheckedState(id, allChecked ^ v.checkBox.isChecked());
                }
            });
            boolean isChecked = getCheckedState(id);
            v.checkBox.setChecked(isChecked);
        }
    }

    private void setTransactionTitleText(Cursor cursor, TextView noteView) {
        sb.setLength(0);
        String payee = cursor.getString(BlotterColumns.payee.ordinal());
        String note = cursor.getString(BlotterColumns.note.ordinal());
        long locationId = cursor.getLong(BlotterColumns.location_id.ordinal());
        String location = getLocationTitle(cursor, locationId);
        long categoryId = cursor.getLong(BlotterColumns.category_id.ordinal());
        String category = getCategoryTitle(cursor, categoryId);
        String text = generateTransactionTitle(sb, payee, note, location, categoryId, category);
        noteView.setText(text);
    }

    private String getCategoryTitle(Cursor cursor, long categoryId) {
        String category = "";
        if (categoryId != 0) {
            category = cursor.getString(BlotterColumns.category_title.ordinal());
        }
        return category;
    }

    private String getLocationTitle(Cursor cursor, long locationId) {
        String location = "";
        if (locationId > 0) {
            location = cursor.getString(BlotterColumns.location.ordinal());
        }
        return location;
    }

    protected void removeRightCenterViewIfNeeded(BlotterViewHolder v) {
        if (v.rightCenterView != null && !isShowRunningBalance()) {
            v.rightCenterView.setVisibility(View.GONE);
            v.iconView.setPadding(0, topPadding, 0, 0);
        }
    }

    protected void setIndicatorColor(BlotterViewHolder v, Cursor cursor) {
        TransactionStatus status = TransactionStatus.valueOf(cursor.getString(BlotterColumns.status.ordinal()));
        v.indicator.setBackgroundColor(colors[status.ordinal()]);
    }

    public boolean getCheckedState(long id) {
        return checkedItems.get(id) != null ? !allChecked : allChecked;
    }

    private void updateCheckedState(long id, boolean checked) {
        if (checked) {
            checkedItems.put(id, true);
        } else {
            checkedItems.remove(id);
        }
    }

    public int getCheckedCount() {
        return allChecked ? getCount() - checkedItems.size() : checkedItems.size();
    }

    public void checkAll() {
        allChecked = true;
        checkedItems.clear();
        notifyDataSetInvalidated();
    }

    public void uncheckAll() {
        allChecked = false;
        checkedItems.clear();
        notifyDataSetInvalidated();
    }

    public static class BlotterViewHolder {

        public final RelativeLayout layout;
        public final TextView indicator;
        public final TextView topView;
        public final TextView centerView;
        public final TextView bottomView;
        public final TextView rightView;
        public final TextView rightCenterView;
        public final ImageView iconView;
        public final CheckBox checkBox;

        public BlotterViewHolder(View view) {
            layout = (RelativeLayout) view.findViewById(R.id.layout);
            indicator = (TextView) view.findViewById(R.id.indicator);
            topView = (TextView) view.findViewById(R.id.top);
            centerView = (TextView) view.findViewById(R.id.center);
            bottomView = (TextView) view.findViewById(R.id.bottom);
            rightView = (TextView) view.findViewById(R.id.right);
            rightCenterView = (TextView) view.findViewById(R.id.right_center);
            iconView = (ImageView) view.findViewById(R.id.right_top);
            checkBox = (CheckBox) view.findViewById(R.id.cb);
        }

    }

    public long[] getAllCheckedIds() {
        int checkedCount = getCheckedCount();
        long[] ids = new long[checkedCount];
        int k = 0;
        if (allChecked) {
            int count = getCount();
            boolean addAll = count == checkedCount;
            for (int i = 0; i < count; i++) {
                long id = getItemId(i);
                boolean checked = addAll || getCheckedState(id);
                if (checked) {
                    ids[k++] = id;
                }
            }
        } else {
            for (Long id : checkedItems.keySet()) {
                ids[k++] = id;
            }
        }
        return ids;
    }

}
