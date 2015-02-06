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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.bus.DeleteEntity;
import ru.orangesoftware.financisto2.bus.EditEntity;
import ru.orangesoftware.financisto2.bus.GreenRobotBus;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTree;

public class CategoryListAdapter2 extends BaseAdapter {

	private final LayoutInflater inflater;
    private final GreenRobotBus bus;

	private CategoryTree categories;
	private Map<Long, String> attributes;

	private final ArrayList<Category> list = new ArrayList<Category>();
	private final HashSet<Long> state = new HashSet<Long>();
	
	private final Drawable expandedDrawable;
	private final Drawable collapsedDrawable;
    private final int incomeColor;
    private final int expenseColor;

    private final int levelPadding;

	public CategoryListAdapter2(Context context, GreenRobotBus bus, CategoryTree categories, Map<Long, String> attributes) {
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.bus = bus;
		this.categories = categories;
        this.attributes = attributes;
        Resources resources = context.getResources();
        this.expandedDrawable = resources.getDrawable(R.drawable.ic_action_expanded);
		this.collapsedDrawable = resources.getDrawable(R.drawable.ic_action_collapsed);
        this.incomeColor = resources.getColor(R.color.category_type_income);
        this.expenseColor = resources.getColor(R.color.category_type_expense);
        this.levelPadding = resources.getDimensionPixelSize(R.dimen.category_padding);
		recreatePlainList();
	}
	
	private void recreatePlainList() {
		list.clear();
		addCategories(categories.getRoot().children);
	}

	private void addCategories(List<Category> categories) {
		if (categories == null || categories.isEmpty()) {
			return;
		}
		for (Category c : categories) {
			list.add(c);
			if (state.contains(c.id)) {
				addCategories(c.children);
			}
		}
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Category getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).id;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder h;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.category_list_item2, parent, false);
			h = Holder.create(convertView);
		} else {
			h = (Holder)convertView.getTag();
		}
        final Category c = getItem(position);
		h.title.setText(Category.getTitle(c.title, c.level));
        int padding = levelPadding*c.level;
		if (c.hasChildren()) {
			h.span.setImageDrawable(state.contains(c.id) ? expandedDrawable : collapsedDrawable);
			h.span.setClickable(true);
			h.span.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onListItemClick(c.id);
                }
            });
            h.span.setPadding(padding, 0, 0, 0);
			h.span.setVisibility(View.VISIBLE);
            padding += collapsedDrawable.getMinimumWidth();
		} else {
            padding += levelPadding/2;
			h.span.setVisibility(View.GONE);
		}
        h.title.setPadding(padding, 0, 0, 0);
        h.label.setPadding(padding, 0, 0, 0);
		long id = c.id;
		if (attributes != null && attributes.containsKey(id)) {
			h.label.setText(attributes.get(id));
			h.label.setVisibility(View.VISIBLE);
		} else {
			h.label.setVisibility(View.GONE);
		}
        if (c.isIncome()) {
            h.indicator.setBackgroundColor(incomeColor);
        } else if (c.isExpense()) {
            h.indicator.setBackgroundColor(expenseColor);
        } else {
            h.indicator.setBackgroundColor(Color.WHITE);
        }
        h.edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bus.post(new EditEntity(c.getId()));
            }
        });
        h.delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bus.post(new DeleteEntity(c.getId()));
            }
        });
		return convertView;
	}
	
	public void onListItemClick(long id) {
		if (state.contains(id)) {
			state.remove(id);
		} else {
			state.add(id);
		}
		notifyDataSetChanged();
	}
	
	public void collapseAllCategories() {
		state.clear();
		notifyDataSetChanged();
	}

	public void expandAllCategories() {
		expandAllCategories(categories.getRoot().children);
		notifyDataSetChanged();
	}
	
	private void expandAllCategories(List<Category> categories) {
		if (categories == null || categories.isEmpty()) {
			return;
		}
		for (Category c : categories) {
			state.add(c.id);
			expandAllCategories(c.children);
		}
	}
	
	@Override
	public void notifyDataSetChanged() {
		recreatePlainList();
		super.notifyDataSetChanged();		
	}

	public void setCategories(CategoryTree categories) {
		this.categories = categories;
		recreatePlainList();
	}

	public void setAttributes(Map<Long, String> attributes) {
		this.attributes = attributes;	
	}

	private static class Holder {
		
        public TextView indicator;
		public ImageView span;
		public TextView title;
		public TextView label;
        public ImageView edit;
        public ImageView delete;

		public static Holder create(View convertView) {
			Holder h = new Holder();
            h.indicator = (TextView)convertView.findViewById(R.id.indicator);
			h.span = (ImageView)convertView.findViewById(R.id.span);
			h.title = (TextView)convertView.findViewById(R.id.line1);
			h.label = (TextView)convertView.findViewById(R.id.label);
            h.edit = (ImageView)convertView.findViewById(R.id.edit);
            h.delete = (ImageView)convertView.findViewById(R.id.delete);
			convertView.setTag(h);
			return h;
		}
		
	}
	
}
