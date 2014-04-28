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
import ru.orangesoftware.financisto.bus.EditEntity;
import ru.orangesoftware.financisto.bus.GreenRobotBus;
import ru.orangesoftware.financisto.model.MyEntity;

public class EntityListAdapter<T extends MyEntity> extends BaseAdapter {
	
	private final LayoutInflater inflater;
    private final GreenRobotBus bus;
	
	private List<T> entities;
	
	public EntityListAdapter(GreenRobotBus bus, Context context, List<T> entities) {
        this.bus = bus;
		this.entities = entities;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return entities.size();
	}

	@Override
	public T getItem(int i) {
		return entities.get(i);
	}

	@Override
	public long getItemId(int i) {
		return getItem(i).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder v;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.entity_list_item, parent, false);
			v = Holder.createAndTag(convertView);
		} else {
			v = (Holder)convertView.getTag();
		}
		final MyEntity e = getItem(position);
		v.label.setText(e.title);
        v.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus.post(new EditEntity(e.getId()));
            }
        });
        v.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bus.post(new DeleteEntity(e.getId()));
            }
        });
		return convertView;
	}

    private static class Holder {
        public TextView label;
        public ImageView edit;
        public ImageView delete;

        public static Holder createAndTag(View v) {
            Holder holder = new Holder();
            holder.label = (TextView) v.findViewById(R.id.label);
            holder.edit = (ImageView) v.findViewById(R.id.edit);
            holder.delete = (ImageView) v.findViewById(R.id.delete);
            v.setTag(holder);
            return holder;
        }

    }


}
