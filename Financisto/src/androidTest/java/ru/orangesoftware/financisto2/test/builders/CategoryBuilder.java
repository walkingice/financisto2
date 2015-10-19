package ru.orangesoftware.financisto2.test.builders;

import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.model.Attribute;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTree;

import java.util.*;

public class CategoryBuilder {

    private final Category category = new Category();

    /**
     * A
     * - A1 [attr0]
     * -- AA1 [attr1, attr2]
     * - A2
     * B
     */
    public static Map<String, Category> createDefaultHierarchy(CategoryRepository categoryRepository) {
        Category a = createCategory("A");
        Category a1 = createCategory("A1");
        a1.attributes = new ArrayList<>();
        a1.attributes.add(AttributeBuilder.withDb(categoryRepository.db).createTextAttribute("attr0"));
        a.addChild(a1);
        Category a2 = createCategory("A2");
        a.addChild(a2);
        Category aa1 = createCategory("AA1");
        aa1.attributes = new ArrayList<>();
        aa1.attributes.add(AttributeBuilder.withDb(categoryRepository.db).createTextAttribute("attr1"));
        aa1.attributes.add(AttributeBuilder.withDb(categoryRepository.db).createTextAttribute("attr2"));
        a1.addChild(aa1);
        Category b = createCategory("B");
        b.makeThisCategoryIncome();
        CategoryTree tree = new CategoryTree();
        tree.addRoot(a);
        tree.addRoot(b);
        categoryRepository.saveCategories(tree);
        return asMap(categoryRepository.loadCategories());
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

}
