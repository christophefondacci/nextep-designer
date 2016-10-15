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

import org.eclipse.core.runtime.IAdaptable;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.impl.ComparisonPropertyProvider;

/**
 * This class represents a basic column. A basic column is part of a basic table
 * and defines name, description, datatype and default value of a table column.
 * 
 * @author Christophe Fondacci
 */
public class BasicColumn extends SynchronizableNamedObservable implements IBasicColumn, IAdaptable {

	// private static final Log log = LogFactory.getLog(BasicColumn.class);

	private IDatatype type = null;
	private IColumnable parent = null;
	private int rank = 0;
	private boolean isNotNull = false;
	private String defaultExpr = ""; //$NON-NLS-1$
	private boolean isVirtual = false;

	public BasicColumn() {
		nameHelper.setFormatter(DBGMHelper.getCurrentVendor().getNameFormatter());
		setReference(new Reference(this.getType(), null, this));
	}

	public BasicColumn(String name, String description, IDatatype type, int rank) {
		this();
		this.setName(name);
		getReference().setArbitraryName(name);
		setDescription(description);
		setDatatype(type);
		this.rank = rank;
	}

	@Override
	public IColumnable getParent() {
		return parent;
	}

	@Override
	public void setParent(IColumnable parent) {
		this.parent = parent;
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(IBasicColumn.TYPE_ID);
	}

	@Override
	public synchronized void lockUpdates() {
		// lock=true;
		notifyListeners(ChangeEvent.UPDATES_LOCKED, null);
	}

	@Override
	public synchronized void unlockUpdates() {
		// lock=false;
		notifyListeners(ChangeEvent.UPDATES_UNLOCKED, null);
	}

	@Override
	public boolean updatesLocked() {
		if (getParent() instanceof ILockable<?>) {
			return ((ILockable<?>) getParent()).updatesLocked();
		} else {
			return false;
		}
	}

	@Override
	public IBasicColumn getModel() {
		return this;
	}

	@Override
	public IDatatype getDatatype() {
		return type;
	}

	@Override
	public void setDatatype(IDatatype datatype) {
		// Setting only on effective change
		if ((type != null && !type.equals(datatype)) || (type == null && datatype != null)) {
			this.type = datatype;
			// Listening to the datatype so that we fire change events when
			// datatype changes
			Designer.getListenerService().unregisterListeners(this);
			Designer.getListenerService().registerListener(this, datatype, new IEventListener() {

				@Override
				public void handleEvent(ChangeEvent event, IObservable source, Object data) {
					BasicColumn.this.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				}
			});
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public int getRank() {
		return getParent().getColumns().indexOf(this);
	}

	@Override
	public void setRank(int rank) {
		if (this.rank != rank) {
			this.rank = rank;
			// TODO check if needed : notifyListeners(ChangeEvent.MODEL_CHANGED,
			// null);
		}
	}

	@Override
	public int compareTo(Object arg0) {
		if (arg0 instanceof IBasicColumn) {
			IBasicColumn c2 = (IBasicColumn) arg0;
			if (rank < c2.getRank()) {
				return -1;
			} else if (rank > c2.getRank()) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return super.compareTo(arg0);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return nameHelper.getName() + "(id:" + getUID() + ") : "
				+ (type == null ? "no type" : (type.getName() + " (" + type.getLength() + ")"));
	}

	@Override
	public String getDefaultExpr() {
		return defaultExpr;
	}

	@Override
	public boolean isNotNull() {
		return isNotNull;
	}

	@Override
	public void setDefaultExpr(String expr) {
		this.defaultExpr = expr;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setNotNull(boolean notNull) {
		this.isNotNull = notNull;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	protected void setHibernateNotNull(String notNull) {
		setNotNull("Y".equals(notNull)); //$NON-NLS-1$
	}

	protected String getHibernateNotNull() {
		return (isNotNull ? "Y" : "N"); //$NON-NLS-1$//$NON-NLS-2$
	}

	protected void setHibernateVirtual(String virtual) {
		setVirtual("Y".equals(virtual)); //$NON-NLS-1$
	}

	protected String getHibernateVirtual() {
		return (isVirtual ? "Y" : "N"); //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertyProvider.class) {
			return new ComparisonPropertyProvider(this);
		}
		return null;
	}

	@Override
	public IBasicColumn copy() {
		IBasicColumn c = new BasicColumn(this.getName(), this.getDescription(), new Datatype(
				this.getDatatype()), this.getRank());
		c.setDefaultExpr(this.getDefaultExpr());
		c.setNotNull(this.isNotNull());
		c.setReference(this.getReference());
		c.setParent(this.getParent());
		return c;
	}

	/**
	 * A specific extension for columns as a workaround for the clustered
	 * columns bug. While reverse synchronizing clusters, column references are
	 * replaced by repository references through this method. Clusters uses
	 * column references to bind cluster columns with clustered table columns.<br>
	 * Since the mapping is reference-based, cluisters has no way to know the
	 * reference switch so we fire a REFERENCE_CHANGED event here and we made
	 * clusters listening to their columns.
	 */
	@Override
	public void setReference(IReference ref) {
		// FIXME: Replace cluster tables column mappings to map columns by the
		// column instance
		// rather than by the column references (requires a repository
		// migration)

		// We notifiy BEFORE applying change for listeners to be able to
		// retrieve both old and new
		// reference
		final IReference oldReference = getReference();
		super.setReference(ref);
		if (getParent() != null && getParent() instanceof IBasicTable) {
			((IBasicTable) getParent()).internalColumnRefChanged(this, oldReference, ref);
		}
	}

	@Override
	public void setVirtual(boolean isVirtual) {
		if (isVirtual != this.isVirtual) {
			this.isVirtual = isVirtual;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public boolean isVirtual() {
		return isVirtual;
	}
}
