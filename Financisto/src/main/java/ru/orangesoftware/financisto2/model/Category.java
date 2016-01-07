/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * <p/>
 * Contributors:
 * Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto2.model;

import android.content.Context;
import android.database.Cursor;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.db.DatabaseHelper.CategoryViewColumns;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@Entity
@Table(name = "category")
public class Category extends MyEntity implements Iterable<Category> {

    public static final long NO_CATEGORY_ID = 0;
    public static final long SPLIT_CATEGORY_ID = -1;

    public static boolean isSplit(long categoryId) {
        return Category.SPLIT_CATEGORY_ID == categoryId;
    }

    public static Category noCategory(Context context) {
        Category category = new Category();
        category.id = 0;
        category.left = -90000;
        category.right = -90000;
        category.title = context.getString(R.string.no_category);
        category.systemEntity = true;
        return category;
    }

    public static Category splitCategory(Context context) {
        Category category = new Category();
        category.id = -1;
        category.left = category.right = -99000;
        category.title = context.getString(R.string.split);
        category.systemEntity = true;
        return category;
    }

    public static final int TYPE_EXPENSE = 0;
    public static final int TYPE_INCOME = 1;

    @Column(name = "last_project_id")
    public long lastProjectId;

    @Column(name = "left")
    public int left;

    @Column(name = "right")
    public int right;

    @Column(name = "type")
    public int type = TYPE_EXPENSE;

    @Transient
    public int level;

    @Transient
    public Category parent;

    @Transient
    public List<Category> children;

    @Transient
    public List<Attribute> attributes;

    @Transient
    public String tag;

    public Category() {
    }

    public Category(long id) {
        this.id = id;
    }

    public long getParentId() {
        return parent != null ? parent.id : 0;
    }

    public void addChild(Category category) {
        if (children == null) {
            children = new ArrayList<Category>();
        }
        category.parent = this;
        category.level = level + 1;
        children.add(category);
    }

    public void removeChild(Category category) {
        if (children != null) {
            children.remove(category);
        }
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public Category childAt(int pos) {
        return children.get(pos);
    }

    public boolean isExpense() {
        return type == TYPE_EXPENSE;
    }

    public boolean isIncome() {
        return type == TYPE_INCOME;
    }

    public void makeThisCategoryIncome() {
        this.type = TYPE_INCOME;
    }

    public void makeThisCategoryExpense() {
        this.type = TYPE_EXPENSE;
    }

    public void copyTypeFromParent() {
        if (parent != null) {
            this.type = parent.type;
        }
    }

    public boolean isSplit() {
        return id == SPLIT_CATEGORY_ID;
    }

    public int childIndex(Category c) {
        return MyEntity.indexOf(children, c.id);
    }

    public int childrenCount() {
        return children.size();
    }

    @Override
    public Iterator<Category> iterator() {
        return children.iterator();
    }

    //    public void moveToNewParent(Category newParent) {
//        if (parent != null) {
//            parent.removeChild(this);
//        }
//        newParent.addChild(this);
//    }
//
    public boolean moveChildUp(int pos) {
        if (pos > 0 && pos < children.size()) {
            swap(pos, pos - 1);
            return true;
        }
        return false;
    }

    public boolean moveChildDown(int pos) {
        if (pos >= 0 && pos < children.size() - 1) {
            swap(pos, pos + 1);
            return true;
        }
        return false;
    }

    public boolean moveChildToTheTop(int pos) {
        if (pos > 0 && pos < children.size()) {
            Category node = children.remove(pos);
            children.add(0, node);
            return true;
        }
        return false;
    }

    public boolean moveChildToTheBottom(int pos) {
        if (pos >= 0 && pos < children.size() - 1) {
            Category node = children.remove(pos);
            children.add(children.size(), node);
            return true;
        }
        return false;
    }

    private void swap(int from, int to) {
        Category fromNode = children.get(from);
        Category toNode = children.set(to, fromNode);
        children.set(from, toNode);
    }

    public void sortByTitle() {
        if (children != null) {
            Collections.sort(children, byTitleComparator);
            for (Category child : children) {
                child.sortByTitle();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("id=").append(id);
        sb.append(",parentId=").append(getParentId());
        sb.append(",title=").append(title);
        sb.append(",level=").append(level);
        sb.append(",left=").append(left);
        sb.append(",right=").append(right);
        sb.append(",type=").append(type);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String getTitle() {
        return getTitle(title, level);
    }

    public static String getTitle(String title, int level) {
        String span = getTitleSpan(level);
        return span + title;
    }

    public static String getTitleSpan(int level) {
        //level -= 1;
        if (level <= 0) {
            return "";
        } else if (level == 1) {
            return "- ";
        } else if (level == 2) {
            return "-- ";
        } else if (level == 3) {
            return "--- ";
        } else {
            StringBuilder sb = new StringBuilder();
            if (level > 0) {
                for (int i = 0; i < level; i++) {
                    sb.append("-");
                }
            }
            return sb.toString();
        }
    }

    private static final Comparator<Category> byTitleComparator = new Comparator<Category>() {
        @Override
        public int compare(Category c1, Category c2) {
            String t1 = c1.title;
            String t2 = c2.title;
            if (t1 == null) {
                t1 = "";
            }
            if (t2 == null) {
                t2 = "";
            }
            return t1.compareTo(t2);
        }
    };

}
