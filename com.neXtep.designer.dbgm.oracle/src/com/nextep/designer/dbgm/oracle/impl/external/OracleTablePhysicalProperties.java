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
package com.nextep.designer.dbgm.oracle.impl.external;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.PhysicalOrganisation;
import com.nextep.designer.dbgm.oracle.model.base.AbstractOraclePhysicalProperties;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleTablePhysicalProperties extends AbstractOraclePhysicalProperties implements
		IOracleTablePhysicalProperties {

	/** Physical organisation of a table (heap, index) */
	private PhysicalOrganisation organization = PhysicalOrganisation.HEAP;
	/** Partitioning columns */
	private List<IReference> columns;
	private IPhysicalObject table;

	public OracleTablePhysicalProperties() {
		super();
		this.columns = new ArrayList<IReference>();
	}

	@Override
	public PhysicalOrganisation getPhysicalOrganisation() {
		return organization;
	}

	@Override
	public void setPhysicalOrganisation(PhysicalOrganisation org) {
		this.organization = org;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public IPhysicalObject getParent() {
		return table;
	}

	@Override
	public void setParent(IPhysicalObject table) {
		this.table = table;
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(ITablePhysicalProperties.TYPE_ID);
	}

	@Override
	public void addPartitionedColumn(IBasicColumn col) {
		if (!columns.contains(col.getReference())) {
			addPartitionedColumnRef(col.getReference());
			notifyListeners(ChangeEvent.COLUMN_ADDED, col);
		}
	}

	@Override
	public List<IReference> getPartitionedColumnsRef() {
		return columns;
	}

	protected void setPartitionedColumnsRef(List<IReference> colRefs) {
		this.columns = colRefs;
	}

	@Override
	public void removePartitionedColumn(IBasicColumn col) {
		if (columns.remove(col.getReference())) {
			notifyListeners(ChangeEvent.COLUMN_REMOVED, col);
		}
	}

	@Override
	public Collection<IReference> getReferenceDependencies() {
		if (columns == null) {
			return new ArrayList<IReference>();
		} else {
			return new ArrayList<IReference>(columns);
		}
	}

	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		if (columns.contains(oldRef)) {
			int index = columns.indexOf(oldRef);
			columns.remove(index);
			columns.add(index, newRef);
			return true;
		}
		return super.updateReferenceDependencies(oldRef, newRef);
	}

	@Override
	public void addPartitionedColumnRef(IReference colRef) {
		if (!columns.contains(colRef)) {
			columns.add(colRef);
			notifyListeners(ChangeEvent.COLUMN_ADDED, colRef);
		}

	}

	@Override
	public void removePartitionedColumnRef(IReference colRef) {
		if (columns.contains(colRef)) {
			columns.remove(colRef);
			notifyListeners(ChangeEvent.COLUMN_REMOVED, colRef);
		}
	}

	@Override
	public IElementType getPartitionType() {
		return IElementType.getInstance(ITablePartition.TYPE_ID);
	}

}
