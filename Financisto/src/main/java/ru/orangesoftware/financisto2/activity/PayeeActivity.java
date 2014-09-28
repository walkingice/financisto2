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

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.model.Payee;

@EActivity(R.layout.project)
public class PayeeActivity extends MyEntityActivity<Payee> {

    @Override
    protected Class<Payee> getEntityClass() {
        return Payee.class;
    }

}
