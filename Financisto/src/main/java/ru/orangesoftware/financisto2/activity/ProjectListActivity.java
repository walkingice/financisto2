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

import org.androidannotations.annotations.EActivity;

import java.util.ArrayList;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.blotter.BlotterFilter;
import ru.orangesoftware.financisto2.filter.Criteria;
import ru.orangesoftware.financisto2.model.Project;

@EActivity
public class ProjectListActivity extends MyEntityListActivity<Project> {

    public ProjectListActivity() {
        super(Project.class);
    }

    @Override
    protected ArrayList<Project> loadEntities() {
        return em.getAllProjectsList(false);
    }

    @Override
    protected int getEmptyListTextResId() {
        return R.string.no_projects;
    }

    @Override
    protected Criteria createBlotterCriteria(Project p) {
        return Criteria.eq(BlotterFilter.PROJECT_ID, String.valueOf(p.id));
    }

    @Override
    protected void deleteItem(long id) {
        em.deleteProject(id);
        reload();
    }

    @Override
    protected void startActivity(long id, int requestCode) {
        ProjectActivity_.intent(this).id(id).startForResult(requestCode);
    }

}
