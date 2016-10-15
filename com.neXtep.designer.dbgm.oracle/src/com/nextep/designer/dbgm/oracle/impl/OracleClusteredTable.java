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

import java.util.HashMap;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;

public class OracleClusteredTable extends IDNamedObservable implements IOracleClusteredTable {

	/** Reference of table within the cluster */
	private IReference tableReference;
	/** Cluster to table column reference mapping */
	private Map<IReference, IReference> columnsMap;
	/** Parent cluster to which this table cluster belongs */
	private IOracleCluster cluster;

	public OracleClusteredTable() {
		setReference(new Reference(getType(), null, this));
		columnsMap = new HashMap<IReference, IReference>();
	}

	@Override
	public IBasicColumn getColumnMapping(IReference clusterCol) {
		final IReference r = getColumnReferenceMapping(clusterCol);
		if (r != null) {
			return (IBasicColumn) VersionHelper.getReferencedItem(r);
		} else {
			return null;
		}
	}

	@Override
	public Map<IReference, IReference> getColumnMappings() {
		return columnsMap;
	}

	@Override
	public IReference getColumnReferenceMapping(IReference clusterCol) {
		return columnsMap.get(clusterCol);
	}

	@Override
	public IReference getTableReference() {
		return tableReference;
	}

	@Override
	public void setColumnReferenceMapping(IReference clusterCol, IReference tableCol) {
		columnsMap.put(clusterCol, tableCol);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setTableReference(IReference ref) {
		final IReference previous = this.tableReference;
		this.tableReference = ref;
		notifyIfChanged(previous, ref, ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * Hibernate map setter
	 * 
	 * @param columnsMap map of table column references hashed by their cluster column reference
	 */
	protected void setColumnMappings(Map<IReference, IReference> columnsMap) {
		this.columnsMap = columnsMap;
	}

	@Override
	public IOracleCluster getCluster() {
		return cluster;
	}

	@Override
	public void setCluster(IOracleCluster cluster) {
		final IOracleCluster previous = this.cluster;
		this.cluster = cluster;
		notifyIfChanged(previous, cluster, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public String getName() {
		try {
			IBasicTable t = (IBasicTable) VersionHelper.getReferencedItem(getTableReference());
			return t.getName();
		} catch (ErrorException e) {
			return "";
		}
	}
}
