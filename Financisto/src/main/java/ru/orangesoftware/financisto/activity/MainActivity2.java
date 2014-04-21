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

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;

import org.androidannotations.annotations.EActivity;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.fragment.AccountListFragment_;
import ru.orangesoftware.financisto.fragment.BlotterFragment_;

@EActivity
public class MainActivity2 extends FragmentActivity {

    private ViewPager pager;
    private AppSectionsPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);

        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);

        PagerTabStrip pagerTabStrip = (PagerTabStrip)findViewById(R.id.pager_strip);
        pagerTabStrip.setDrawFullUnderline(true);

        pagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), this);
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
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
                default:
                    return BlotterFragment_.builder().build();
            }
        }

        @Override
        public int getCount() {
            return 2;
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

}
