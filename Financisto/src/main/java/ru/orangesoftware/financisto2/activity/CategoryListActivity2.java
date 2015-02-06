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
package ru.orangesoftware.financisto2.activity;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.ArrayList;
import java.util.Map;

import ru.orangesoftware.financisto2.R;
import ru.orangesoftware.financisto2.adapter.CategoryListAdapter2;
import ru.orangesoftware.financisto2.bus.DeleteEntity;
import ru.orangesoftware.financisto2.bus.EditEntity;
import ru.orangesoftware.financisto2.bus.GreenRobotBus;
import ru.orangesoftware.financisto2.db.CategoryRepository;
import ru.orangesoftware.financisto2.db.DatabaseAdapter;
import ru.orangesoftware.financisto2.model.Category;
import ru.orangesoftware.financisto2.model.CategoryTree;

@EActivity(R.layout.category_list)
@OptionsMenu(R.menu.category_list_menu)
public class CategoryListActivity2 extends ListActivity {
	
	private static final int NEW_CATEGORY_REQUEST = 1;
	private static final int EDIT_CATEGORY_REQUEST = 2;
	
    @Bean
    protected DatabaseAdapter db;

    @Bean
    protected CategoryRepository categoryRepository;

    @Bean
    protected GreenRobotBus bus;

    private CategoryTree tree;
	private Map<Long, String> attributes;

    @Override
    protected void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
    }

    @AfterViews
	protected void afterViews() {
        reload();
	}

    @OptionsItem(R.id.menu_attributes)
    protected void showAttributes() {
        AttributeListActivity_.intent(this).startForResult(0);
    }

    @OptionsItem(R.id.menu_expand)
    protected void expandAllCategories() {
        ((CategoryListAdapter2)getListAdapter()).expandAllCategories();
    }

    @OptionsItem(R.id.menu_collapse)
    protected void collapseAllCategories() {
        ((CategoryListAdapter2)getListAdapter()).collapseAllCategories();
    }

    @OptionsItem(R.id.menu_add)
	protected void addItem() {
        CategoryActivity_.intent(this).startForResult(NEW_CATEGORY_REQUEST);
	}

    public void reload() {
        long t0 = System.currentTimeMillis();
        tree = categoryRepository.loadCategories();
        attributes = db.getAttributesMapping();
        updateAdapter();
        long t1 = System.currentTimeMillis();
        Log.d("CategoryListActivity2", "Requery in "+(t1-t0)+"ms");
    }

    private void updateAdapter() {
        CategoryListAdapter2 adapter = (CategoryListAdapter2) getListAdapter();
        if (adapter == null) {
            adapter = new CategoryListAdapter2(this, bus, tree, attributes);
            setListAdapter(adapter);
        } else {
            adapter.setCategories(tree);
            adapter.setAttributes(attributes);
            notifyDataSetChanged();
        }
	}

    @SuppressWarnings("unused")
	public void onEventMainThread(final DeleteEntity event) {
		Category c = db.getCategory(event.id);
		new AlertDialog.Builder(this)
			.setTitle(c.getTitle())
			.setMessage(R.string.delete_category_dialog)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
                    categoryRepository.deleteCategoryById(event.id);
					reload();
				}				
			})
			.setNegativeButton(R.string.no, null)
			.show();		
	}

    @SuppressWarnings("unused")
	public void onEventMainThread(EditEntity event) {
        CategoryActivity_.intent(this).categoryId(event.id).startForResult(EDIT_CATEGORY_REQUEST);
	}

    @ItemClick(android.R.id.list)
	protected void viewItem(final Category c) {
		final ArrayList<PositionAction> actions = new ArrayList<PositionAction>();
		Category p = c.parent;
		final Category categoryUnderAction;
		if (p == null) {
			categoryUnderAction = tree.getRoot();
		} else {
			categoryUnderAction = p;
		}
		final int pos = categoryUnderAction.childIndex(c);
		if (pos > 0) {
			actions.add(top);
			actions.add(up);
		}
		if (pos < categoryUnderAction.childrenCount() - 1) {
			actions.add(down);
			actions.add(bottom);
		}
		if (c.hasChildren()) {
			actions.add(sortByTitle);
		}
		final ListAdapter a = new CategoryPositionListAdapter(actions);
		new AlertDialog.Builder(this)
			.setTitle(c.getTitle())
			.setAdapter(a, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PositionAction action = actions.get(which);
					if (action.execute(categoryUnderAction, pos)) {
                        categoryRepository.saveCategories(tree);
						notifyDataSetChanged();
					}
				}
			})
			.show();		
	}	

    @OptionsItem(R.id.menu_integrity_fix)
    protected void reIndex() {
        reload();
    }

    @OptionsItem(R.id.menu_sort)
    protected void sortByTitle() {
        tree.getRoot().sortByTitle();
        categoryRepository.saveCategories(tree);
        reload();
    }

    protected void notifyDataSetChanged() {
		((CategoryListAdapter2)getListAdapter()).notifyDataSetChanged();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            reload();
        }
    }

    private abstract class PositionAction {

        final int icon;
		final int title;

        public PositionAction(int icon, int title) {
			this.icon = icon;
			this.title = title;
		}

        public abstract boolean execute(Category category, int pos);

    }
	
	private final PositionAction top = new PositionAction(R.drawable.ic_action_move_to_top, R.string.position_move_top){
		@Override
		public boolean execute(Category category, int pos) {
			return category.moveChildToTheTop(pos);
		}
	};
	
	private final PositionAction up = new PositionAction(R.drawable.ic_action_move_up, R.string.position_move_up){
		@Override
		public boolean execute(Category category, int pos) {
			return category.moveChildUp(pos);
		}
	};
	
	private final PositionAction down = new PositionAction(R.drawable.ic_action_move_down, R.string.position_move_down){
		@Override
		public boolean execute(Category category, int pos) {
			return category.moveChildDown(pos);
		}
	};
	
	private final PositionAction bottom = new PositionAction(R.drawable.ic_action_move_to_bottom, R.string.position_move_bottom){
		@Override
		public boolean execute(Category category, int pos) {
			return category.moveChildToTheBottom(pos);
		}
	};
	
	private final PositionAction sortByTitle = new PositionAction(R.drawable.ic_action_sort_by_title, R.string.sort_by_title){
		@Override
		public boolean execute(Category category, int pos) {
            category.sortByTitle();
            return true;
        }
	};

	private class CategoryPositionListAdapter extends BaseAdapter {
		
		private final ArrayList<PositionAction> actions;
		
		public CategoryPositionListAdapter(ArrayList<PositionAction> actions) {
			this.actions = actions;
		}

		@Override
		public int getCount() {
			return actions.size();
		}

		@Override
		public PositionAction getItem(int position) {
			return actions.get(position);
		}

		@Override
		public long getItemId(int position) {
			return actions.get(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.position_list_item, parent, false);
			}
			ImageView v = (ImageView)convertView.findViewById(R.id.icon);
			TextView t = (TextView)convertView.findViewById(R.id.line1);
			PositionAction a = actions.get(position);
			v.setImageResource(a.icon);
			t.setText(a.title);			
			return convertView;
		}
		
	}
	
}
