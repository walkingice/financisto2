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

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.db.DatabaseHelper.AttributeColumns;
import ru.orangesoftware.financisto.db.MyEntityManager;
import ru.orangesoftware.financisto.model.Attribute;
import ru.orangesoftware.financisto.utils.PinProtection;
import ru.orangesoftware.financisto.utils.Utils;

@EActivity(R.layout.attribute)
public class AttributeActivity extends Activity implements OnItemSelectedListener {

    @Bean
    protected MyEntityManager em;

    @ViewById(R.id.type)
    protected Spinner typeSpinner;
    @ViewById(R.id.name)
    protected EditText nameTextView;
    @ViewById(R.id.values)
    protected EditText valuesTextView;
    @ViewById(R.id.default_value_text)
    protected EditText defaultValueTextView;
    @ViewById(R.id.default_value_check)
    protected CheckBox defaultValueCheckBox;


    @Extra
    protected long attributeId = -1;

    private Attribute attribute = new Attribute();

    @AfterViews
    protected void afterViews() {
        typeSpinner.setOnItemSelectedListener(this);
        if (attributeId != -1) {
            attribute = em.get(Attribute.class, attributeId);
            editAttribute();
        }
    }

    @Click(R.id.bOK)
    protected void onSave() {
        updateAttributeFromUI();
        if (Utils.checkEditText(nameTextView, "name", true, 256)) {
            long id = em.saveOrUpdate(attribute);
            Intent intent = new Intent();
            intent.putExtra(AttributeColumns.ID, id);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Click(R.id.bCancel)
    protected void onCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void updateAttributeFromUI() {
        attribute.name = nameTextView.getText().toString();
        attribute.listValues = Utils.text(valuesTextView);
        attribute.type = typeSpinner.getSelectedItemPosition() + 1;
        if (attribute.type == Attribute.TYPE_CHECKBOX) {
            attribute.defaultValue = String.valueOf(defaultValueCheckBox.isChecked());
        } else {
            attribute.defaultValue = Utils.text(defaultValueTextView);
        }
    }

    private void editAttribute() {
        nameTextView.setText(attribute.name);
        typeSpinner.setSelection(attribute.type - 1);
        if (attribute.listValues != null) {
            valuesTextView.setText(attribute.listValues);
        }
        if (attribute.defaultValue != null) {
            if (attribute.type == Attribute.TYPE_CHECKBOX) {
                defaultValueCheckBox.setChecked(Boolean.valueOf(attribute.defaultValue));
            } else {
                defaultValueTextView.setText(attribute.defaultValue);
            }
        }
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        boolean showDefaultCheck = Attribute.TYPE_CHECKBOX - position == 1;
        findViewById(R.id.default_value_layout1).setVisibility(!showDefaultCheck ? View.VISIBLE : View.GONE);
        findViewById(R.id.default_value_check).setVisibility(showDefaultCheck ? View.VISIBLE : View.GONE);
        boolean showValues = Attribute.TYPE_LIST - position == 1 || showDefaultCheck;
        findViewById(R.id.values_layout).setVisibility(showValues ? View.VISIBLE : View.GONE);
        if (showDefaultCheck) {
            valuesTextView.setHint(R.string.checkbox_values_hint);
        } else {
            valuesTextView.setHint(R.string.attribute_values_hint);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

}
