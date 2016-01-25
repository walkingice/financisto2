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
package ru.orangesoftware.financisto2.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androidannotations.annotations.res.DimensionPixelSizeRes;
import org.androidannotations.annotations.res.DrawableRes;

import java.util.Date;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.db.DatabaseHelper.BlotterColumns;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.Currency;
import ru.orangesoftware.financisto2.model.TransactionStatus;
import ru.orangesoftware.financisto2.recur.Recurrence;
import ru.orangesoftware.financisto2.utils.CurrencyCache;
import ru.orangesoftware.financisto2.utils.MyPreferences;
import ru.orangesoftware.financisto2.utils.Utils;
import ru.orangesoftware.financisto2.utils.Utils_;

import static ru.orangesoftware.financisto2.model.Category.isSplit;
import static ru.orangesoftware.financisto2.utils.TransactionTitleUtils.generateTransactionTitle;

public class BlotterListAdapter2 extends CursorRecyclerViewAdapter<BlotterListAdapter2.BlotterViewHolder> {

    private final Date dt = new Date();

    protected final StringBuilder sb = new StringBuilder();

    public Utils u;
    public Drawable icBlotterIncome;
    public Drawable icBlotterExpense;
    public Drawable icBlotterTransfer;
    public Drawable icBlotterSplit;
    public int topPadding;

    private int colors[];

    private boolean allChecked = true;
    private final LongSparseArray<Boolean> checkedItems = new LongSparseArray<Boolean>();

    public boolean showRunningBalance;

    protected final Context context;

    public BlotterListAdapter2(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
        this.u = Utils_.getInstance_(context);
        initializeResources(context);
        this.showRunningBalance = MyPreferences.isShowRunningBalance(context);
    }

    private void initializeResources(Context context) {
        Resources r = context.getResources();
        TransactionStatus[] statuses = TransactionStatus.values();
        int count = statuses.length;
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            colors[i] = r.getColor(statuses[i].colorId);
        }
        this.colors = colors;
        this.icBlotterIncome = context.getDrawable(R.drawable.ic_action_arrow_left_bottom);
        this.icBlotterExpense = context.getDrawable(R.drawable.ic_action_arrow_right_top);
        this.icBlotterTransfer = context.getDrawable(R.drawable.ic_action_arrow_top_down);
        this.icBlotterSplit = context.getDrawable(R.drawable.ic_action_share);
        this.topPadding = r.getDimensionPixelSize(R.dimen.transaction_icon_padding);
    }

    @Override
    public BlotterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.blotter_list_item, parent, false);
        return new BlotterViewHolder(v);
    }

    protected boolean isShowRunningBalance() {
        return showRunningBalance;
    }

    @Override
    public void onBindViewHolder(final BlotterViewHolder v, Cursor cursor) {
        long toAccountId = cursor.getLong(BlotterColumns.to_account_id.ordinal());
        int isTemplate = cursor.getInt(BlotterColumns.is_template.ordinal());
        TextView noteView = isTemplate == 1 ? v.bottomView : v.centerView;
        if (toAccountId > 0) {
            v.topView.setText(R.string.transfer);

            String fromAccountTitle = cursor.getString(BlotterColumns.from_account_title.ordinal());
            String toAccountTitle = cursor.getString(BlotterColumns.to_account_title.ordinal());
            u.setTransferTitleText(noteView, fromAccountTitle, toAccountTitle);

            long fromCurrencyId = cursor.getLong(BlotterColumns.from_account_currency_id.ordinal());
            Currency fromCurrency = CurrencyCache.getCurrencyOrEmpty(fromCurrencyId);
            long toCurrencyId = cursor.getLong(BlotterColumns.to_account_currency_id.ordinal());
            Currency toCurrency = CurrencyCache.getCurrencyOrEmpty(toCurrencyId);

            long fromAmount = cursor.getLong(BlotterColumns.from_amount.ordinal());
            long toAmount = cursor.getLong(BlotterColumns.to_amount.ordinal());
            long fromBalance = cursor.getLong(BlotterColumns.from_account_balance.ordinal());
            long toBalance = cursor.getLong(BlotterColumns.to_account_balance.ordinal());
            u.setTransferAmountText(v.rightCenterView, fromCurrency, fromAmount, toCurrency, toAmount);
            if (v.rightView != null) {
                u.setTransferBalanceText(v.rightView, fromCurrency, fromBalance, toCurrency, toBalance);
            }
            v.iconView.setImageDrawable(icBlotterTransfer);
            v.iconView.setColorFilter(u.transferColor);
        } else {
            String fromAccountTitle = cursor.getString(BlotterColumns.from_account_title.ordinal());
            v.topView.setText(fromAccountTitle);
            setTransactionTitleText(cursor, noteView);
            sb.setLength(0);
            long fromCurrencyId = cursor.getLong(BlotterColumns.from_account_currency_id.ordinal());
            Currency fromCurrency = CurrencyCache.getCurrencyOrEmpty(fromCurrencyId);
            long amount = cursor.getLong(BlotterColumns.from_amount.ordinal());
            long originalCurrencyId = cursor.getLong(BlotterColumns.original_currency_id.ordinal());
            if (originalCurrencyId > 0) {
                Currency originalCurrency = CurrencyCache.getCurrencyOrEmpty(originalCurrencyId);
                long originalAmount = cursor.getLong(BlotterColumns.original_from_amount.ordinal());
                u.setAmountText(sb, v.rightCenterView, originalCurrency, originalAmount, fromCurrency, amount, true);
            } else {
                u.setAmountText(sb, v.rightCenterView, fromCurrency, amount, true);
            }
            long categoryId = cursor.getLong(BlotterColumns.category_id.ordinal());
            if (isSplit(categoryId)) {
                v.iconView.setImageDrawable(icBlotterSplit);
                v.iconView.setColorFilter(u.splitColor);
            } else if (amount == 0) {
                int categoryType = cursor.getInt(BlotterColumns.category_type.ordinal());
                if (categoryType == Category.TYPE_INCOME) {
                    v.iconView.setImageDrawable(icBlotterIncome);
                    v.iconView.setColorFilter(u.positiveColor);
                } else if (categoryType == Category.TYPE_EXPENSE) {
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
            if (v.rightView != null) {
                long balance = cursor.getLong(BlotterColumns.from_account_balance.ordinal());
                v.rightView.setText(Utils.amountToString(fromCurrency, balance, false));
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
        removeRunningBalanceViewIfNeeded(v);
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
        long categoryId = cursor.getLong(BlotterColumns.category_id.ordinal());
        String category = getCategoryTitle(cursor, categoryId);
        String text = generateTransactionTitle(sb, payee, note, categoryId, category);
        noteView.setText(text);
    }

    private String getCategoryTitle(Cursor cursor, long categoryId) {
        String category = "";
        if (categoryId != 0) {
            category = cursor.getString(BlotterColumns.category_title.ordinal());
        }
        return category;
    }

    protected void removeRunningBalanceViewIfNeeded(BlotterViewHolder v) {
        if (v.rightView != null && !isShowRunningBalance()) {
            v.rightView.setVisibility(View.GONE);
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

//    public int getCheckedCount() {
//        return allChecked ? getCount() - checkedItems.size() : checkedItems.size();
//    }
//
//    public void checkAll() {
//        allChecked = true;
//        checkedItems.clear();
//        notifyDataSetInvalidated();
//    }
//
//    public void uncheckAll() {
//        allChecked = false;
//        checkedItems.clear();
//        notifyDataSetInvalidated();
//    }
//
    public static class BlotterViewHolder extends RecyclerView.ViewHolder {

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
            super(view);
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

    /*public long[] getAllCheckedIds() {
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
    }*/

}
