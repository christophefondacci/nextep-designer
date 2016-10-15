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
package com.nextep.designer.dbgm.oracle.impl;

import com.nextep.datadesigner.dbgm.impl.SynchedVersionable;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.DBOMMessages;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;

public class OracleMaterializedViewLog extends SynchedVersionable<IMaterializedViewLog> implements
		IMaterializedViewLog {

	private IReference tableRef;
	private boolean isRowId;
	private boolean isPrimaryKey;
	private boolean isSequence;
	private boolean includeNewVals;
	private IPhysicalProperties props;

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public IReference getTableReference() {
		return tableRef;
	}

	@Override
	public boolean isIncludingNewValues() {
		return includeNewVals;
	}

	@Override
	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	@Override
	public boolean isRowId() {
		return isRowId;
	}

	@Override
	public boolean isSequence() {
		return isSequence;
	}

	@Override
	public void setIncludingNewValues(boolean include) {
		if (this.includeNewVals != include) {
			this.includeNewVals = include;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setPrimaryKey(boolean primaryKey) {
		if (this.isPrimaryKey != primaryKey) {
			this.isPrimaryKey = primaryKey;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setRowId(boolean rowId) {
		if (isRowId != rowId) {
			this.isRowId = rowId;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setSequence(boolean sequence) {
		if (isSequence != sequence) {
			this.isSequence = sequence;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setTableReference(IReference tableRef) {
		this.tableRef = tableRef;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public IPhysicalProperties getPhysicalProperties() {
		return props;
	}

	@Override
	public void setPhysicalProperties(IPhysicalProperties properties) {
		if (properties != null && props == null) {
			this.props = properties;
			props.setParent(this);
			notifyListeners(ChangeEvent.GENERIC_CHILD_ADDED, properties);
		} else if (properties == null && props != null) {
			notifyListeners(ChangeEvent.GENERIC_CHILD_REMOVED, props);
			this.props = null;
		}
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		if (getTable() == null) {
			throw new InconsistentObjectException(
					DBOMMessages.getString("materializedViewLogNoParentTable"));
		}
	}

	@Override
	public IBasicTable getTable() {
		if (getTableReference() == null)
			return null;
		try {
			IBasicTable t = (IBasicTable) VersionHelper.getReferencedItem(getTableReference());
			return t;
		} catch (ErrorException e) {
			return null;
		}
	}

	@Override
	public String getName() {
		final IBasicTable t = getTable();
		if (t == null) {
			return "Unresolved table";
		}
		return t.getName() + " (log)";
	}

	@Override
	public IElementType getPhysicalPropertiesType() {
		return IElementType.getInstance(ITablePhysicalProperties.TYPE_ID);
	}
}
