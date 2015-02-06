package ru.orangesoftware.financisto2.test.model;

import android.support.v4.util.LongSparseArray;
import android.test.AndroidTestCase;

import java.util.List;

import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTree;

public class CategoryTreeTest extends AndroidTestCase {

    CategoryTree tree = new CategoryTree();

    public void test_should_add_new_root_category() {
        tree.addRoot(createCategory("A"));
        assertTree(
                "L0 I1 P0 [ 1, 2] A"
        );
    }

    public void test_should_add_new_child_category() {
        Category parent = createCategory("A");
        Category child = createCategory("a1");
        parent.addChild(child);
        tree.addRoot(parent);
        assertTree(
                "L0 I1 P0 [ 1, 4] A",
                "L1 I2 P1 [ 2, 3] - a1"
        );
        assertSame(tree.getRoot(), tree.rootAt(0).parent);
        assertEquals(0, tree.rootAt(0).parentId);
        assertSame(tree.rootAt(0), tree.rootAt(0).childAt(0).parent);
        assertEquals(1, tree.rootAt(0).childAt(0).parentId);

        child.addChild(createCategory("aa1"));
        assertTree(
                "L0 I1 P0 [ 1, 6] A",
                "L1 I2 P1 [ 2, 5] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1"
        );

        parent.addChild(createCategory("a2"));
        assertTree(
                "L0 I1 P0 [ 1, 8] A",
                "L1 I2 P1 [ 2, 5] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L1 I4 P1 [ 6, 7] - a2"
        );

        child.addChild(createCategory("aa2"));
        assertTree(
                "L0 I1 P0 [ 1,10] A",
                "L1 I2 P1 [ 2, 7] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L2 I5 P2 [ 5, 6] -- aa2",
                "L1 I4 P1 [ 8, 9] - a2"
        );

        tree.addRoot(createCategory("B"));
        assertTree(
                "L0 I1 P0 [ 1,10] A",
                "L1 I2 P1 [ 2, 7] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L2 I5 P2 [ 5, 6] -- aa2",
                "L1 I4 P1 [ 8, 9] - a2",
                "L0 I6 P0 [11,12] B"
        );
    }

    /*public void test_should_move_child_category_to_another_parent() {
        Category parent = createCategory("A");
        Category child = createCategory("a1");
        parent.addChild(child);
        parent.addChild(createCategory("a2"));
        child.addChild(createCategory("aa1"));
        child.addChild(createCategory("aa2"));
        tree.addRoot(parent);
        tree.addRoot(createCategory("B"));
        assertTree(
                "L0 I1 P0 [ 1,10] A",
                "L1 I2 P1 [ 2, 7] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L2 I4 P2 [ 5, 6] -- aa2",
                "L1 I5 P1 [ 8, 9] - a2",
                "L0 I6 P0 [11,12] B"
        );
        Category aa2 = parent.childAt(0).childAt(1);
        Category a2 = parent.childAt(1);
        aa2.moveToNewParent(a2);
        assertTree(
                "L0 I1 P0 [ 1,10] A",
                "L1 I2 P1 [ 2, 5] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L1 I5 P1 [ 6, 9] - a2",
                "L2 I4 P5 [ 7, 8] -- aa2",
                "L0 I6 P0 [11,12] B"
        );
        a2.moveToNewParent(tree.rootAt(1));
        assertTree(
                "L0 I1 P0 [ 1, 6] A",
                "L1 I2 P1 [ 2, 5] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L0 I6 P0 [ 7,12] B",
                "L1 I5 P6 [ 8,11] - a2",
                "L2 I4 P5 [ 9,10] -- aa2"
        );
        Category a1 = tree.asIdMap().get(2);
        a1.moveToNewParent(tree.getRoot());
        assertTree(
                "L0 I1 P0 [ 1, 2] A",
                "L0 I6 P0 [ 3, 8] B",
                "L1 I5 P6 [ 4, 7] - a2",
                "L2 I4 P5 [ 5, 6] -- aa2",
                "L0 I2 P0 [ 9,12] a1",
                "L1 I3 P2 [10,11] - aa1"
        );
    }*/

    public void test_should_delete_category() {
        Category parent = createCategory("A");
        Category child = createCategory("a1");
        parent.addChild(child);
        parent.addChild(createCategory("a2"));
        child.addChild(createCategory("aa1"));
        child.addChild(createCategory("aa2"));
        tree.addRoot(parent);
        tree.addRoot(createCategory("B"));
        assertTree(
                "L0 I1 P0 [ 1,10] A",
                "L1 I2 P1 [ 2, 7] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L2 I4 P2 [ 5, 6] -- aa2",
                "L1 I5 P1 [ 8, 9] - a2",
                "L0 I6 P0 [11,12] B"
        );

        parent.removeChild(parent.childAt(0));
        assertTree(
                "L0 I1 P0 [ 1, 4] A",
                "L1 I5 P1 [ 2, 3] - a2",
                "L0 I6 P0 [ 5, 6] B"
        );

        tree.removeRoot(tree.rootAt(0));
        assertTree(
                "L0 I6 P0 [ 1, 2] B"
        );
    }

    public void test_should_convert_tree_into_map() {
        Category parent = createCategory("A");
        Category child = createCategory("a1");
        parent.addChild(child);
        parent.addChild(createCategory("a2"));
        child.addChild(createCategory("aa1"));
        child.addChild(createCategory("aa2"));
        tree.addRoot(parent);
        tree.addRoot(createCategory("B"));
        assertTree(
                "L0 I1 P0 [ 1,10] A",
                "L1 I2 P1 [ 2, 7] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L2 I4 P2 [ 5, 6] -- aa2",
                "L1 I5 P1 [ 8, 9] - a2",
                "L0 I6 P0 [11,12] B"
        );
        LongSparseArray<Category> map = tree.asIdMap();
        assertEquals("A", map.get(1).title);
        assertEquals("a1", map.get(2).title);
        assertEquals("aa2", map.get(4).title);
        assertEquals("B", map.get(6).title);
    }

    public void test_should_convert_tree_into_flat_list() {
        Category parent = createCategory("A");
        Category child = createCategory("a1");
        parent.addChild(child);
        parent.addChild(createCategory("a2"));
        child.addChild(createCategory("aa1"));
        child.addChild(createCategory("aa2"));
        tree.addRoot(parent);
        tree.addRoot(createCategory("B"));
        assertTree(
                "L0 I1 P0 [ 1,10] A",
                "L1 I2 P1 [ 2, 7] - a1",
                "L2 I3 P2 [ 3, 4] -- aa1",
                "L2 I4 P2 [ 5, 6] -- aa2",
                "L1 I5 P1 [ 8, 9] - a2",
                "L0 I6 P0 [11,12] B"
        );
        List<Category> list = tree.asFlatList();
        assertEquals("A", list.get(0).title);
        assertEquals("a1", list.get(1).title);
        assertEquals("aa1", list.get(2).title);
        assertEquals("aa2", list.get(3).title);
    }

    private CategoryTree assertTree(String...expectedTree) {
        StringBuilder sb = new StringBuilder();
        for (String s : expectedTree) {
            sb.append("\n").append(s);
        }
        tree.reIndex();
        String actualTree = tree.printTree();
        assertEquals(sb.toString(), actualTree);
        return tree;
    }

    private Category assertCategoryAtIndex(CategoryTree tree, int i, String title) {
        Category category = tree.rootAt(i);
        assertEquals(title, category.title);
        return category;
    }

    private Category createCategory(String title) {
        Category category = new Category();
        category.title = title;
        return category;
    }

}
