package ru.orangesoftware.financisto2.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.Arrays;
import java.util.List;

import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTree;
import ru.orangesoftware.orb.Expressions;

@EBean
public class CategoryRepository {

    @Bean
    public DatabaseAdapter db;

    private final Context context;

    public CategoryRepository(Context context) {
        this.context = context;
    }

    private CategoryTree tree;

    public CategoryTree loadCategories() {
        List<Category> categories = db.createQuery(Category.class)
                .where(Expressions.gt("id", 0))
                .asc("left")
                .list();
        CategoryTree tree = CategoryTree.createFromListSortedByLeft(categories);
        return tree;
    }

    public void loadAttributesFor(Category category) {
        category.attributes = db.getAttributesForCategory(category.id);
    }

    public void saveCategory(Category newCategory) {
        CategoryTree tree = loadCategories();
        if (newCategory.id > 0) {
            LongSparseArray<Category> map = tree.asIdMap();
            Category oldCategory = map.get(newCategory.id);
            if (oldCategory != null) {
                oldCategory.title = newCategory.title;
                Category oldParent = map.get(oldCategory.getParentId());
                Category newParent = map.get(newCategory.getParentId());
                if (newParent != oldParent) {
                    if (oldParent != null) {
                        oldParent.removeChild(oldCategory);
                    } else {
                        tree.removeRoot(oldCategory);
                    }
                    if (newParent != null) {
                        newParent.addChild(oldCategory);
                    } else {
                        tree.addRoot(oldCategory);
                    }
                }
            }
        } else {
            if (newCategory.getParentId() > 0) {
                LongSparseArray<Category> map = tree.asIdMap();
                Category parent = map.get(newCategory.getParentId());
                if (parent != null) {
                    parent.addChild(newCategory);
                }
            } else {
                tree.addRoot(newCategory);
            }
        }
        saveCategories(tree);
    }

    public Category getCategoryById(long id) {
        if (id == Category.NO_CATEGORY_ID) return Category.noCategory(context);
        if (id == Category.SPLIT_CATEGORY_ID) return Category.splitCategory(context);
        CategoryTree tree = loadCategories();
        LongSparseArray<Category> map = tree.asIdMap();
        return map.get(id);
    }

    public Category getCategoryByLeft(long left) {
        CategoryTree tree = loadCategories();
        List<Category> list = tree.asFlatList();
        for (Category category : list) {
            if (category.left == left) return category;
        }
        return null;
    }

    public void deleteCategoryById(long id) {
        if (id > 0) {
            CategoryTree tree = loadCategories();
            LongSparseArray<Category> map = tree.asIdMap();
            Category category = map.get(id);
            if (category != null) {
                Category parent = category.parent;
                if (parent == null) {
                    tree.removeRoot(category);
                } else {
                    parent.removeChild(category);
                }
                saveCategories(tree);
            }
        }
    }

    public void saveCategories(CategoryTree tree) {
        tree.reIndex();
        saveCategoriesInTransaction(tree);
    }

    protected void saveCategoriesInTransaction(CategoryTree tree) {
        SQLiteDatabase database = db.db();
        database.beginTransaction();
        try {
            insertInTransaction(tree);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void insertInTransaction(CategoryTree tree) {
        db.db().delete("category", null, null);
        db.reInsertCategory(Category.splitCategory(context));
        db.reInsertCategory(Category.noCategory(context));
        insertInTransaction(tree.getRoot().children);
    }

    private void insertInTransaction(List<Category> categories) {
        if (categories == null) return;
        for (Category category : categories) {
            if (category.id <= 0) continue;
            db.reInsertCategory(category);
            db.addAttributes(category.id, category.attributes);
            if (category.hasChildren()) {
                insertInTransaction(category.children);
            }
        }
    }
}
