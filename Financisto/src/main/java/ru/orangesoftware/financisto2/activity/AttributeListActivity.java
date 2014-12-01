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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.List;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.adapter.AttributeListAdapter;
import ru.orangesoftware.financisto2.bus.DeleteEntity;
import ru.orangesoftware.financisto2.bus.GreenRobotBus;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.model.Attribute;

@EActivity(R.layout.attributes_list)
@OptionsMenu(R.menu.attributes_list_menu)
public class AttributeListActivity extends ListActivity {

    @Bean
    protected DatabaseAdapter db;

    @Bean
    protected GreenRobotBus bus;

    @Override
    protected void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @AfterViews
    protected void afterViews() {
        reload();
    }

    private void reload() {
        List<Attribute> attributes = db.getAllAttributes();
        AttributeListAdapter adapter = new AttributeListAdapter(this, bus, attributes);
        setListAdapter(adapter);
    }

    @OptionsItem(R.id.menu_add)
	protected void addItem() {
        AttributeActivity_.intent(this).startForResult(1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			reload();
		}
	}

    @SuppressWarnings("unused")
	public void onEventMainThread(final DeleteEntity event) {
		new AlertDialog.Builder(this)
			.setTitle(R.string.delete)
			.setMessage(R.string.attribute_delete_alert)
			.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					db.deleteAttribute(event.id);
					reload();
				}				
			})
			.setNegativeButton(R.string.cancel, null)
			.show();		
	}

    @ItemClick(android.R.id.list)
	public void editItem(Attribute attribute) {
        AttributeActivity_.intent(this).attributeId(attribute.id).startForResult(2);
	}

}
