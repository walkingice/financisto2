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

import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;

import java.util.ArrayList;

import ru.orangesoftware.financisto.R;
import ru.orangesoftware.financisto.adapter.CategoryListAdapter;
import ru.orangesoftware.financisto.db.DatabaseHelper.AttributeColumns;
import ru.orangesoftware.financisto.db.DatabaseHelper.CategoryColumns;
import ru.orangesoftware.financisto.model.Attribute;
import ru.orangesoftware.financisto.model.Category;
import ru.orangesoftware.financisto.utils.EnumUtils;
import ru.orangesoftware.financisto.utils.LocalizableEnum;

import static ru.orangesoftware.financisto.utils.Utils.checkEditText;
import static ru.orangesoftware.financisto.utils.Utils.text;

@EActivity(R.layout.category)
@OptionsMenu(R.menu.category_menu)
public class CategoryActivity extends AbstractActivity {

    public static final int NEW_ATTRIBUTE_REQUEST = 1;
    public static final int EDIT_ATTRIBUTE_REQUEST = 2;

    private static final CategoryType[] categoryTypes = CategoryType.values();

    @StringArrayRes(R.array.attribute_types)
    protected String[] types;

    private Cursor attributeCursor;
    private ListAdapter attributeAdapter;

    private EditText categoryTitle;
    private TextView parentCategoryText;
    private TextView categoryTypeText;

    private Cursor categoryCursor;
    private CategoryListAdapter categoryAdapter;

    @ViewById(R.id.scroll)
    protected ScrollView scrollView;

    private LinearLayout attributesLayout;
    private LinearLayout parentAttributesLayout;

    @Extra
    protected long categoryId = -1;

    private Category category = new Category(-1);
    private CategoryType categoryType = CategoryType.EXPENSE;

    private boolean allowTypeChange = true;

    @AfterViews
    protected void afterViews() {

        if (categoryId != -1) {
            category = db.getCategory(categoryId);
        }

        attributeCursor = db.getAllAttributes();
        startManagingCursor(attributeCursor);
        attributeAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                attributeCursor, new String[]{AttributeColumns.NAME}, new int[]{android.R.id.text1});

        if (category.id == -1) {
            categoryCursor = db.getCategories(true);
        } else {
            categoryCursor = db.getCategoriesWithoutSubtree(category.id);
        }
        startManagingCursor(categoryCursor);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        parentCategoryText = x.addListNode(layout, R.id.category, R.string.parent, R.string.select_category);
        categoryTypeText = x.addListNode(layout, R.id.category_type, R.string.category_type, R.string.expense);

        categoryTitle = new EditText(this);
        categoryTitle.setSingleLine();
        x.addEditNode(layout, R.string.title, categoryTitle);

        attributesLayout = (LinearLayout) x.addTitleNodeNoDivider(layout, R.string.attributes).findViewById(R.id.layout);
        x.addInfoNodePlus(attributesLayout, R.id.new_attribute, R.id.add_attribute, R.string.add_attribute);
        addAttributes();
        parentAttributesLayout = (LinearLayout) x.addTitleNodeNoDivider(layout, R.string.parent_attributes).findViewById(R.id.layout);
        addParentAttributes();

        categoryAdapter = new CategoryListAdapter(
                db, this, android.R.layout.simple_spinner_dropdown_item, categoryCursor);

        editCategory();
    }

    @OptionsItem(R.id.menu_save)
    protected void onSave() {
        if (checkEditText(categoryTitle, "title", true, 100)) {
            category.title = text(categoryTitle);
            setCategoryType(category);
            int count = attributesLayout.getChildCount();
            ArrayList<Attribute> attributes = new ArrayList<Attribute>(count);
            for (int i = 0; i < count; i++) {
                View v = attributesLayout.getChildAt(i);
                Object o = v.getTag();
                if (o instanceof Attribute) {
                    attributes.add((Attribute) o);
                }
            }
            long id = db.insertOrUpdate(category, attributes);
            Intent data = new Intent();
            data.putExtra(CategoryColumns._id.name(), id);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @OptionsItem(R.id.menu_cancel)
    protected void onCancel() {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    private void setCategoryType(Category category) {
        if (category.getParentId() > 0) {
            category.copyTypeFromParent();
        } else {
            if (categoryType == CategoryType.INCOME) {
                category.makeThisCategoryIncome();
            } else {
                category.makeThisCategoryExpense();
            }
        }
    }

    private void editCategory() {
        selectParentCategory(category.getParentId());
        categoryTitle.setText(category.title);
    }

    private void updateIncomeExpenseType() {
        if (category.getParentId() > 0) {
            String type = getString(category.parent.isIncome() ? R.string.income : R.string.expense);
            categoryTypeText.setText(getString(R.string.category_type_inherited, type));
            categoryTypeText.setEnabled(false);
            allowTypeChange = false;
        } else {
            updateIncomeExpenseType(category.isIncome());
            categoryTypeText.setEnabled(true);
            allowTypeChange = true;
        }

    }

    private void updateIncomeExpenseType(boolean isIncome) {
        categoryTypeText.setText(isIncome ? R.string.income : R.string.expense);
    }

    private void addAttributes() {
        long categoryId = category.id;
        if (categoryId == -1) {
            categoryId = 0;
        }
        ArrayList<Attribute> attributes = db.getAttributesForCategory(categoryId);
        for (Attribute a : attributes) {
            addAttribute(a);
        }
    }

    private void addParentAttributes() {
        long categoryId = category.getParentId();
        ArrayList<Attribute> attributes = db.getAllAttributesForCategory(categoryId);
        if (attributes.size() > 0) {
            for (Attribute a : attributes) {
                View v = x.inflater.new Builder(parentAttributesLayout, R.layout.select_entry_simple).create();
                v.setTag(a);
                setAttributeData(v, a);
            }
        } else {
            x.addInfoNodeSingle(parentAttributesLayout, -1, R.string.no_attributes);
        }
    }

    private void addAttribute(Attribute a) {
        View v = x.inflater.new Builder(attributesLayout, R.layout.select_entry_simple_minus).withId(R.id.edit_attribute, this).create();
        setAttributeData(v, a);
        ImageView plusImageView = (ImageView) v.findViewById(R.id.plus_minus);
        plusImageView.setId(R.id.remove_attribute);
        plusImageView.setOnClickListener(this);
        plusImageView.setTag(v.getTag());
        v.setTag(a);
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void setAttributeData(View v, Attribute a) {
        TextView labelView = (TextView) v.findViewById(R.id.label);
        labelView.setText(a.name);
        TextView dataView = (TextView) v.findViewById(R.id.data);
        dataView.setText(types[a.type - 1]);
    }

    @Override
    protected void onClick(View v, int id) {
        switch (id) {
            case R.id.category:
                x.select(this, R.id.category, R.string.parent, categoryCursor, categoryAdapter,
                        CategoryColumns._id.name(), category.getParentId());
                break;
            case R.id.category_type:
                if (allowTypeChange) {
                    ArrayAdapter<String> adapter = EnumUtils.createDropDownAdapter(this, categoryTypes);
                    x.selectPosition(this, R.id.category_type, R.string.category_type, adapter, categoryType.ordinal());
                }
                break;
            case R.id.new_attribute:
                x.select(this, R.id.new_attribute, R.string.attribute, attributeCursor, attributeAdapter,
                        AttributeColumns.ID, -1);
                break;
            case R.id.add_attribute: {
                Intent intent = new Intent(this, AttributeActivity.class);
                startActivityForResult(intent, NEW_ATTRIBUTE_REQUEST);
            }
            break;
            case R.id.edit_attribute: {
                Object o = v.getTag();
                if (o instanceof Attribute) {
                    Intent intent = new Intent(this, AttributeActivity.class);
                    intent.putExtra(AttributeColumns.ID, ((Attribute) o).id);
                    startActivityForResult(intent, EDIT_ATTRIBUTE_REQUEST);
                }
            }
            break;
            case R.id.remove_attribute:
                attributesLayout.removeView((View) v.getTag());
                attributesLayout.removeView((View) v.getParent());
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                break;
        }
    }

    @Override
    public void onSelectedId(int id, long selectedId) {
        switch (id) {
            case R.id.category:
                selectParentCategory(selectedId);
                break;
            case R.id.new_attribute:
                Attribute a = db.getAttribute(selectedId);
                addAttribute(a);
                break;
        }
    }

    @Override
    public void onSelectedPos(int id, int selectedPos) {
        switch (id) {
            case R.id.category_type:
                categoryType = categoryTypes[selectedPos];
                updateIncomeExpenseType(categoryType == CategoryType.INCOME);
                break;
        }
    }

    private void selectParentCategory(long parentId) {
        Category c = em.getCategory(parentId);
        if (c != null) {
            category.parent = c;
            parentCategoryText.setText(c.title);
        }
        updateIncomeExpenseType();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case NEW_ATTRIBUTE_REQUEST: {
                    long attributeId = data.getLongExtra(AttributeColumns.ID, -1);
                    if (attributeId != -1) {
                        Attribute a = db.getAttribute(attributeId);
                        addAttribute(a);
                    }
                }
                break;
                case EDIT_ATTRIBUTE_REQUEST: {
                    long attributeId = data.getLongExtra(AttributeColumns.ID, -1);
                    if (attributeId != -1) {
                        Attribute a = db.getAttribute(attributeId);
                        attributeCursor.requery();
                        updateAttribute(attributesLayout, a);
                        updateAttribute(parentAttributesLayout, a);
                    }
                }
                break;
            }
        }
    }

    private void updateAttribute(LinearLayout layout, Attribute a) {
        int count = layout.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = layout.getChildAt(i);
            Object o = v.getTag();
            if (o instanceof Attribute) {
                Attribute a2 = (Attribute) o;
                if (a2.id == a.id) {
                    setAttributeData(v, a);
                }
            }
        }
    }

    private static enum CategoryType implements LocalizableEnum {
        INCOME(R.string.income),EXPENSE(R.string.expense);

        private final int titleId;

        CategoryType(int titleId) {
            this.titleId = titleId;
        }

        @Override
        public int getTitleId() {
            return titleId;
        }
    }

}
