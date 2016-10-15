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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.ICheckedObject;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Basic implementation of a database versioned index.
 * 
 * @author Christophe Fondacci
 */
public class BasicIndex extends SynchedVersionable<IIndex> implements IIndex, ICheckedObject {

	private List<IReference> columnsRef;
	private IReference tableRef;
	/** Maps column references identifiers with associated function expressions */
	private Map<IReference, String> functionColRefMap;
	private IndexType indexType = IndexType.NON_UNIQUE;

	public BasicIndex() {
		columnsRef = new ArrayList<IReference>();
		nameHelper.setFormatter(DBGMHelper.getCurrentVendor().getNameFormatter());
		functionColRefMap = new HashMap<IReference, String>();
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(INDEX_TYPE);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#addColumn(com.nextep.datadesigner.dbgm.model.IBasicColumn)
	 */
	@Override
	public void addColumnRef(IReference colRef) {
		columnsRef.add(colRef);
		notifyListeners(ChangeEvent.COLUMN_ADDED, colRef);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#getIndexType()
	 */
	@Override
	public IndexType getIndexType() {
		return indexType;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#getIndexedColumnsRef()
	 */
	@Override
	public List<IReference> getIndexedColumnsRef() {
		return columnsRef;
	}

	/**
	 * Hibernate columns setter
	 * 
	 * @param columnsRef
	 */
	protected void setIndexedColumnsRef(List<IReference> columnsRef) {
		this.columnsRef = columnsRef;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#getIndexedTableRef()
	 */
	@Override
	public IReference getIndexedTableRef() {
		return tableRef;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#removeColumn(com.nextep.datadesigner.dbgm.model.IBasicColumn)
	 */
	@Override
	public void removeColumnRef(IReference colRef) {
		if (columnsRef.remove(colRef)) {
			notifyListeners(ChangeEvent.COLUMN_REMOVED, colRef);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#setIndexType(com.nextep.datadesigner.dbgm.model.IndexType)
	 */
	@Override
	public void setIndexType(IndexType type) {
		this.indexType = type;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#setIndexedTableRef(com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public void setIndexedTableRef(IReference tableRef) {
		this.tableRef = tableRef;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		// IBasicTable t = (IBasicTable)VersionHelper.getReferencedItem(tableRef);
		// t.addIndex(this);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#getColumns()
	 */
	@Override
	public List<IBasicColumn> getColumns() {
		List<IBasicColumn> cols = new ArrayList<IBasicColumn>();
		if (getIndexedTableRef() != null) {
			IVersionable<IBasicTable> t = VersionHelper.getVersionable((IBasicTable) VersionHelper
					.getReferencedItem(getIndexedTableRef()));
			Map<IReference, IReferenceable> refMap = t.getReferenceMap();
			for (IReference r : columnsRef) {
				IBasicColumn c = (IBasicColumn) refMap.get(r);
				if (c == null) {
					c = (IBasicColumn) VersionHelper.getReferencedItem(r);
				}
				cols.add(c);
			}
		}
		return cols;
	}

	@Override
	public void addColumn(IBasicColumn column) {
		// IColumnable compatibility implementation
		addColumnRef(column.getReference());
	}

	@Override
	public void removeColumn(IBasicColumn column) {
		removeColumnRef(column.getReference());
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		List<IReference> refs = new ArrayList<IReference>(super.getReferenceDependencies());
		refs.addAll(columnsRef);
		refs.add(tableRef);
		return refs;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#updateReferenceDependencies(com.nextep.datadesigner.model.IReference,
	 *      com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		if (!super.updateReferenceDependencies(oldRef, newRef)) {
			if (getFunctionColRefMap().keySet().contains(oldRef)) {
				final String expr = getFunction(oldRef);
				getFunctionColRefMap().remove(oldRef);
				getFunctionColRefMap().put(newRef, expr);
			}
			if (columnsRef.contains(oldRef)) {
				int index = columnsRef.indexOf(oldRef);
				columnsRef.remove(index);
				columnsRef.add(index, newRef);
				return true;
			}
			if (oldRef.equals(tableRef)) {
				tableRef = newRef;
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#getIndexedTable()
	 */
	@Override
	public IBasicTable getIndexedTable() {
		return (IBasicTable) VersionHelper.getReferencedItem(tableRef);
	}

	/**
	 * @see com.nextep.designer.core.model.ICheckedObject#checkConsistency()
	 */
	@Override
	public void checkConsistency() throws InconsistentObjectException {
		if (getName() == null || "".equals(getName().trim())) { //$NON-NLS-1$
			throw new InconsistentObjectException(
					DBGMMessages.getString("consistency.index.emptyName")); //$NON-NLS-1$
		}
		if (tableRef == null) {
			throw new InconsistentObjectException(
					DBGMMessages.getString("consistency.index.noTable")); //$NON-NLS-1$
		}
		try {
			VersionHelper.getReferencedItem(tableRef);
		} catch (Exception e) {
			throw new InconsistentObjectException(
					DBGMMessages.getString("consistency.index.tableNotFound")); //$NON-NLS-1$
		}
		if (columnsRef.size() == 0) {
			throw new InconsistentObjectException(
					DBGMMessages.getString("consistency.index.noColumn")); //$NON-NLS-1$
		}
		for (IBasicColumn c : getColumns()) {
			if (c == null) {
				throw new InconsistentObjectException(
						DBGMMessages.getString("consistency.index.columnNotFound")); //$NON-NLS-1$
			}
			// if(!c.getParent().equals(getIndexedTable())) {
			// throw new
			// InconsistentObjectException("Columns of this index are inconsistent with the indexed table.");
			// }
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#getIndexName()
	 */
	@Override
	public String getIndexName() {
		return super.getName();
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IIndex#setIndexName(java.lang.String)
	 */
	@Override
	public void setIndexName(String name) {
		super.setName(name);
	}

	/**
	 * @see com.nextep.datadesigner.impl.NamedObservable#getName()
	 */
	@Override
	public String getName() {
		if (getIndexedTableRef() != null) {
			try {
				return "[" + getIndexedTable().getName() + "] " + getIndexName(); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (ErrorException e) {
				return getIndexName();
			}
		} else {
			return getIndexName();
		}
	}

	@Override
	public void setName(String name) {
		if (name != null && name.startsWith("[")) { //$NON-NLS-1$
			final int pos = name.indexOf("] "); //$NON-NLS-1$
			if (pos > 0 && pos < name.length() - 2)
				super.setName(name.substring(pos + 2));
		} else {
			super.setName(name);
		}
	}

	@Override
	public String getFunction(IReference r) {
		if (r == null)
			return null;
		return functionColRefMap.get(r);
	}

	@Override
	public void setFunction(IReference r, String func) {
		functionColRefMap.put(r, func);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * Hibernate map setter
	 * 
	 * @param funcColRefMap
	 */
	protected void setFunctionColRefMap(Map<IReference, String> funcColRefMap) {
		this.functionColRefMap = funcColRefMap;
	}

	/**
	 * Hibernate map getter
	 * 
	 * @return
	 */
	protected Map<IReference, String> getFunctionColRefMap() {
		return functionColRefMap;
	}
}
