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
package com.nextep.designer.sqlclient.ui.model.impl;

import java.util.ArrayList;
import java.util.List;
import com.nextep.designer.sqlclient.ui.model.ISQLRowModificationStatus;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;

public class SQLRowModificationStatus implements ISQLRowModificationStatus {

	private boolean isModifiable = false;
	private ISQLRowResult row;
	private String message;
	private List<String> rowColumnNames;
	private String updatedColumnName;
	private String updatedTableName;

	public SQLRowModificationStatus(ISQLRowResult row) {
		this.row = row;
		rowColumnNames = new ArrayList<String>();
	}

	@Override
	public boolean isModifiable() {
		return isModifiable;
	}

	public void setModifiable(boolean isModifiable) {
		this.isModifiable = isModifiable;
	}

	@Override
	public ISQLRowResult getSQLRow() {
		return row;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean isInsertNeeded() {
		return isModifiable && row.isPending();
	}

	@Override
	public List<String> getRowColumnNames() {
		return rowColumnNames;
	}

	public void addRowColumnName(String columnName) {
		rowColumnNames.add(columnName);
	}

	@Override
	public String getUpdatedColumnName() {
		return updatedColumnName;
	}

	public void setUpdatedColumnName(String updatedColumnName) {
		this.updatedColumnName = updatedColumnName;
	}

	@Override
	public String getUpdatedTableName() {
		return updatedTableName;
	}

	public void setUpdatedTableName(String updatedTableName) {
		this.updatedTableName = updatedTableName;
	}

}
