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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.postgre.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nextep.datadesigner.dbgm.impl.VersionedTable;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlTable;

/**
 * @author Christophe Fondacci
 */
public class PostgreSqlTable extends VersionedTable implements IPostgreSqlTable {

	private IPhysicalProperties physicalProperties;
	/** Check constraints of this table */
	private Set<ICheckConstraint> checkConstraints;
	private Set<IReference> inherithances;

	public PostgreSqlTable() {
		super();
		checkConstraints = new HashSet<ICheckConstraint>();
		// setReference(new Reference(getType(), null, this));
		inherithances = new HashSet<IReference>();
	}

	@Override
	public void setPhysicalProperties(IPhysicalProperties properties) {
		final IPhysicalProperties oldProperties = physicalProperties;
		this.physicalProperties = properties;
		if (this.physicalProperties != null) {
			this.physicalProperties.setParent(this);
			notifyIfChanged(oldProperties, physicalProperties, ChangeEvent.GENERIC_CHILD_ADDED);
		} else {
			notifyIfChanged(oldProperties, physicalProperties, ChangeEvent.GENERIC_CHILD_REMOVED);
		}
	}

	@Override
	public IPhysicalProperties getPhysicalProperties() {
		return physicalProperties;
	}

	@Override
	public IElementType getPhysicalPropertiesType() {
		return IElementType.getInstance(ITablePhysicalProperties.TYPE_ID);
	}

	@Override
	public void addCheckConstraint(ICheckConstraint constraint) {
		if (checkConstraints.add(constraint)) {
			notifyListeners(ChangeEvent.GENERIC_CHILD_ADDED, constraint);
		}
	}

	@Override
	public Set<ICheckConstraint> getCheckConstraints() {
		return checkConstraints;
	}

	@Override
	public void removeCheckConstraint(ICheckConstraint constraint) {
		if (checkConstraints.remove(constraint)) {
			notifyListeners(ChangeEvent.GENERIC_CHILD_REMOVED, constraint);
		}
	}

	/**
	 * Defines the check constraints set for this table.<br>
	 * <b>Only for hibernate calls.</b>
	 * 
	 * @param constraints
	 *            check constraints of this table
	 */
	protected void setCheckConstraints(Set<ICheckConstraint> constraints) {
		this.checkConstraints = constraints;
	}

	@Override
	public Set<IReference> getInheritances() {
		return inherithances;
	}

	@Override
	public void addInheritance(IBasicTable t) {
		if (t != null) {
			addInheritanceRef(t.getReference());
		}
	}

	@Override
	public void addInheritanceRef(IReference r) {
		if (inherithances.add(r)) {
			notifyListeners(ChangeEvent.GENERIC_CHILD_ADDED, r);
		}
	}

	@Override
	public void removeInheritance(IBasicTable t) {
		if (t != null && inherithances.remove(t.getReference())) {
			notifyListeners(ChangeEvent.GENERIC_CHILD_REMOVED, t);
		}
	}

	protected void setInheritances(Set<IReference> inheritances) {
		this.inherithances = inheritances;
	}

	@Override
	public Collection<IReference> getReferenceDependencies() {
		final List<IReference> refs = new ArrayList<IReference>(super.getReferenceDependencies());
		refs.addAll(inherithances);
		return refs;
	}

	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		if (!super.updateReferenceDependencies(oldRef, newRef)) {
			if (inherithances.contains(oldRef)) {
				inherithances.remove(oldRef);
				inherithances.add(newRef);
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected boolean isNoColumnSupported() {
		return inherithances != null && !inherithances.isEmpty();
	}
}
