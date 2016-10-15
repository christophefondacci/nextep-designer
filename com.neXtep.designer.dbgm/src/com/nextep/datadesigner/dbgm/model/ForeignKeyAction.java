/*******************************************************************************
 * Copyright (c) 2011 neXtep Software and contributors.
 * All rights reserved.
 *
 * This file is part of neXtep designer.
 *
 * NeXtep designer is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 *
 * NeXtep designer is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.datadesigner.dbgm.model;

import org.hibernate.mapping.ForeignKey;

/**
 * An action constant to be executed by the "ON UPDATE" or "ON DELETE" clauses of a
 * {@link ForeignKey}
 * 
 * @author Christophe Fondacci
 */
public enum ForeignKeyAction {
	CASCADE("Cascade", "CASCADE"), NO_ACTION("No action", "NO ACTION"), RESTRICT("Restrict",
			"RESTRICT"), SET_DEFAULT("Set default value", "SET DEFAULT"), SET_NULL("Set null",
			"SET NULL");

	private String label;
	private String sql;

	ForeignKeyAction(String label, String sql) {
		this.label = label;
		this.sql = sql;
	}

	/**
	 * Retrieves a user-friendly representation of this action for user selection
	 * 
	 * @return a user-friendly label for this action
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Retrieves the SQL code portion corresponding to this action as defined
	 * by the SQL92 specification.
	 * 
	 * @return the SQL textual codification of this action
	 */
	public String getSql() {
		return sql;
	}
}
