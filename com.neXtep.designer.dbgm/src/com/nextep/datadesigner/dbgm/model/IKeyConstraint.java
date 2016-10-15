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
/**
 *
 */
package com.nextep.datadesigner.dbgm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdaptable;
import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.impl.SynchronizableNamedObservable;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.vcs.impl.ComparisonPropertyProvider;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.ICheckedObject;
import com.nextep.designer.core.model.IParentable;

/**
 * Super interface of key constraints. Provides the generic abstraction interface for key-related
 * types and provides the default implementation.<br>
 * Implementors (primary keys, foreign keys, check constraints) should only add new functions and
 * not override the ones published by this class.
 * 
 * @author Christophe Fondacci
 */
public abstract class IKeyConstraint extends SynchronizableNamedObservable implements
		IDatabaseObject<IKeyConstraint>, IReferenceable, IReferencer, ICheckedObject, IAdaptable,
		IParentable<IBasicTable>, IColumnable {

	boolean lock;
	private IBasicTable constrainedTable;
	private List<IReference> colRef = new ArrayList<IReference>();
	private static final Log log = LogFactory.getLog(IKeyConstraint.class);

	protected IKeyConstraint() {
		nameHelper.setFormatter(DBGMHelper.getCurrentVendor().getNameFormatter());
		setReference(new Reference(this.getType(), null, this));
	}

	/**
	 * Abstract constructor which initializes the default abstract structure. Please make sure that
	 * the constraint type is initialized before calling this constructor.
	 * 
	 * @param name name of the constraint
	 * @param description constraint's description
	 * @param constrainedTable table constrained by this element
	 */
	protected IKeyConstraint(String name, String description, IBasicTable constrainedTable) {
		this();
		setName(name);
		getReference().setArbitraryName(name);
		setDescription(description);
		setConstrainedTable(constrainedTable);
		// Bug of addConstraint calling listeners before being completely initialized
		// constrainedTable.addConstraint(this);
	}

	/**
	 * @return the constraint type
	 */
	public abstract ConstraintType getConstraintType();

	/**
	 * Changes the constraint type. This could happen when a unique key becomes a primary key, or a
	 * primary key a unique key. This method may throw an error exception for incompatible types.
	 * 
	 * @param type new constraint type
	 */
	public abstract void setConstraintType(ConstraintType type);

	/**
	 * This convenience method allows to build a column list instead of a column <u>reference</u>
	 * list of this key constraint. If the current key constraint contains column references which
	 * cannot be resolved in the current context, <code>null</code> values will be added in the list
	 * instead.
	 * 
	 * @return the set of columns constrained by this key, possibly containing <code>null</code>
	 *         values for unresolved columns
	 */
	@Override
	public List<IBasicColumn> getColumns() {
		// Building referenced columns list
		List<IBasicColumn> colList = new ArrayList<IBasicColumn>();
		for (IReference r : colRef) {
			IBasicColumn c = null;
			try {
				c = (IBasicColumn) VersionHelper.getReferencedItem(r);
			} catch (UnresolvedItemException e) {
				// Not doing anything here will result in inserting null values for columns which
				// are not reolved in the current view. This is the best thing we can do
				log.debug("Unresolved column while building column list"); //$NON-NLS-1$
			}
			colList.add(c);
		}
		return colList;
	}

	/**
	 * Adds a new column constrained by this key.
	 * 
	 * @param c new constrained column
	 */
	@Override
	public void addColumn(IBasicColumn c) {
		addConstrainedReference(c.getReference());
	}

	/**
	 * Adds a new column constrained by this key.
	 * 
	 * @param index index at which this column should be added
	 * @param c new constrained column
	 */
	public void addConstrainedColumn(int index, IBasicColumn c) {
		addConstrainedReference(index, c.getReference());
	}

	/**
	 * Removes a column from this constraint scope.
	 * 
	 * @param c column to remove from constraint
	 */
	@Override
	public void removeColumn(IBasicColumn c) {
		if (c == null) {
			throw new ErrorException(DBGMMessages.getString("error.key.removeNullCol")); //$NON-NLS-1$
		}
		removeConstrainedReference(c.getReference());

	}

	public void removeConstrainedReference(IReference r) {
		colRef.remove(r);
		notifyListeners(ChangeEvent.COLUMN_REMOVED, r);
	}

	/**
	 * Defines the table on which this constraint should be applied.
	 * 
	 * @param t constrained table
	 */
	public void setConstrainedTable(IBasicTable t) {
		if (t != this.constrainedTable && t != null) {
			this.constrainedTable = t;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @return the constrained table
	 */
	public IBasicTable getConstrainedTable() {
		return constrainedTable;
	}

	public void up(IBasicColumn c) {
		int index = colRef.indexOf(c.getReference());
		if (index > 0) {
			final IBasicColumn swappedCol = getColumns().get(index - 1);
			Collections.swap(colRef, index, index - 1);
			notifyListeners(ChangeEvent.MODEL_CHANGED, c);
			c.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			swappedCol.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	public void down(IBasicColumn c) {
		int index = colRef.indexOf(c.getReference());
		if (index < colRef.size() - 1) {
			final IBasicColumn swappedCol = getColumns().get(index + 1);
			Collections.swap(colRef, index, index + 1);
			notifyListeners(ChangeEvent.MODEL_CHANGED, c);
			c.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			swappedCol.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#getModel()
	 */
	public IKeyConstraint getModel() {
		return this;
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#lockUpdates()
	 */
	public void lockUpdates() {
		lock = true;
		notifyListeners(ChangeEvent.UPDATES_LOCKED, null);
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#unlockUpdates()
	 */
	public void unlockUpdates() {
		lock = false;
		notifyListeners(ChangeEvent.UPDATES_UNLOCKED, null);

	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#updatesLocked()
	 */
	public boolean updatesLocked() {
		return getConstrainedTable().updatesLocked();
	}

	protected void setConstrainedColumnsRef(List<IReference> colRef) {
		this.colRef = colRef;
	}

	public List<IReference> getConstrainedColumnsRef() {
		return colRef;
	}

	public void addConstrainedReference(IReference ref) {
		colRef.add(ref);
		notifyListeners(ChangeEvent.COLUMN_ADDED, ref);
	}

	public void addConstrainedReference(int index, IReference ref) {
		colRef.add(index, ref);
		notifyListeners(ChangeEvent.COLUMN_ADDED, ref);
	}

	/**
	 * A key constraint has references to each column reference it contains
	 * 
	 * @see com.nextep.datadesigner.model.IReferencer#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		return new ArrayList<IReference>(colRef);
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferencer#updateReferenceDependencies(com.nextep.datadesigner.model.IReference,
	 *      com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		if (colRef.contains(oldRef)) {
			int index = colRef.indexOf(oldRef);
			colRef.remove(index);
			colRef.add(index, newRef);
			return true;
		}
		return false;
	}

	/**
	 * @see com.nextep.designer.core.model.ICheckedObject#checkConsistency()
	 */
	@Override
	public void checkConsistency() throws InconsistentObjectException {
		if (getName() == null || "".equals(getName().trim())) { //$NON-NLS-1$
			throw new InconsistentObjectException(DBGMMessages.getString("consistency.key.name")); //$NON-NLS-1$
		}
		if (getConstrainedTable() == null) {
			throw new InconsistentObjectException(
					DBGMMessages.getString("consistency.key.parentTable")); //$NON-NLS-1$
		}
		if (getConstrainedColumnsRef().size() == 0) {
			throw new InconsistentObjectException(DBGMMessages.getString("consistency.key.column")); //$NON-NLS-1$
		}
		for (IBasicColumn c : getColumns()) {
			if (c == null
					|| !((IBasicTable) c.getParent()).getReference().equals(
							((IBasicTable) getConstrainedTable()).getReference())) {
				throw new InconsistentObjectException(
						DBGMMessages.getString("consistency.key.tableMismatch")); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertyProvider.class) {
			return new ComparisonPropertyProvider(this);
		}
		return null;
	}

	/**
	 * This method finds the list of index of the constrained table which are able to enforce this
	 * constraint.<br>
	 * <b>Important notes:</b><br>
	 * 1. A constraint can be enforced by either an index or a constraint (unique or primary key)<br>
	 * 2. There is no guarantee that the first element of the collection is the element which will
	 * effectively enforce the constraint in the database engine.<br>
	 * <br>
	 * If no index enforces this constraint, this method will return an empty collection.
	 * 
	 * @return all index and constraints of the constrained table whose columns can at least enforce
	 *         this constraint.
	 */
	public Collection<IDatabaseRawObject> getEnforcingIndex() {
		final Collection<IDatabaseRawObject> enforcingIndex = new ArrayList<IDatabaseRawObject>();
		// Iterating on all table unique keys
		// XXX not good... should merge unique key and indexes in a common interface
		for (IKeyConstraint key : this.getConstrainedTable().getConstraints()) {
			boolean match = false;
			if (key.getConstraintType() == ConstraintType.UNIQUE
					|| key.getConstraintType() == ConstraintType.PRIMARY) {
				Iterator<IReference> indexColIt = key.getConstrainedColumnsRef().iterator();
				Iterator<IReference> fkColIt = this.getConstrainedColumnsRef().iterator();
				// Parallel iteration on columns, looking for a match
				while (fkColIt.hasNext()) {
					match = true;
					if (!indexColIt.hasNext()) {
						match = false;
						break;
					} else {
						if (!fkColIt.next().equals(indexColIt.next())) {
							match = false;
							break;
						}
					}
				}
				if (match) {
					enforcingIndex.add(key);
				}
			}
		}
		// Iterating on all table indexes
		for (IIndex i : this.getConstrainedTable().getIndexes()) {
			boolean match = false;
			Iterator<IReference> indexColIt = i.getIndexedColumnsRef().iterator();
			Iterator<IReference> fkColIt = this.getConstrainedColumnsRef().iterator();
			// Parallel iteration on columns, looking for a match
			while (fkColIt.hasNext()) {
				match = true;
				if (!indexColIt.hasNext()) {
					match = false;
					break;
				} else {
					if (!fkColIt.next().equals(indexColIt.next())) {
						match = false;
						break;
					}
				}
			}
			if (match) {
				enforcingIndex.add(i);
			}
		}
		// Returning (even if null)
		return enforcingIndex;
	}

	@Override
	public IBasicTable getParent() {
		return getConstrainedTable();
	}

	@Override
	public void setParent(IBasicTable parent) {
		setConstrainedTable(parent);
	}
}
