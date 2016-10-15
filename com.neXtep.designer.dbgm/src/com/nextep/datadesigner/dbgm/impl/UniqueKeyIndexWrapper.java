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

import java.util.Collection;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.SynchStatus;
import com.nextep.datadesigner.model.UID;

public class UniqueKeyIndexWrapper implements IIndex {

	private final UniqueKeyConstraint uk;

	public UniqueKeyIndexWrapper(UniqueKeyConstraint uk) {
		this.uk = uk;
	}

	protected UniqueKeyConstraint getUniqueKey() {
		return uk;
	}

	@Override
	public void addColumnRef(IReference colRef) {
		uk.addConstrainedReference(colRef);
	}

	@Override
	public String getIndexName() {
		return uk.getName();
	}

	@Override
	public IndexType getIndexType() {
		return IndexType.UNIQUE;
	}

	@Override
	public List<IBasicColumn> getColumns() {
		return uk.getColumns();
	}

	@Override
	public List<IReference> getIndexedColumnsRef() {
		return uk.getConstrainedColumnsRef();
	}

	@Override
	public IBasicTable getIndexedTable() {
		return uk.getConstrainedTable();
	}

	@Override
	public IReference getIndexedTableRef() {
		return uk.getConstrainedTable().getReference();
	}

	@Override
	public void removeColumnRef(IReference colRef) {
		uk.removeConstrainedReference(colRef);
	}

	@Override
	public void setIndexName(String name) {
		uk.setName(name);
	}

	@Override
	public void setIndexType(IndexType type) {
		// Nonsense => Unique index type for unique constraint
		if (type != IndexType.UNIQUE) {
			throw new ErrorException(
					"This index is a unique constraint index and must remain unique.");
		}
	}

	@Override
	public void setIndexedTableRef(IReference tableRef) {
		// Non effect
	}

	@Override
	public SynchStatus getSynchStatus() {
		return uk.getSynchStatus();
	}

	@Override
	public void setSynched(SynchStatus synched) {
		// Noop : underlying constraint will define synch state
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(INDEX_TYPE);
	}

	@Override
	public UID getUID() {
		return uk.getUID();
	}

	@Override
	public void setUID(UID id) {
		// Noop
	}

	@Override
	public String getDescription() {
		return uk.getDescription();
	}

	@Override
	public String getName() {
		return uk.getName();
	}

	@Override
	public void setDescription(String description) {
		uk.setDescription(description);
	}

	@Override
	public void setName(String name) {
		uk.setName(name);
	}

	@Override
	public void addListener(IEventListener listener) {
		uk.addListener(listener);
	}

	@Override
	public Collection<IEventListener> getListeners() {
		return uk.getListeners();
	}

	@Override
	public void notifyListeners(ChangeEvent event, Object o) {
		uk.notifyListeners(event, o);
	}

	@Override
	public void removeListener(IEventListener listener) {
		uk.removeListener(listener);
	}

	@Override
	public IIndex getModel() {
		return this;
	}

	@Override
	public void lockUpdates() {
		// Noop
	}

	@Override
	public void unlockUpdates() {
		// Noop
	}

	@Override
	public boolean updatesLocked() {
		return uk.updatesLocked();
	}

	@Override
	public IReference getReference() {
		return uk.getReference();
	}

	@Override
	public void setReference(IReference ref) {
		// Noop

	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		uk.checkConsistency();

	}

	@Override
	public void addColumn(IBasicColumn column) {
		uk.addColumn(column);
	}

	@Override
	public void removeColumn(IBasicColumn column) {
		uk.removeColumn(column);
	}

	@Override
	public String getFunction(IReference r) {
		return null;
	}

	@Override
	public void setFunction(IReference r, String func) {
		// No-op
	}
}
