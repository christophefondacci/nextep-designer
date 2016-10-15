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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.nextep.datadesigner.dbgm.impl.VersionedTable;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;

/**
 * Oracle implementation of a table.
 * 
 * @author Christophe Fondacci
 */
public class OracleTable extends VersionedTable implements IOracleTable, IBasicTable {

	private IOracleTablePhysicalProperties properties;
	/** Check constraints of this table */
	private Set<ICheckConstraint> checkConstraints;

	public OracleTable() {
		super();
		checkConstraints = new HashSet<ICheckConstraint>();
	}

	/**
	 * @see com.nextep.designer.dbgm.oracle.model.IOracleTable#getPhysicalProperties()
	 */
	@Override
	public IOracleTablePhysicalProperties getPhysicalProperties() {
		return properties;
	}

	/**
	 * @see com.nextep.designer.dbgm.oracle.model.IOracleTable#setPhysicalProperties(com.nextep.designer.dbgm.model.IPhysicalProperties)
	 */
	@Override
	public void setPhysicalProperties(IPhysicalProperties properties) {
		if (properties != this.properties) {
			final IOracleTablePhysicalProperties oldProps = this.properties;
			this.properties = (IOracleTablePhysicalProperties) properties;
			if (properties == null) {
				notifyListeners(ChangeEvent.GENERIC_CHILD_REMOVED, this.properties);
			} else {
				this.properties.setParent(this);
				notifyIfChanged(oldProps, this.properties, ChangeEvent.GENERIC_CHILD_ADDED);
			}
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.impl.VersionedTable#getReferenceMap()
	 */
	@Override
	public Map<IReference, IReferenceable> getReferenceMap() {
		Map<IReference, IReferenceable> map = super.getReferenceMap();
		if (getPhysicalProperties() != null) {
			map.put(getPhysicalProperties().getReference(), getPhysicalProperties());
			for (IPartition p : getPhysicalProperties().getPartitions()) {
				map.put(p.getReference(), p);
				final IPhysicalProperties partPhysProps = p.getPhysicalProperties();
				if (partPhysProps != null) {
					map.put(partPhysProps.getReference(), partPhysProps);
				}
			}
		}
		for (ICheckConstraint c : getCheckConstraints()) {
			map.put(c.getReference(), c);
		}
		for (IKeyConstraint c : getConstraints()) {
			if (c instanceof OracleUniqueConstraint) {
				map.putAll(((OracleUniqueConstraint) c).getReferenceMap());
			}
		}
		return map;
	}

	@Override
	public Collection<IReference> getReferenceDependencies() {
		Collection<IReference> refs = super.getReferenceDependencies();
		if (getPhysicalProperties() != null) {
			refs.addAll(getPhysicalProperties().getReferenceDependencies());
		}
		return refs;
	}

	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		if (!super.updateReferenceDependencies(oldRef, newRef)) {
			if (getPhysicalProperties() != null) {
				return getPhysicalProperties().updateReferenceDependencies(oldRef, newRef);
			}
			return false;
		}
		return true;
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
	 * @param constraints check constraints of this table
	 */
	protected void setCheckConstraints(Set<ICheckConstraint> constraints) {
		this.checkConstraints = constraints;
	}

	@Override
	public IElementType getPhysicalPropertiesType() {
		return IElementType.getInstance(IOracleTablePhysicalProperties.TYPE_ID);
	}
}
