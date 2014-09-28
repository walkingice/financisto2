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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "attributes")
public class Attribute extends MyEntity {

	public static final int TYPE_TEXT = 1;
	public static final int TYPE_NUMBER = 2;
	public static final int TYPE_LIST = 3;
	public static final int TYPE_CHECKBOX = 4;

    @Column(name = "name")
	public String name;

    @Column(name = "type")
	public int type;

    @Column(name = "list_values")
	public String listValues;

    @Column(name = "default_value")
	public String defaultValue;
	
	public String getDefaultValue() {
		if (type == TYPE_CHECKBOX) {
			String[] values = listValues != null ? listValues.split(";") : null;
			boolean checked = Boolean.valueOf(defaultValue);
			if (values != null && values.length > 1) {
				return values[checked ? 0 : 1];
			}
			return String.valueOf(checked);
		} else {
			return defaultValue;
		}
	}

    @Override
    public String toString() {
        return name;
    }

}
