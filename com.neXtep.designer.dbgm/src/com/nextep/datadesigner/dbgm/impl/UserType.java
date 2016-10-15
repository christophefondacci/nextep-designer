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
package com.nextep.datadesigner.dbgm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;

/**
 * Default user type implementation.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class UserType extends SynchedVersionable<IUserType> implements IUserType {

	/** The list of columns which constitutes this type */
	private List<ITypeColumn> columns;

	public UserType() {
		columns = new ArrayList<ITypeColumn>();
		nameHelper.setFormatter(DBGMHelper.getCurrentVendor().getNameFormatter());
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public void addColumn(ITypeColumn column) {
		columns.add(column);
		column.setParent(this);
		notifyListeners(ChangeEvent.COLUMN_ADDED, column);
	}

	@Override
	public List<ITypeColumn> getColumns() {
		return columns;
	}

	@Override
	public void removeColumn(ITypeColumn column) {
		if (columns.remove(column)) {
			notifyListeners(ChangeEvent.COLUMN_REMOVED, column);
			// column.setParent(null);
		}
	}

	@Override
	public void setColumns(List<ITypeColumn> columns) {
		this.columns = columns;
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		super.checkConsistency();
		if (getColumns().size() == 0) {
			throw new InconsistentObjectException("A user type must have at least 1 column.");
		}
	}

	@Override
	public Map<IReference, IReferenceable> getReferenceMap() {
		Map<IReference, IReferenceable> refMap = new HashMap<IReference, IReferenceable>();
		for (ITypeColumn c : getColumns()) {
			refMap.put(c.getReference(), c);
		}
		return refMap;
	}

}
