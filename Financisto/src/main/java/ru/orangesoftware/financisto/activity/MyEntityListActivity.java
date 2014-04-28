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
package ru.orangesoftware.financisto.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.adapter.EntityListAdapter;
import ru.orangesoftware.financisto.bus.DeleteEntity;
import ru.orangesoftware.financisto.bus.EditEntity;
import ru.orangesoftware.financisto.bus.GreenRobotBus;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.filter.Criteria;
import ru.orangesoftware.financisto.model.ActiveMyEntity;

@EActivity
@OptionsMenu(R.menu.entity_list_menu)
public abstract class MyEntityListActivity<T extends ActiveMyEntity> extends ListActivity {

    private static final int NEW_ENTITY_REQUEST = 1;
    private static final int EDIT_ENTITY_REQUEST = 2;

    @Bean
    protected MyEntityManager em;

    @Bean
    protected GreenRobotBus bus;

    @ViewById(android.R.id.empty)
    protected TextView emptyView;

    private final Class<T> clazz;

    public MyEntityListActivity(Class<T> clazz) {
        this.clazz = clazz;
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entity_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @ItemClick(android.R.id.list)
    protected void onItemClick(int position) {
        Toast.makeText(this, getListAdapter().getItem(position).toString(), Toast.LENGTH_SHORT).show();
    }

    @AfterViews
    protected void internalOnCreate() {
        emptyView.setText(getEmptyListTextResId());
        reload();
    }

    @OptionsItem(android.R.id.home)
    public void onHome() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @OptionsItem(R.id.menu_add)
    protected void addItem() {
        startActivity(-1, NEW_ENTITY_REQUEST);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(EditEntity event) {
        editItem(event.id);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final DeleteEntity event) {
        final long id = event.id;
        T e = em.load(clazz, id);
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.delete_confirm, e.getTitle()))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(id);
                    }
                })
                .show();
    }

    public void editItem(long id) {
        startActivity(id, EDIT_ENTITY_REQUEST);
    }

    protected void deleteItem(long id) {
        em.delete(clazz, id);
        reload();
    }

    protected void viewItem(long id) {
        T e = em.load(clazz, id);
        Intent intent = new Intent(this, BlotterActivity.class);
        Criteria blotterFilter = createBlotterCriteria(e);
        blotterFilter.toIntent(e.title, intent);
        startActivity(intent);
    }

    public void reload() {
        List<T> entities = loadEntities();
        EntityListAdapter<T> listAdapter = new EntityListAdapter<T>(bus, this, entities);
        setListAdapter(listAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            reload();
        }
    }

    protected abstract int getEmptyListTextResId();

    protected abstract List<T> loadEntities();

    protected abstract void startActivity(long id, int requestCode);

    protected abstract Criteria createBlotterCriteria(T e);

}
