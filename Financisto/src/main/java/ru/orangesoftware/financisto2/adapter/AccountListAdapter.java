/*******************************************************************************
 * Copyright (c) 2016 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * <p/>
 * Contributors:
 * Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.datetime.DateUtils;
import ru.orangesoftware.financisto2.model.Account;
import ru.orangesoftware.financisto2.model.AccountType;
import ru.orangesoftware.financisto2.model.CardIssuer;
import ru.orangesoftware.financisto2.model.ElectronicPaymentType;
import ru.orangesoftware.financisto2.utils.MyPreferences;
import ru.orangesoftware.financisto2.utils.Utils;
import ru.orangesoftware.financisto2.utils.Utils_;

public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.AccountViewHolder> {

    private final Context context;
    private final Utils u;
    private final DateFormat df;
    private boolean isShowAccountLastTransactionDate;

    private final List<Account> accounts;

    public AccountListAdapter(Context context, List<Account> accounts) {
        this.context = context;
        this.u = Utils_.getInstance_(context);
        this.df = DateUtils.getMediumDateFormat(context);
        this.isShowAccountLastTransactionDate = MyPreferences.isShowAccountLastTransactionDate(context);
        this.accounts = accounts;
        setHasStableIds(true);
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_list_item, parent, false);
        return new AccountViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AccountViewHolder v, int position) {
        Account a = getAccountAt(position);
        AccountType type = AccountType.valueOf(a.type);
        setAccountIcon(v, a, type);
        setAccountTitle(v, a, type);
        setAccountDate(v, a);
        setAccountAmount(v, a, type);
    }

    protected void setAccountIcon(AccountViewHolder v, Account a, AccountType type) {
        if (type.isCard && a.cardIssuer != null) {
            CardIssuer cardIssuer = CardIssuer.valueOf(a.cardIssuer);
            v.iconView.setImageResource(cardIssuer.iconId);
        } else if (type.isElectronic && a.cardIssuer != null) {
            ElectronicPaymentType paymentType = ElectronicPaymentType.valueOf(a.cardIssuer);
            v.iconView.setImageResource(paymentType.iconId);
        } else {
            v.iconView.setImageResource(type.iconId);
        }
        if (a.isActive) {
            v.iconView.getDrawable().mutate().setAlpha(0xFF);
            v.iconOverView.setVisibility(View.INVISIBLE);
        } else {
            v.iconView.getDrawable().mutate().setAlpha(0x77);
            v.iconOverView.setVisibility(View.VISIBLE);
        }
    }

    protected void setAccountTitle(AccountViewHolder v, Account a, AccountType type) {
        StringBuilder sb = new StringBuilder();
        if (!Utils.isEmpty(a.issuer)) {
            sb.append(a.issuer);
        }
        if (!Utils.isEmpty(a.number)) {
            sb.append(" #").append(a.number);
        }
        if (sb.length() == 0) {
            sb.append(context.getString(type.titleId));
        }
        v.topView.setText(sb.toString());
        v.centerView.setText(a.title);
    }

    protected void setAccountDate(AccountViewHolder v, Account a) {
        long date = a.creationDate;
        if (isShowAccountLastTransactionDate && a.lastTransactionDate > 0) {
            date = a.lastTransactionDate;
        }
        v.bottomView.setText(df.format(new Date(date)));
    }

    protected void setAccountAmount(AccountViewHolder v, Account a, AccountType type) {
        long amount = a.totalAmount;
        if (type == AccountType.CREDIT_CARD && a.limitAmount != 0) {
            long limitAmount = Math.abs(a.limitAmount);
            long balance = limitAmount + amount;
            long balancePercentage = 10000 * balance / limitAmount;
            u.setAmountText(v.rightCenterView, a.currency, amount, false);
            u.setAmountText(v.rightView, a.currency, balance, false);
            v.rightCenterView.setVisibility(View.VISIBLE);
            v.progressBar.setMax(10000);
            v.progressBar.setProgress((int) balancePercentage);
            v.progressBar.setVisibility(View.VISIBLE);
        } else {
            u.setAmountText(v.rightCenterView, a.currency, amount, false);
            v.rightView.setVisibility(View.GONE);
            v.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public Account getAccountAt(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getAccountAt(position).getId();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {

        public final ImageView iconView;
        public final ImageView iconOverView;
        public final TextView topView;
        public final TextView centerView;
        public final TextView bottomView;
        public final TextView rightView;
        public final TextView rightCenterView;
        public final ProgressBar progressBar;

        public AccountViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.icon);
            iconOverView = (ImageView) view.findViewById(R.id.active_icon);
            topView = (TextView) view.findViewById(R.id.top);
            centerView = (TextView) view.findViewById(R.id.center);
            bottomView = (TextView) view.findViewById(R.id.bottom);
            rightView = (TextView) view.findViewById(R.id.right);
            rightCenterView = (TextView) view.findViewById(R.id.right_center);
            progressBar = (ProgressBar) view.findViewById(R.id.progress);
        }

    }


}
