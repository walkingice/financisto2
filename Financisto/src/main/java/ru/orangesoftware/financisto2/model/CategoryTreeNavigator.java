/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.model;

import android.content.Context;
import android.support.v4.util.LongSparseArray;

import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 3/18/12 8:21 PM
 */
public class CategoryTreeNavigator {

    public static final long INCOME_CATEGORY_ID = -101;
    public static final long EXPENSE_CATEGORY_ID = -102;

    private final Stack<List<Category>> categoriesStack = new Stack<List<Category>>();

    Category noCategory;
    Category splitCategory;

    public List<Category> categories;
    public long selectedCategoryId = 0;

    public CategoryTreeNavigator(Context context, CategoryRepository repository) {
        this.categories = repository.loadCategories().getRoot().children;
        this.noCategory = Category.noCategory(context);
        this.splitCategory = Category.splitCategory(context);
        tagCategories(noCategory);
    }

    public void selectCategory(long selectedCategoryId) {
        LongSparseArray<Category> map = CategoryTree.asDeepMap(categories);
        Category selectedCategory = map.get(selectedCategoryId);
        if (selectedCategory != null) {
            Stack<Long> path = new Stack<Long>();
            Category parent = selectedCategory.parent;
            while (parent != null) {
                path.push(parent.id);
                parent = parent.parent;
            }
            while (!path.isEmpty()) {
                navigateTo(path.pop());
            }
            this.selectedCategoryId = selectedCategoryId;
        }
    }

    public void tagCategories(Category parent) {
        if (categories.size() > 0 && categories.get(0).id != parent.id) {
            Category copy = new Category();
            copy.id = parent.id;
            copy.title = parent.title;
            if (parent.isIncome()) {
                copy.makeThisCategoryIncome();
            }
            categories.add(0, copy);
        }
        StringBuilder sb = new StringBuilder();
        for (Category c : categories) {
            if (c.tag == null && c.hasChildren()) {
                sb.setLength(0);
                List<Category> children = c.children;
                for (Category child : children) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(child.title);
                }
                c.tag = sb.toString();
            }
        }
    }

    public boolean goBack() {
        if (!categoriesStack.isEmpty()) {
            Category selectedCategory = findCategory(selectedCategoryId);
            if (selectedCategory != null) {
                selectedCategoryId = selectedCategory.getParentId();
            }
            categories = categoriesStack.pop();
            return true;
        }
        return false;
    }

    public boolean canGoBack() {
        return !categoriesStack.isEmpty();
    }

    public boolean goRoot() {
        boolean result = canGoBack();

        while (canGoBack())
            goBack();

        return result;
    }

    public boolean navigateTo(long categoryId) {
        Category selectedCategory = findCategory(categoryId);
        if (selectedCategory != null) {
            selectedCategoryId = selectedCategory.id;
            if (selectedCategory.hasChildren()) {
                categoriesStack.push(categories);
                categories = selectedCategory.children;
                tagCategories(selectedCategory);
                return true;
            }
        }
        return false;
    }

    private Category findCategory(long categoryId) {
        for (Category category : categories) {
            if (category.id == categoryId) {
                return category;
            }
        }
        return null;
    }

    public boolean isSelected(long categoryId) {
        return selectedCategoryId == categoryId;
    }
    
    public List<Category> getSelectedRoots() {
        return categories;
    }

    public void addSplitCategoryToTheTop() {
        categories.add(0, splitCategory);
    }

    public void separateIncomeAndExpense() {
        List<Category> newCategories = new ArrayList<Category>();
        Category income = new Category();
        income.id = INCOME_CATEGORY_ID;
        income.makeThisCategoryIncome();
        income.title = "<INCOME>";
        Category expense = new Category();
        expense.id = EXPENSE_CATEGORY_ID;
        expense.makeThisCategoryExpense();
        expense.title = "<EXPENSE>";
        for (Category category : categories) {
            if (category.id <= 0) {
                newCategories.add(category);
            } else {
                if (category.isIncome()) {
                    income.addChild(category);
                } else {
                    expense.addChild(category);
                }
            }
        }
        if (income.hasChildren()) {
            newCategories.add(income);
        }
        if (expense.hasChildren()) {
            newCategories.add(expense);
        }
        categories = newCategories;
    }

}
