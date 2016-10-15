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
package com.nextep.designer.dbgm.model.impl;

import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.services.IStorageService;

/**
 * Default implementation of a {@link IStorageHandle}. This class should not be instantiated
 * directly. Callers may obtain a storage handle by using the {@link IStorageService} methods.
 * 
 * @author Christophe Fondacci
 */
public class StorageHandle implements IStorageHandle {

	private String name;
	private String insertStmt;
	private String selectStmt;
	private int displayedColumns = -1;
	private boolean repositoryHandle = false;

	public StorageHandle(String name, IDataSet set) {
		this.name = name;
		repositoryHandle = set.getUID() != null;
	}

	@Override
	public String getStorageUnitName() {
		return name;
	}

	@Override
	public String getInsertStatement() {
		return insertStmt;
	}

	@Override
	public String getSelectStatement() {
		return selectStmt;
	}

	@Override
	public int getDisplayedColumnsCount() {
		return displayedColumns;
	}

	public void setInsertStatement(String insertStmt) {
		this.insertStmt = insertStmt;
	}

	public void setSelectStatement(String selectStmt) {
		this.selectStmt = selectStmt;
	}

	public void setDisplayedColumnsCount(int displayedColumns) {
		this.displayedColumns = displayedColumns;
	}

	@Override
	public boolean isRepositoryHandle() {
		return repositoryHandle;
	}
}
