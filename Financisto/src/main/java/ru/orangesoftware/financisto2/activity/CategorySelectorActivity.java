package ru.orangesoftware.financisto2.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.res.ColorRes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.adapter.BlotterListAdapter;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTree;
import ru.orangesoftware.financisto2.model.CategoryTreeNavigator;
import ru.orangesoftware.financisto2.utils.MenuItemInfo;
import ru.orangesoftware.financisto2.utils.MyPreferences;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 3/14/12 10:40 PM
 */
@EActivity
@OptionsMenu(R.menu.category_selector_menu)
public class CategorySelectorActivity extends AbstractListActivity {

    public static final String SELECTED_CATEGORY_ID = "includeSplit";

    @ColorRes(R.color.category_type_income)
    protected int incomeColor;
    @ColorRes(R.color.category_type_expense)
    protected int expenseColor;

    @Extra
    protected boolean includeSplit;
    @Extra
    protected long selectedCategoryId;

    private CategoryTreeNavigator navigator;
    private Map<Long, String> attributes;

    public CategorySelectorActivity() {
        super(R.layout.category_selector);
        enablePin = false;
    }

    @Override
    protected void internalOnCreate(Bundle savedInstanceState) {
        attributes = db.getAttributesMapping();
        navigator = new CategoryTreeNavigator(db);
        if (MyPreferences.isSeparateIncomeExpense(this)) {
            navigator.separateIncomeAndExpense();
        }
        if (includeSplit) {
            navigator.addSplitCategoryToTheTop();
        }
        navigator.selectCategory(selectedCategoryId);
    }

    @OptionsItem(R.id.menu_back)
    protected void goBack() {
        if (navigator.goBack()) {
            recreateAdapter();
        }
    }

    @OptionsItem(R.id.menu_select)
    protected void confirmSelection() {
        Intent data = new Intent();
        data.putExtra(SELECTED_CATEGORY_ID, navigator.selectedCategoryId);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected List<MenuItemInfo> createContextMenus(long id) {
        return Collections.emptyList();
    }

    @Override
    protected Cursor createCursor() {
        return null;
    }

    @Override
    protected ListAdapter createAdapter(Cursor cursor) {
        return new CategoryAdapter(navigator.categories);
    }

    @Override
    protected void deleteItem(View v, int position, long id) {
    }

    @Override
    protected void editItem(View v, int position, long id) {
    }

    @Override
    protected void viewItem(View v, int position, long id) {
        if (navigator.navigateTo(id)) {
            recreateAdapter();
        } else {
            if (MyPreferences.isAutoSelectChildCategory(this)) {
                confirmSelection();
            }
        }
    }

    public static boolean pickCategory(Activity activity, long selectedCategoryId, boolean includeSplitCategory) {
        if (MyPreferences.isUseHierarchicalCategorySelector(activity)) {
            CategorySelectorActivity_.intent(activity)
                    .selectedCategoryId(selectedCategoryId)
                    .includeSplit(includeSplitCategory)
                    .startForResult(R.id.category_pick);
            return true;
        }
        return false;
    }

    private class CategoryAdapter extends BaseAdapter {

        private final CategoryTree<Category> categories;

        private CategoryAdapter(CategoryTree<Category> categories) {
            this.categories = categories;
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Category getItem(int i) {
            return categories.getAt(i);
        }

        @Override
        public long getItemId(int i) {
            return getItem(i).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BlotterListAdapter.BlotterViewHolder v;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.blotter_list_item, parent, false);
                v = new BlotterListAdapter.BlotterViewHolder(convertView);
                convertView.setTag(v);
            } else {
                v = (BlotterListAdapter.BlotterViewHolder)convertView.getTag();
            }
            Category c = getItem(position);
            if (c.id == CategoryTreeNavigator.INCOME_CATEGORY_ID) {
                v.centerView.setText(getString(R.string.income));                
            } else if (c.id == CategoryTreeNavigator.EXPENSE_CATEGORY_ID) {
                v.centerView.setText(getString(R.string.expense));
            } else {
                v.centerView.setText(c.title);
            }
            v.bottomView.setText(c.tag);
            v.indicator.setBackgroundColor(c.isIncome() ? incomeColor : expenseColor);
            v.rightCenterView.setVisibility(View.INVISIBLE);
            v.iconView.setVisibility(View.INVISIBLE);
            if (attributes != null && attributes.containsKey(c.id)) {
                v.rightView.setText(attributes.get(c.id));
                v.rightView.setVisibility(View.VISIBLE);
            } else {
                v.rightView.setVisibility(View.GONE);
            }
            v.topView.setVisibility(View.INVISIBLE);
            if (navigator.isSelected(c.id)) {
                getListView().setItemChecked(position, true);
            }
            return convertView;
        }

    }
    

}
