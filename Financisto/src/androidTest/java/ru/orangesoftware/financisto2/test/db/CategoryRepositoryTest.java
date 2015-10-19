package ru.orangesoftware.financisto2.test.db;

import java.util.ArrayList;
import java.util.Arrays;

import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.model.Attribute;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTree;
import ru.orangesoftware.financisto2.test.builders.AttributeBuilder;

import static java.util.Arrays.asList;

public class CategoryRepositoryTest extends AbstractDbTest {

    CategoryRepository repository;
    CategoryTree tree;
    Attribute attribute1;
    Attribute attribute2;
    Attribute attribute3;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        repository = new CategoryRepository(context);
        repository.db = db;
        tree = repository.loadCategories();
        attribute1 = AttributeBuilder.withDb(db).createTextAttribute("attr1");
        attribute2 = AttributeBuilder.withDb(db).createTextAttribute("attr2");
        attribute3 = AttributeBuilder.withDb(db).createNumberAttribute("attr3");
    }

    public void test_should_load_empty_tree() {
        assertEquals(0, tree.asIdMap().size());
    }

    public void test_should_not_save_system_categories() {
        tree.addRoot(createSystemCategory(0));
        tree.addRoot(createSystemCategory(-1));
        repository.saveCategories(tree);
        tree = repository.loadCategories();
        assertEquals(0, tree.asIdMap().size());
    }

    public void test_should_move_child_category_to_another_parent() {
        Category parent = createCategory("A");
        Category child = createCategory("a1");
        parent.addChild(child);
        parent.addChild(createCategory("a2"));
        child.addChild(createCategory("aa1"));
        child.addChild(createCategory("aa2"));
        tree.addRoot(parent);
        tree.addRoot(createCategory("B"));
        repository.saveCategories(tree);
        assertTree(
                "L0 I1 P0 [ 1,10] A",
                "L1 I2 P1 [ 2, 7] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L2 I4 P2 [ 5, 6] -- aa2",
                "L1 I5 P1 [ 8, 9] - a2",
                "L0 I6 P0 [11,12] B"
        );

        Category a1 = tree.rootAt(0).childAt(0);
        a1.parent = Category.noCategory(context);
        repository.saveCategory(a1);
        assertTree(
                "L0 I1 P0 [ 1, 4] A",
                "L1 I5 P1 [ 2, 3] - a2",
                "L0 I6 P0 [ 5, 6] B",
                "L0 I2 P0 [ 7,12] a1",
                "L1 I3 P2 [ 8, 9] - aa1",
                "L1 I4 P2 [10,11] - aa2"
        );
    }

    public void test_should_save_new_root_category() {
        repository.saveCategory(createCategory("A"));
        repository.saveCategory(createCategory("B"));
        assertTree(
                "L0 I1 P0 [ 1, 2] A",
                "L0 I2 P0 [ 3, 4] B"
        );
    }

    public void test_should_save_new_child_category() {
        Category a = createCategory("A");
        repository.saveCategory(a);
        assertTree(
                "L0 I1 P0 [ 1, 2] A"
        );
        Category a1 = createCategory("a1");
        a1.parent = a;
        repository.saveCategory(a1);
        assertTree(
                "L0 I1 P0 [ 1, 4] A",
                "L1 I2 P1 [ 2, 3] - a1"
        );
        Category aa1 = createCategory("aa1");
        aa1.parent = a1;
        repository.saveCategory(aa1);
        assertTree(
                "L0 I1 P0 [ 1, 6] A",
                "L1 I2 P1 [ 2, 5] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1"
        );
    }

    public void test_should_update_root_category() {
        Category a = createCategory("A");
        repository.saveCategory(a);
        Category b = createCategory("B");
        repository.saveCategory(b);
        assertTree(
                "L0 I1 P0 [ 1, 2] A",
                "L0 I2 P0 [ 3, 4] B"
        );
        a.title = "AA";
        repository.saveCategory(a);
        assertTree(
                "L0 I1 P0 [ 1, 2] AA",
                "L0 I2 P0 [ 3, 4] B"
        );
        b.parent = a;
        b.title = "aa1";
        repository.saveCategory(b);
        assertTree(
                "L0 I1 P0 [ 1, 4] AA",
                "L1 I2 P1 [ 2, 3] - aa1"
        );
        b.parent = Category.noCategory(context);
        b.title = "C";
        repository.saveCategory(b);
        assertTree(
                "L0 I1 P0 [ 1, 2] AA",
                "L0 I2 P0 [ 3, 4] C"
        );
    }

    public void test_should_update_child_category() {
        Category a = createCategory("A");
        Category a1 = createCategory("a1");
        Category aa1 = createCategory("aa1");
        a.addChild(a1);
        a1.addChild(aa1);
        Category b = createCategory("B");
        tree.addRoot(a);
        tree.addRoot(b);
        repository.saveCategories(tree);
        assertTree(
                "L0 I1 P0 [ 1, 6] A",
                "L1 I2 P1 [ 2, 5] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L0 I4 P0 [ 7, 8] B"
        );
        a1.parent = b;
        repository.saveCategory(a1);
        assertTree(
                "L0 I1 P0 [ 1, 2] A",
                "L0 I4 P0 [ 3, 8] B",
                "L1 I2 P4 [ 4, 7] - a1",
                "L2 I3 P2 [ 5, 6] -- aa1"
        );
        aa1.parent = Category.noCategory(context);
        aa1.title = "C";
        repository.saveCategory(aa1);
        assertTree(
                "L0 I1 P0 [ 1, 2] A",
                "L0 I4 P0 [ 3, 6] B",
                "L1 I2 P4 [ 4, 5] - a1",
                "L0 I3 P0 [ 7, 8] C"
        );
    }

    public void test_should_delete_root_category() {
        Category a = createCategory("A");
        Category a1 = createCategory("a1");
        Category aa1 = createCategory("aa1");
        a.addChild(a1);
        a1.addChild(aa1);
        Category b = createCategory("B");
        tree.addRoot(a);
        tree.addRoot(b);
        repository.saveCategories(tree);
        assertTree(
                "L0 I1 P0 [ 1, 6] A",
                "L1 I2 P1 [ 2, 5] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L0 I4 P0 [ 7, 8] B"
        );
        repository.deleteCategoryById(b.id);
        assertTree(
                "L0 I1 P0 [ 1, 6] A",
                "L1 I2 P1 [ 2, 5] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1"
        );
        repository.deleteCategoryById(2);
        assertTree(
                "L0 I1 P0 [ 1, 2] A"
        );
    }

    public void test_should_save_attributes() {
        Category a = createCategory("A");
        a.attributes = asList(attribute1, attribute2);
        repository.saveCategory(a);
        assertAttributes(a, "attr1", "attr2");

        a.attributes = asList(attribute2, attribute3);
        repository.saveCategory(a);
        assertAttributes(a, "attr2", "attr3");

        Category a1 = createCategory("a1");
        a.addChild(a1);
        tree.addRoot(a);
        repository.saveCategories(tree);

        assertTree(
                "L0 I1 P0 [ 1, 4] A",
                "L1 I2 P1 [ 2, 3] - a1"
        );
        assertAttributes(a, "attr2", "attr3");
    }

    private CategoryTree assertTree(String...expectedTree) {
        StringBuilder sb = new StringBuilder();
        for (String s : expectedTree) {
            sb.append("\n").append(s);
        }
        tree = repository.loadCategories();
        String actualTree = tree.printTree();
        assertEquals(sb.toString(), actualTree);
        return tree;
    }

    private void assertAttributes(Category c, String...attributeNames) {
        repository.loadAttributesFor(c);
        assertEquals(attributeNames.length, c.attributes.size());
        for (int i = 0; i < attributeNames.length; i++) {
            assertEquals("Attribute at "+i, attributeNames[i], c.attributes.get(i).name);
        }
    }

    private Category createCategory(String title) {
        Category category = new Category();
        category.title = title;
        return category;
    }

    private Category createSystemCategory(long id) {
        Category category = new Category(id);
        category.systemEntity = true;
        return category;
    }

}
