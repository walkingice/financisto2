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

import android.app.Activity;
import android.content.Intent;
import android.widget.CheckBox;
import android.widget.EditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.db.DatabaseHelper;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.model.ActiveMyEntity;
import ru.orangesoftware.financisto2.utils.PinProtection;

@EActivity
public abstract class MyEntityActivity<T extends ActiveMyEntity> extends Activity {
	
    @Bean
	protected DatabaseAdapter db;

    @Extra
    protected long id = -1;

    @ViewById(R.id.title)
    protected EditText title;
    @ViewById(R.id.isActive)
    protected CheckBox isActiveCheckBox;

    private T entity;

    protected abstract Class<T> getEntityClass();

    @AfterViews
	protected void afterViews() {
        Class<T> clazz = getEntityClass();
        if (id != -1) {
            entity = db.load(clazz, id);
            editEntity();
        } else {
            try {
                entity = clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
	}

    @Click(R.id.bOK)
    protected void onOK() {
        updateFromUI();
        long id = db.saveOrUpdate(entity);
        Intent intent = new Intent();
        intent.putExtra(DatabaseHelper.EntityColumns.ID, id);
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void updateFromUI() {
        entity.title = title.getText().toString();
        entity.isActive = isActiveCheckBox.isChecked();
    }

    @Click(R.id.bCancel)
    protected void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

	private void editEntity() {
		title.setText(entity.title);
        isActiveCheckBox.setChecked(entity.isActive);
	}

	@Override
	protected void onPause() {
		super.onPause();
		PinProtection.lock(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PinProtection.unlock(this);
	}

}
