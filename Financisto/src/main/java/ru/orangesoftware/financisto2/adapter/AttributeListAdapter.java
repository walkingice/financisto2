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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.bus.DeleteEntity;
import ru.orangesoftware.financisto2.bus.GreenRobotBus;
import ru.orangesoftware.financisto2.model.Attribute;

public class AttributeListAdapter extends BaseAdapter {

    private final GreenRobotBus bus;
	private final String[] attributeTypes;
    private final List<Attribute> attributes;

	public AttributeListAdapter(Context context, GreenRobotBus bus, List<Attribute> attributes) {
        this.bus = bus;
		this.attributeTypes = context.getResources().getStringArray(R.array.attribute_types);
        this.attributes = attributes;
	}

    @Override
    public int getCount() {
        return attributes.size();
    }

    @Override
    public Attribute getItem(int position) {
        return attributes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AttributeViewHolder h;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.attribute_list_item, parent, false);
            h = AttributeViewHolder.createAndTag(convertView);
        } else {
            h = (AttributeViewHolder)convertView.getTag();
        }
        final Attribute a = getItem(position);
        h.name.setText(a.name);
        h.type.setText(attributeTypes[a.type-1]);
        String defaultValue = a.getDefaultValue();
        if (defaultValue != null) {
            h.defaultValue.setVisibility(View.VISIBLE);
            h.defaultValue.setText(defaultValue);
        } else {
            h.defaultValue.setVisibility(View.INVISIBLE);
        }
        h.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus.post(new DeleteEntity(a.id));
            }
        });
        return convertView;
    }

    private static class AttributeViewHolder {

        public TextView name;
        public TextView type;
        public TextView defaultValue;
        public ImageView delete;

        public static AttributeViewHolder createAndTag(View view) {
            AttributeViewHolder views = new AttributeViewHolder();
            views.name = (TextView)view.findViewById(R.id.name);
            views.type = (TextView)view.findViewById(R.id.type);
            views.defaultValue = (TextView)view.findViewById(R.id.default_value);
            views.delete = (ImageView) view.findViewById(R.id.delete);
            view.setTag(views);
            return views;
        }
    }

}
