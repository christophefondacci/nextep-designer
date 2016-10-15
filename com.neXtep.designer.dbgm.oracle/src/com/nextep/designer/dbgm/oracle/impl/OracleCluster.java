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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.oracle.DBOMMessages;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;

public class OracleCluster extends OracleTable implements IOracleCluster {

	private Set<IOracleClusteredTable> clusteredTables;

	public OracleCluster() {
		clusteredTables = new HashSet<IOracleClusteredTable>();
	}

	@Override
	public Set<IOracleClusteredTable> getClusteredTables() {
		return clusteredTables;
	}

	/**
	 * Defines the list of tables within this table cluster<br>
	 * <b>Hibernate setter</b>
	 * 
	 * @param clusteredTables
	 */
	protected void setClusteredTables(Set<IOracleClusteredTable> clusteredTables) {
		this.clusteredTables = clusteredTables;
		for (IOracleClusteredTable t : clusteredTables) {
			t.setCluster(this);
		}
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(CLUSTER_TYPE_ID);
	}

	@Override
	public IOracleClusteredTable addClusteredTable(IReference r) {
		// Checking if this table already exists in the cluster
		for (IOracleClusteredTable ct : getClusteredTables()) {
			if (ct.getTableReference().equals(r)) {
				throw new ErrorException(DBOMMessages.getString("clusteredTableAlreadyDefined"));
			}
		}
		// We're clear to proceed
		IOracleClusteredTable ct = new OracleClusteredTable();
		ct.setTableReference(r);
		ct.setCluster(this);
		getClusteredTables().add(ct);
		notifyListeners(ChangeEvent.GENERIC_CHILD_ADDED, ct);
		return ct;
	}

	@Override
	public void removeClusteredTable(IReference r) {
		// Processing currently defined clustered tables
		for (IOracleClusteredTable ct : new ArrayList<IOracleClusteredTable>(clusteredTables)) {
			if (ct.getTableReference().equals(r)) {
				clusteredTables.remove(ct);
				notifyListeners(ChangeEvent.GENERIC_CHILD_REMOVED, ct);
				return;
			}
		}
	}

	@Override
	public Collection<IReference> getReferenceDependencies() {
		final Collection<IReference> refs = super.getReferenceDependencies();
		for (IOracleClusteredTable t : getClusteredTables()) {
			refs.add(t.getTableReference());
			for (IReference r : t.getColumnMappings().values()) {
				refs.add(r);
			}
		}
		return refs;
	}

	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		boolean updated = super.updateReferenceDependencies(oldRef, newRef);
		for (IOracleClusteredTable t : getClusteredTables()) {
			if (t.getReference().equals(oldRef)) {
				t.setReference(newRef);
				updated = true;
				continue;
			}
			if (t.getTableReference().equals(oldRef)) {
				t.setTableReference(newRef);
				updated = true;
				continue;
			}
			for (IReference r : t.getColumnMappings().keySet()) {
				if (r.equals(oldRef)) {
					final IReference mappedRef = t.getColumnMappings().get(r);
					t.getColumnMappings().remove(r);
					t.getColumnMappings().put(newRef, mappedRef);
					updated = true;
				} else {
					IReference mappedcol = t.getColumnMappings().get(r);
					if (oldRef.equals(mappedcol)) {
						t.setColumnReferenceMapping(r, newRef);
						updated = true;
					}
				}
			}
		}
		return updated;
	}

	@Override
	public Map<IReference, IReferenceable> getReferenceMap() {
		Map<IReference, IReferenceable> refMap = super.getReferenceMap();
		for (IOracleClusteredTable t : getClusteredTables()) {
			refMap.put(t.getReference(), t);
		}
		return refMap;
	}

	@Override
	public void addConstraint(IKeyConstraint c) {
		// Voiding implementation
		throw new ErrorException(DBOMMessages.getString("clusterConstraintsNotAllowed"));
	}

	@Override
	public void addDataSet(IDataSet dataSet) {
		throw new ErrorException(DBOMMessages.getString("clusterDatasetNotAllowed"));
	}

	@Override
	public void addTrigger(ITrigger trigger) {
		throw new ErrorException(DBOMMessages.getString("clusterTriggerNotAllowed"));
	}

	@Override
	public void addCheckConstraint(ICheckConstraint constraint) {
		throw new ErrorException(DBOMMessages.getString("clusterConstraintsNotAllowed"));
	}

	@Override
	public void internalColumnRefChanged(IBasicColumn col, IReference oldRef, IReference newRef) {
		if (getClusteredTables() != null) {
			for (IOracleClusteredTable t : getClusteredTables()) {
				for (IReference r : new ArrayList<IReference>(t.getColumnMappings().keySet())) {
					if (r.equals(oldRef)) {
						final IReference mappedRef = t.getColumnMappings().get(r);
						t.getColumnMappings().remove(r);
						t.getColumnMappings().put(newRef, mappedRef);
					}
				}
			}
		}
	}

	@Override
	public IOracleClusteredTable getClusteredTable(IReference r) {
		for (IOracleClusteredTable t : getClusteredTables()) {
			if (t.getTableReference() != null && t.getTableReference().equals(r)) {
				return t;
			}
		}
		return null;
	}

}
