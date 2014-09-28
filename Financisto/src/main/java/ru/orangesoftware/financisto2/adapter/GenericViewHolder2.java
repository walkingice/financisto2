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
package ru.orangesoftware.financisto2.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import ru.orangesoftware.financisto2.R;

@EViewGroup(R.layout.generic_list_item_2)
public class GenericViewHolder2 extends RelativeLayout {

    @ViewById(R.id.icon)
	public ImageView iconView;

    @ViewById(R.id.active_icon)
	public ImageView iconOverView;

    @ViewById(R.id.top)
	public TextView topView;

    @ViewById(R.id.center)
	public TextView centerView;

    @ViewById(R.id.bottom)
	public TextView bottomView;

    @ViewById(R.id.right)
	public TextView rightView;

    @ViewById(R.id.right_center)
	public TextView rightCenterView;

    @ViewById(R.id.progress)
	public ProgressBar progressBar;

    @ViewById(R.id.progress_text)
	public TextView progressText;

    public GenericViewHolder2(Context context) {
        super(context);
    }

//    public static View create(View view) {
//		GenericViewHolder2 v = new GenericViewHolder2();
//		v.rightCenterView.setVisibility(View.GONE);
//		v.progressBar.setVisibility(View.GONE);
//		v.progressText.setVisibility(View.GONE);
//		view.setTag(v);
//		return view;
//	}
	
}
