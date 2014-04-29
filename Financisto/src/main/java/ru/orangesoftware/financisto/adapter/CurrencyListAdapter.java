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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.bus.DeleteEntity;
import ru.orangesoftware.financisto.bus.GreenRobotBus;
import ru.orangesoftware.financisto.model.Currency;
import ru.orangesoftware.financisto.utils.Utils;

public class CurrencyListAdapter extends BaseAdapter {

    private final GreenRobotBus bus;
    private final List<Currency> currencies;

	public CurrencyListAdapter(GreenRobotBus bus, List<Currency> currencies) {
        this.bus = bus;
        this.currencies = currencies;
    }

    @Override
    public int getCount() {
        return currencies.size();
    }

    @Override
    public Currency getItem(int position) {
        return currencies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CurrencyViewHolder h;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.currency_list_item, parent, false);
            h = CurrencyViewHolder.createAndTag(convertView);
        } else {
            h = (CurrencyViewHolder)convertView.getTag();
        }
        final Currency c = getItem(position);
        h.title.setText(c.title);
        h.name.setText(c.name);
        h.format.setText(Utils.amountToString(c, 100000));
        if (c.isDefault) {
            h.icon.setImageResource(R.drawable.ic_home_currency);
        } else {
            h.icon.setImageDrawable(null);
        }
        h.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus.post(new DeleteEntity(c.id));
            }
        });
        return convertView;
    }

    private static class CurrencyViewHolder {

        public TextView title;
        public TextView name;
        public TextView format;
        public ImageView icon;
        public ImageView delete;

        public static CurrencyViewHolder createAndTag(View view) {
            CurrencyViewHolder views = new CurrencyViewHolder();
            views.title = (TextView)view.findViewById(R.id.title);
            views.name = (TextView)view.findViewById(R.id.name);
            views.format = (TextView)view.findViewById(R.id.format);
            views.icon = (ImageView) view.findViewById(R.id.icon);
            views.delete = (ImageView) view.findViewById(R.id.delete);
            view.setTag(views);
            return views;
        }
    }


}
