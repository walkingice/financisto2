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

import java.util.List;
import java.util.Map;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.db.DatabaseHelper.CategoryViewColumns;
import ru.orangesoftware.financisto2.model.Category;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class CategoryListAdapter extends ArrayAdapter<Category> {
	
	private final DatabaseAdapter db;
    private final int layoutId;

	public CategoryListAdapter(DatabaseAdapter db, Context context, int layoutId, List<Category> categoryList) {
		super(context, layoutId, categoryList);
		this.db = db;
        this.layoutId = layoutId;
	}

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = (TextView) super.getView(position, convertView, parent);
        Category category = getItem(position);
        textView.setText(category.getTitle());
        return textView;
    }

}
