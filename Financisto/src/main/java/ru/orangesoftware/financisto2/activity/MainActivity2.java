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

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.adapter.EntityEnumAdapter;
import ru.orangesoftware.financisto2.bus.GreenRobotBus;
import ru.orangesoftware.financisto2.bus.InitialLoad;
import ru.orangesoftware.financisto2.dialog.WebViewDialog;
import ru.orangesoftware.financisto2.fragment.AccountListFragment_;
import ru.orangesoftware.financisto2.fragment.BlotterFragment_;
import ru.orangesoftware.financisto2.fragment.EmptyFragment_;
import ru.orangesoftware.financisto2.utils.EntityEnum;
import ru.orangesoftware.financisto2.utils.PinProtection;

@EActivity(R.layout.main2)
public class MainActivity2 extends FragmentActivity {

    @Bean
    protected GreenRobotBus bus;

    @ViewById(R.id.pager)
    protected ViewPager pager;

    @ViewById(R.id.drawer_layout)
    protected DrawerLayout drawerLayout;

    @ViewById(R.id.pager_strip)
    protected PagerTabStrip pagerTabStrip;

    @ViewById(R.id.left_drawer)
    protected ListView drawer;

    protected ActionBarDrawerToggle drawerToggle;

    @AfterInject
    protected void afterInject() {
        bus.post(new InitialLoad());
    }

    @AfterViews
    protected void afterViews() {
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(R.string.app_name);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(R.string.app_name);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        EntityEnumAdapter<DrawerItem> adapter = new EntityEnumAdapter<DrawerItem>(this, R.layout.drawer_list_item, DrawerItem.values());
        drawer.setAdapter(adapter);
        drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onDrawerItemClicked(position);
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        pagerTabStrip.setDrawFullUnderline(true);
        AppSectionsPagerAdapter pagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(pagerAdapter);
    }

    private void onDrawerItemClicked(int position) {
        drawer.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawer);
        DrawerItem item = DrawerItem.values()[position];
        item.onClick(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PinProtection.unlock(this);
        if (PinProtection.isUnlocked()) {
            WebViewDialog.checkVersionAndShowWhatsNewIfNeeded(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PinProtection.lock(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PinProtection.immediateLock(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private final Context context;

        public AppSectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return AccountListFragment_.builder().build();
                case 1:
                    return BlotterFragment_.builder().build();
                default:
                    return EmptyFragment_.builder().build();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getString(R.string.accounts);
                case 1:
                    return context.getString(R.string.blotter);
                case 2:
                    return context.getString(R.string.budgets);
                case 3:
                    return context.getString(R.string.reports);
            }
            return null;
        }
    }

    private enum DrawerItem implements EntityEnum {
        BACKUP_RESTORE(R.string.backup_restore, R.drawable.drawer_action_reload){
            @Override
            public void onClick(final Context context) {
                BackupRestoreListActivity_.intent(context).start();
            }
        },
        ENTITIES(R.string.entities, R.drawable.drawer_action_entities){
            @Override
            public void onClick(final Context context) {
                EntityListActivity_.intent(context).start();
            }
        },
        PREFERENCES(R.string.preferences, R.drawable.drawer_action_preferences){
            @Override
            public void onClick(final Context context) {
                PreferencesActivity_.intent(context).start();
            }
        },
        ;

        private final int titleId;
        private final int iconId;

        DrawerItem(int titleId, int iconId) {
            this.titleId = titleId;
            this.iconId = iconId;
        }

        @Override
        public int getTitleId() {
            return titleId;
        }

        @Override
        public int getIconId() {
            return iconId;
        }

        public abstract void onClick(Context context);
    }

}
