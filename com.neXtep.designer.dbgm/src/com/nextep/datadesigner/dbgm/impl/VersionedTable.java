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

import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.ICheckedObject;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.util.IdentitySet;

/**
 * Implementation of a basic table
 * 
 * @see com.nextep.datadesigner.dbgm.model.IBasicTable
 * @author Christophe Fondacci
 */
public class VersionedTable extends SynchedVersionable<IBasicTable> implements IBasicTable {

	// private static final Log log = LogFactory.getLog(VersionedTable.class);

	private List<IBasicColumn> columns;
	private final IElementType type;
	private Set<IKeyConstraint> constraints;
	private String shortName;
	private Set<IDataSet> dataSets;
	private final Set<IIndex> indexes;
	private final Set<ITrigger> triggers;
	private boolean temporary;

	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	/**
	 * Default empty constructor
	 */
	public VersionedTable() {
		super();
		nameHelper.setFormatter(DBGMHelper.getCurrentVendor().getNameFormatter());
		// We get an instance of a table type
		type = IElementType.getInstance("TABLE"); //$NON-NLS-1$

		// Initializing column list
		columns = new ArrayList<IBasicColumn>();
		constraints = new HashSet<IKeyConstraint>();
		// Initializing data sets
		dataSets = new IdentitySet<IDataSet>();
		// Initializing indexes
		indexes = new IdentitySet<IIndex>();
		// Initializing triggers
		triggers = new IdentitySet<ITrigger>();
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#addColumn(com.nextep.datadesigner.dbgm.model.IBasicColumn)
	 */
	@Override
	public void addColumn(IBasicColumn c) {
		columns.add(c);
		c.setParent(this);
		Collections.sort(columns);
		notifyListeners(ChangeEvent.COLUMN_ADDED, c);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#addConstraint()
	 */
	@Override
	public void addConstraint(IKeyConstraint c) {
		constraints.add(c);
		c.setConstrainedTable(this);
		// Collections.sort(constraints,NameComparator.getInstance());
		notifyListeners(ChangeEvent.CONSTRAINT_ADDED, c);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#addIndex()
	 */
	@Override
	public void addIndex(IIndex index) {
		if (indexes.add(index)) {
			notifyListeners(ChangeEvent.INDEX_ADDED, index);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#getColumns()
	 */
	@Override
	public List<IBasicColumn> getColumns() {
		return columns;

	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#getConstraints()
	 */
	@Override
	public Set<IKeyConstraint> getConstraints() {
		return constraints;

	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#getIndexes()
	 */
	@Override
	public Set<IIndex> getIndexes() {
		return indexes;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#removeColumn(com.nextep.datadesigner.dbgm.model.IBasicColumn)
	 */
	@Override
	public void removeColumn(IBasicColumn c) {
		// We save the rank of deleted column
		final int delRank = c.getRank();
		// Then we remove the column from the list
		columns.remove(c);
		// And we align column rankings
		for (IBasicColumn col : columns) {
			final int colRank = col.getRank();
			if (colRank > delRank) {
				col.setRank(colRank - 1);
			}
		}
		notifyListeners(ChangeEvent.COLUMN_REMOVED, c);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#removeConstraint()
	 */
	@Override
	public void removeConstraint(IKeyConstraint c) {
		constraints.remove(c);
		c.setConstrainedTable(null);
		// c.setConstrainedTable(null);
		notifyListeners(ChangeEvent.CONSTRAINT_REMOVED, c);

	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#removeIndex()
	 */
	@Override
	public void removeIndex(IIndex index) {
		if (indexes.remove(index)) {
			notifyListeners(ChangeEvent.INDEX_REMOVED, index);
		}
	}

	// /**
	// * @see
	// com.nextep.datadesigner.dbgm.model.IDatabaseObject#getSQLGenerator()
	// */
	// @Override
	// public ISQLGenerator getSQLGenerator() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IDatabaseObject#getType()
	 */
	@Override
	public IElementType getType() {
		return this.type;
	}

	/**
	 * Hibernate column setter
	 * 
	 * @param columns
	 *            columns set to define for this table
	 */
	protected void setColumns(List<IBasicColumn> columns) {
		this.columns = columns;
		// Registering table as parent
		// for(IBasicColumn c : columns) {
		// c.setParent(this);
		// }
	}

	protected void setConstraints(Set<IKeyConstraint> constraints) {
		this.constraints = constraints;
		for (IKeyConstraint c : constraints) {
			c.setConstrainedTable(this);
		}
	}

	// /**
	// * @see com.nextep.datadesigner.dbgm.model.IBasicTable#getPrimaryKey()
	// */
	// public IKeyConstraint getPrimaryKey() {
	// return primaryKey;
	// }
	// /**
	// * @see
	// com.nextep.datadesigner.dbgm.model.IBasicTable#setPrimaryKey(com.nextep.datadesigner.dbgm.model.IKeyConstraint)
	// */
	// public void setPrimaryKey(IKeyConstraint pk) {
	// if(this.primaryKey!=null && this.primaryKey!=pk) {
	// log.debug("Warning: Primary key has been replaced");
	// }
	// this.primaryKey=pk;
	// notifyListeners(ChangeEvent.CONSTRAINT_ADDED, pk);
	// }
	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#getShortName()
	 */
	@Override
	public String getShortName() {
		return shortName;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#setShortName(java.lang.String)
	 */
	@Override
	public void setShortName(String shortName) {
		if (this.shortName == null || !this.shortName.equals(shortName)) {
			this.shortName = IFormatter.UPPERCASE.format(shortName);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#getDataSet()
	 */
	@Override
	public void removeDataSet(IDataSet dataSet) {
		dataSets.remove(dataSet);
		dataSet.setTable(null);
		notifyListeners(ChangeEvent.DATASET_REMOVED, dataSet);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#setDataSet(com.nextep.designer.dbgm.model.IDataSet)
	 */
	@Override
	public void addDataSet(IDataSet dataSet) {
		dataSets.add(dataSet);
		// dataSet.setTable(this); TODO Check if ok to do the reverse way, else
		// find a workaround
		notifyListeners(ChangeEvent.DATASET_ADDED, dataSet);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#getDataSets()
	 */
	@Override
	public Collection<IDataSet> getDataSets() {
		return dataSets;
	}

	/**
	 * Hibernate datasets setter
	 * 
	 * @param dataSets
	 *            loaded data sets
	 */
	protected void setDataSets(Set<IDataSet> dataSets) {
		this.dataSets = dataSets;
	}

	/*
	 * @see
	 * com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getReferenceMap()
	 */
	@Override
	public Map<IReference, IReferenceable> getReferenceMap() {
		// FIXME Ne plus recréer systématiquement la MAP des références
		Map<IReference, IReferenceable> refMap = new HashMap<IReference, IReferenceable>();
		for (IKeyConstraint c : getConstraints()) {
			refMap.put(c.getReference(), c);
		}
		for (IBasicColumn c : getColumns()) {
			refMap.put(c.getReference(), c);
		}
		return refMap;
	}

	/**
	 * This implementation returns the inner dependencies generated from inner
	 * table elements such has keys.
	 * 
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		List<IReference> refList = new ArrayList<IReference>();
		for (IKeyConstraint c : getConstraints()) {
			refList.addAll(c.getReferenceDependencies());
		}
		return refList;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#updateReferenceDependencies(com.nextep.datadesigner.model.IReference,
	 *      com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		for (IKeyConstraint c : getConstraints()) {
			if (c.updateReferenceDependencies(oldRef, newRef)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#addTrigger(com.nextep.datadesigner.dbgm.model.ITrigger)
	 */
	@Override
	public void addTrigger(ITrigger trigger) {
		if (triggers.add(trigger)) {
			notifyListeners(ChangeEvent.TRIGGER_ADDED, trigger);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#getTriggers()
	 */
	@Override
	public Collection<ITrigger> getTriggers() {
		return triggers;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#removeTrigger(com.nextep.datadesigner.dbgm.model.ITrigger)
	 */
	@Override
	public void removeTrigger(ITrigger trigger) {
		if (triggers.remove(trigger)) {
			notifyListeners(ChangeEvent.TRIGGER_REMOVED, trigger);
		}
	}

	@Override
	public void internalColumnRefChanged(IBasicColumn col, IReference oldRef, IReference newRef) {
		// Doing nothing (only here for cluster implementation)
	}

	/**
	 * @see com.nextep.datadesigner.impl.NamedObservable#checkConsistency()
	 */
	@Override
	public void checkConsistency() throws InconsistentObjectException {
		super.checkConsistency();
		// Verifying consistency
		if (getColumns().size() == 0 && !isNoColumnSupported()) {
			throw new InconsistentObjectException(MessageFormat.format(
					DBGMMessages.getString("consistency.table.noColumn"), getName())); //$NON-NLS-1$
		}
		// Checking duplicate column names
		final Set<String> colNames = new HashSet<String>();
		for (IBasicColumn c : getColumns()) {
			final String colName = c.getName();
			if (colNames.contains(colName)) {
				throw new InconsistentObjectException(MessageFormat.format(
						DBGMMessages.getString("consistency.table.duplicateColumn"), getName(), //$NON-NLS-1$
						colName));
			}
			colNames.add(c.getName());
		}
		// Checking inner errors
		final Map<IReference, IReferenceable> refMap = getReferenceMap();
		for (IReferenceable ref : refMap.values()) {
			if (ref instanceof ICheckedObject) {
				try {
					((ICheckedObject) ref).checkConsistency();
				} catch (InconsistentObjectException e) {
					throw new InconsistentObjectException(
							MessageFormat.format(
									DBGMMessages
											.getString("consistency.table.innerElementInconsistent"), getName(), //$NON-NLS-1$
									NameHelper.getQualifiedName(ref), e.getMessage()));
				}
			}
		}
	}

	/**
	 * This method is meant to be overridden for special implementations which
	 * allows tables to have 0-column. This information is used by consistency
	 * checks.
	 * 
	 * @return <code>true</code> when the table could normally have 0 column,
	 *         else <code>false</code> (the default)
	 */
	protected boolean isNoColumnSupported() {
		return false;
	}

	@Override
	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

	@Override
	public boolean isTemporary() {
		return temporary;
	}

	public String getHibernateTemporary() {
		return temporary ? "Y" : "N"; //$NON-NLS-1$//$NON-NLS-2$
	}

	public void setHibernateTemporary(String temporary) {
		this.temporary = "Y".equals(temporary); //$NON-NLS-1$
	}
	// @Override
	// public void propertyChange(PropertyChangeEvent evt) {
	//
	// }
	//
	// public void addPropertyChangeListener(String propertyName,
	// PropertyChangeListener listener) {
	// propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	// }
	//
	// public void removePropertyChangeListener(PropertyChangeListener listener)
	// {
	// propertyChangeSupport.removePropertyChangeListener(listener);
	// }
	//
	// @Override
	// public void setName(String name) {
	// final String oldValue = getName();
	// super.setName(name);
	//		propertyChangeSupport.firePropertyChange("name", oldValue, name); //$NON-NLS-1$
	// }
}
