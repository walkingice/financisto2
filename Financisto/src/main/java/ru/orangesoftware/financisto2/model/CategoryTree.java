/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 ******************************************************************************/
package ru.orangesoftware.financisto2.model;

import java.util.*;

import android.database.Cursor;
import android.support.v4.util.LongSparseArray;

public class CategoryTree {

	private final Category root;

    private long lastId = 0;

    public CategoryTree() {
        this.root = new Category(0);
    }

    public static CategoryTree createFromList(List<Category> categories) {
        LongSparseArray<Category> map = new LongSparseArray<Category>();
        for (Category category : categories) {
            map.put(category.id, category);
        }
        CategoryTree tree = new CategoryTree();
        for (Category category : categories) {
            if (category.parentId > 0) {
                Category parent = map.get(category.parentId);
                if (parent != null) {
                    parent.addChild(category);
                }
            } else {
                tree.addRoot(category);
            }
        }
        return tree;
    }

    @Deprecated()
    public static CategoryTree createFromListSortedByLeft(List<Category> categories) {
        CategoryTree tree = new CategoryTree();
        Category parent = null;
        for (Category category : categories) {
            while (parent != null) {
                if (category.left > parent.left && category.right < parent.right) {
                    parent.addChild(category);
                    break;
                } else {
                    parent = parent.parent;
                }
            }
            if (parent == null) {
                tree.addRoot(category);
            }
            if (category.id > 0 && (category.right - category.left > 1)) {
                parent = category;
            }
        }
        return tree;
    }

    public void addRoot(Category category) {
        root.addChild(category);
    }

    public Category rootAt(int i) {
        return root.childAt(i);
    }

    public void removeRoot(Category category) {
        root.removeChild(category);
    }

    public List<Category> asFlatList() {
        List<Category> list = new ArrayList<Category>();
        addToList(list, root.children, 0);
        return list;
    }

    public List<Category> asFlatListWithoutSubtree(long categoryIdToSkip) {
        List<Category> list = new ArrayList<Category>();
        addToList(list, root.children, categoryIdToSkip);
        return list;
    }

    private void addToList(List<Category> list, List<Category> categories, long categoryIdToSkip) {
        if (categories == null) return;
        for (Category category : categories) {
            if (category.id == categoryIdToSkip) continue;
            list.add(category);
            if (category.hasChildren()) {
                addToList(list, category.children, categoryIdToSkip);
            }
        }
    }

    public static interface NodeCreator<T> {
        T createNode(Cursor c);
    }

	public LongSparseArray<Category> asIdMap() {
        return asDeepMap(root.children);
	}

    public static LongSparseArray<Category> asDeepMap(List<Category> categories) {
        LongSparseArray<Category> map = new LongSparseArray<Category>();
        initializeMap(map, categories);
        return map;
    }

	private static void initializeMap(LongSparseArray<Category> map, List<Category> categories) {
        if (categories == null) return;
		for (Category c : categories) {
			map.put(c.id, c);
			if (c.hasChildren()) {
				initializeMap(map, c.children);
			}
		}
	}

    public Category getRoot() {
        return root;
    }

    public void reIndex() {
        reIndex(root.children, 1, 0);
        assignIds(root.children, root);
	}

    private int reIndex(List<Category> categories, int left, int level) {
        if (categories != null) {
            for (Category node : categories) {
                node.level = level;
                node.left = left;
                if (node.hasChildren()) {
                    node.right = reIndex(node.children, left + 1, level + 1);
                } else {
                    node.right = left + 1;
                }
                left = node.right + 1;
                lastId = Math.max(lastId, node.id);
            }
        }
		return left;
	}

    private void assignIds(List<Category> categories, Category parent) {
        if (categories == null) return;
        for (Category t : categories) {
            long parentId = parent.id;
            t.parentId = parentId;
            if (parentId > 0) {
                t.type = parent.type;
            }
            if (!t.systemEntity && t.id <= 0) assignNextId(t);
            if (t.hasChildren()) assignIds(t.children, t);
        }
    }

    private Category assignNextId(Category category) {
        category.id = ++lastId;
        return category;
    }

    public String printTree() {
        StringBuilder sb = new StringBuilder();
        printTree(sb, root.children);
        return sb.toString();
    }

    private void printTree(StringBuilder sb, List<Category> tree) {
        for (Category t : tree) {
            sb.append("\n").append("L").append(t.level).append(" I").append(t.id).append(" P").append(t.parentId);
            sb.append(" [").append(w2(t.left)).append(",").append(w2(t.right)).append("] ").append(t.getTitle());
            if (t.hasChildren()) {
                printTree(sb, t.children);
            }
        }
    }

    private String w2(int x) {
        return String.format("%2d", x);
    }

}
