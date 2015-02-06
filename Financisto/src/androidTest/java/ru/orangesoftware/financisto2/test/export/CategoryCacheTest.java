/*
 * Copyright (c) 2012 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package ru.orangesoftware.financisto2.test.export;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.orangesoftware.financisto2.export.CategoryCache;
import ru.orangesoftware.financisto2.export.CategoryInfo;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTree;
import ru.orangesoftware.financisto2.test.builders.CategoryBuilder;

import static ru.orangesoftware.financisto2.export.CategoryCache.extractCategoryName;

/**
 * Created by IntelliJ IDEA.
 * User: denis.solonenko
 * Date: 5/29/12 2:25 PM
 */
public class CategoryCacheTest extends AbstractImportExportTest {

    CategoryCache cache = new CategoryCache();

    public void test_should_split_category_name() {
        assertEquals("P1", extractCategoryName("P1"));
        assertEquals("P1:c1", extractCategoryName("P1:c1"));
        assertEquals("P1", extractCategoryName("P1/C2"));
        assertEquals("P1:c1", extractCategoryName("P1:c1/C2"));
    }

    public void test_should_import_categories() throws Exception {
        //given
        //P1         1-10
        // - cc1     2-7
        // -- c1     3-4
        // -- c2     5-6
        // - cc2     8-9
        //P2         11-14
        // - x1      12-13
        Set<CategoryInfo> list = new HashSet<CategoryInfo>();
        list.add(new CategoryInfo("P1:cc1:c1", true));
        list.add(new CategoryInfo("P1:cc1", true));
        list.add(new CategoryInfo("P1:cc1:c2", true));
        list.add(new CategoryInfo("P2", false));
        list.add(new CategoryInfo("P2:x1", false));
        list.add(new CategoryInfo("P1", false));
        list.add(new CategoryInfo("P1:cc2", true));

        //when
        cache.insertCategories(categoryRepository, list);

        //then
        assertNotNull(cache.findCategory("P1"));
        assertNotNull(cache.findCategory("P1:cc1"));
        assertNotNull(cache.findCategory("P1:cc1:c2"));
        assertNotNull(cache.findCategory("P2:x1"));

        //then
        CategoryTree tree = categoryRepository.loadCategories();
        assertNotNull(tree);
        assertEquals(2, tree.getRoot().childrenCount());

        Category c = tree.rootAt(0);
        assertCategory("P1", true, c);
        assertEquals(2, c.children.size());

        assertCategory("cc1", true, c.childAt(0));
        assertEquals(2, c.childAt(0).children.size());

        assertCategory("cc2", true, c.childAt(1));
        assertFalse(c.childAt(1).hasChildren());

        c = tree.rootAt(1);
        assertCategory("P2", false, c);
        assertEquals(1, c.children.size());

        assertCategory("x1", false, c.childAt(0));
    }

    public void test_should_load_existing_categories() throws Exception {
        //given existing
        /**
         * A
         * - A1
         * -- AA1
         * - A2
         * B
         */
        Map<String, Category> existingCategories = CategoryBuilder.createDefaultHierarchy(categoryRepository);
        //when
        cache.loadExistingCategories(categoryRepository);
        //then
        assertEquals(existingCategories.get("A").id, cache.findCategory("A").id);
        assertEquals(existingCategories.get("A1").id, cache.findCategory("A:A1").id);
        assertEquals(existingCategories.get("AA1").id, cache.findCategory("A:A1:AA1").id);
        assertEquals(existingCategories.get("A2").id, cache.findCategory("A:A2").id);
        assertEquals(existingCategories.get("B").id, cache.findCategory("B").id);
    }

    public void test_should_merge_existing_and_new_categories() throws Exception {
        //given existing
        CategoryBuilder.createDefaultHierarchy(categoryRepository);

        //when
        cache.loadExistingCategories(categoryRepository);
        Set<CategoryInfo> list = new HashSet<CategoryInfo>();
        list.add(new CategoryInfo("A:A1", true));
        list.add(new CategoryInfo("B", true));
        list.add(new CategoryInfo("A:A1:AA2", true));
        list.add(new CategoryInfo("A:A2:AB1", false));
        list.add(new CategoryInfo("C", false));
        list.add(new CategoryInfo("D:D1", true));
        cache.insertCategories(categoryRepository, list);

        //then
        /**
         * A            1-12
         * - A1         2-7
         * -- AA1       3-4
         * -- AA2       5-6
         * - A2         8-11
         * -- AB1       9-10
         * B            13-14
         * C            15-16
         * D            17-20
         * - D1         18-19
         */
        CategoryTree tree = categoryRepository.loadCategories();
        assertNotNull(tree);
        assertEquals(4, tree.getRoot().childrenCount());
    }

}
