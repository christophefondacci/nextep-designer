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
package com.nextep.designer.dbgm.model.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IAdaptable;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.impl.ComparisonPropertyProvider;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PhysicalAttribute;

/**
 * Base class for physical properties containing most default (and reused) implementation.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public abstract class AbstractPhysicalProperties extends IDNamedObservable implements
		IPhysicalProperties, IAdaptable {

	private String tablespaceName;
	/** A map of all defined storage attributes (PCT_FREE, etc) */
	private Map<PhysicalAttribute, Object> attrMap;

	private boolean compressed = false;

	private boolean logging = true;

	public AbstractPhysicalProperties() {
		super();
		setReference(new Reference(getType(), "", this)); //$NON-NLS-1$
		attrMap = new HashMap<PhysicalAttribute, Object>();
	}

	@Override
	public void setReference(IReference ref) {
		// Debugging
		super.setReference(ref);
	}

	@Override
	public Object getAttribute(PhysicalAttribute attr) {
		Object o = attrMap.get(attr);
		// We consider 0-value is equivalent to null
		if (o instanceof Number && ((Number) o).longValue() == 0) {
			return null;
		}
		return o;
	}

	@Override
	public Map<PhysicalAttribute, Object> getAttributes() {
		return attrMap;
	}

	public void setAttributes(Map<PhysicalAttribute, Object> attrs) {
		this.attrMap = attrs;
	}

	@Override
	public String getTablespaceName() {
		return tablespaceName;
	}

	@Override
	public boolean isCompressed() {
		return compressed;
	}

	@Override
	public void setAttribute(PhysicalAttribute attr, Object value) {
		attrMap.put(attr, value);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setTablespaceName(String tablespaceName) {
		this.tablespaceName = tablespaceName;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertyProvider.class) {
			return new ComparisonPropertyProvider(this);
		}
		return null;
	}

	@Override
	public String getName() {
		if (getParent() != null) {
			return getParent().getName();
		} else {
			return "[Orphaned]"; //$NON-NLS-1$
		}
	}

	@Override
	public Collection<IReference> getReferenceDependencies() {
		return Collections.emptyList();
	}

	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		return false;
	}

	@Override
	public boolean isLogging() {
		return logging;
	}

	@Override
	public void setLogging(boolean logging) {
		if (this.logging != logging) {
			this.logging = logging;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

}
