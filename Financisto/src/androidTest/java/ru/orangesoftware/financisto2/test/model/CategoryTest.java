package ru.orangesoftware.financisto2.test.model;

import android.test.AndroidTestCase;

import ru.orangesoftware.financisto2.model.Category;

public class CategoryTest extends AndroidTestCase {

	private Category root;

    @Override
	protected void setUp() throws Exception {
        Category a = createCategory("A");
		a.addChild(createCategory("a1"));
		a.addChild(createCategory("a2"));
		a.addChild(createCategory("a3"));
        Category b = createCategory("B");
        Category b1 = createCategory("b1");
		b.addChild(b1);
		b1.addChild(createCategory("bb2"));
		b1.addChild(createCategory("bb1"));
        Category c = createCategory("C");
		root = new Category();
        root.addChild(a);
        root.addChild(b);
        root.addChild(c);
	}

    public void testShouldMoveCategoryWithNoChildrenUpCorrectly() {
        Category tree = root.childAt(0);
		assertFalse(tree.moveChildUp(tree.children.size()));
		assertFalse(tree.moveChildUp(-1));
		assertFalse(tree.moveChildUp(0));
		assertTrue(tree.moveChildUp(1));
        assertChildren(tree, "a2", "a1", "a3");
	}

    public void testShouldMoveCategoryWithNoChildrenDownCorrectly() {
        Category tree = root.childAt(0);
		assertFalse(tree.moveChildDown(tree.children.size()));
		assertFalse(tree.moveChildDown(tree.children.size() - 1));
		assertFalse(tree.moveChildDown(-1));
		assertTrue(tree.moveChildDown(0));
        assertChildren(tree, "a2", "a1", "a3");
	}

	public void testShouldMoveCategoryWithChildrenUpCorrectly() {
        Category tree = root;
		assertTrue(tree.moveChildUp(1));
        assertChildren(tree, "B", "A", "C");
        assertChildren(tree.childAt(0), "b1");
        assertChildren(tree.childAt(1), "a1", "a2", "a3");
	}

	public void testShouldMoveCategoryWithChildrenDownCorrectly() {
        Category tree = root;
		assertTrue(tree.moveChildDown(0));
        assertChildren(tree, "B", "A", "C");
        assertChildren(tree.childAt(0), "b1");
        assertChildren(tree.childAt(1), "a1", "a2", "a3");
	}

	public void testShouldMoveCategoryWithNoChildrenToTopCorrectly() {
        Category tree = root.childAt(0);
		assertFalse(tree.moveChildToTheTop(tree.children.size()));
		assertFalse(tree.moveChildToTheTop(-1));
		assertFalse(tree.moveChildToTheTop(0));
		assertTrue(tree.moveChildToTheTop(2));
        assertChildren(tree, "a3", "a1", "a2");
	}

	public void testShouldMoveCategoryWithNoChildrenToBottomCorrectly() {
        Category tree = root.childAt(0);
		assertFalse(tree.moveChildToTheBottom(tree.children.size()));
		assertFalse(tree.moveChildToTheBottom(tree.children.size() - 1));
		assertFalse(tree.moveChildToTheBottom(-1));
		assertTrue(tree.moveChildToTheBottom(1));
        assertChildren(tree, "a1", "a3", "a2");
	}

	public void testShouldMoveCategoryWithChildrenToTopCorrectly() {
        Category tree = root;
		assertTrue(tree.moveChildToTheTop(1));
        assertChildren(tree, "B", "A", "C");
        assertChildren(tree.childAt(0), "b1");
        assertChildren(tree.childAt(1), "a1", "a2", "a3");
	}

	public void testShouldMoveCategoryWithChildrenToBottomCorrectly() {
        Category tree = root;
		assertTrue(tree.moveChildToTheBottom(0));
        assertChildren(tree, "B", "C", "A");
        assertChildren(tree.childAt(0), "b1");
        assertChildren(tree.childAt(1));
        assertChildren(tree.childAt(2), "a1", "a2", "a3");
	}

	public void testShouldSortByTitle() {
        Category tree = root;
        tree.moveChildToTheBottom(0);
        assertChildren(tree, "B", "C", "A");
        assertChildren(tree.childAt(0).childAt(0), "bb2", "bb1");
		tree.sortByTitle();
        assertChildren(tree, "A", "B", "C");
        assertChildren(tree.childAt(1).childAt(0), "bb1", "bb2");
	}

    private static Category createCategory(String title) {
        Category c = new Category();
        c.title = title;
        return c;
    }

    private static void assertChildren(Category tree, String...expectedChildren) {
        assertEquals("Expected children", expectedChildren.length, tree.children != null ? tree.children.size() : 0);
        for (int i=0 ;i<expectedChildren.length; i++) {
            assertEquals("Child "+i, expectedChildren[i], tree.childAt(i).title);
        }
    }

}
