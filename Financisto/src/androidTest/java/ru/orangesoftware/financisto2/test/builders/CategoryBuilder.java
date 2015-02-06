package ru.orangesoftware.financisto2.test.builders;

import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTree;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Denis Solonenko
 * Date: 4/28/11 11:29 PM
 */
public class CategoryBuilder {

    //private final DatabaseAdapter db;
    private final Category category = new Category();

    /**
     * A
     * - A1
     * -- AA1
     * - A2
     * B
     */
    public static Map<String, Category> createDefaultHierarchy(CategoryRepository categoryRepository) {
        Category a = createCategory("A");
        Category a1 = createCategory("A1");
        a.addChild(a1);
        Category a2 = createCategory("A2");
        a.addChild(a2);
        Category aa1 = createCategory("AA1");
        a1.addChild(aa1);
        Category b = createCategory("B");
        b.makeThisCategoryIncome();
        CategoryTree tree = new CategoryTree();
        tree.addRoot(a);
        tree.addRoot(b);
        categoryRepository.saveCategories(tree);
        return asMap(categoryRepository.loadCategories());
//        Category a = new CategoryBuilder(db).withTitle("A").create();
//        Category a1 = new CategoryBuilder(db).withParent(a).withTitle("A1").create();
//        new CategoryBuilder(db).withParent(a1).withTitle("AA1")
//                .withAttributes(
//                        AttributeBuilder.withDb(db).createTextAttribute("attr1"),
//                        AttributeBuilder.withDb(db).createNumberAttribute("attr2")
//                ).create();
//        new CategoryBuilder(db).withParent(a).withTitle("A2").create();
//        new CategoryBuilder(db).withTitle("B").income().create();
        //return allCategoriesAsMap(db);
    }

    private static Map<String, Category> asMap(CategoryTree categoryTree) {
        HashMap<String, Category> map = new HashMap<String, Category>();
        initMap(map, categoryTree.getRoot().children);
        return map;
    }

    private static void initMap(Map<String, Category> map, List<Category> categories) {
        if (categories == null) return;
        for (Category category : categories) {
            map.put(category.title, category);
            if (category.hasChildren()) {
                initMap(map, category.children);
            }
        }
    }

    private static Category createCategory(String title) {
        Category c = new Category();
        c.title = title;
        return c;
    }

//    private CategoryBuilder withAttributes(Attribute...attributes) {
//        category.attributes = Arrays.asList(attributes);
//        return this;
//    }
//
//    private static Map<String, Category> allCategoriesAsMap(DatabaseAdapter db) {
//        HashMap<String, Category> map = new HashMap<String, Category>();
//        List<Category> categories = db.getAllCategoriesList(false);
//        for (Category category : categories) {
//            category.attributes = db.getAttributesForCategory(category.id);
//            map.put(category.title, category);
//        }
//        return map;
//    }
//
//    public static Category split(DatabaseAdapter db) {
//        return db.getCategory(Category.SPLIT_CATEGORY_ID);
//    }
//
//    public static Category noCategory(DatabaseAdapter db) {
//        return db.getCategory(Category.NO_CATEGORY_ID);
//    }
//
//    private CategoryBuilder(DatabaseAdapter db) {
//        this.db = db;
//    }
//
//    public CategoryBuilder withTitle(String title) {
//        category.title = title;
//        return this;
//    }
//
//    public CategoryBuilder withParent(Category parent) {
//        category.parent = parent;
//        return this;
//    }
//
//    private CategoryBuilder income() {
//        category.makeThisCategoryIncome();
//        return this;
//    }
//
//    public Category create() {
//        //category.id = db.insertOrUpdate(category, category.attributes);
//        return category;
//    }

}
