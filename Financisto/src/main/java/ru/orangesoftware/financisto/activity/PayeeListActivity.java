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

import java.util.List;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.blotter.BlotterFilter;
import ru.orangesoftware.financisto.filter.Criteria;
import ru.orangesoftware.financisto.model.Payee;

public class PayeeListActivity extends MyEntityListActivity<Payee> {

    public PayeeListActivity() {
        super(Payee.class);
    }

    @Override
    protected List<Payee> loadEntities() {
        return em.getAllPayeeList();
    }

    @Override
    protected String getContextMenuHeaderTitle(int position) {
        return getString(R.string.payee);
    }

    @Override
    protected Criteria createBlotterCriteria(Payee p) {
        return Criteria.eq(BlotterFilter.PAYEE_ID, String.valueOf(p.id));
    }

    @Override
    protected void startActivity(long id, int requestCode) {
        PayeeActivity_.intent(this).id(id).startForResult(requestCode);
    }

}
