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
package com.nextep.datadesigner.sqlgen.impl;

import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.model.ISynchronizationFilter;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * A synchronization filter default implementation.
 * 
 * @see ISynchronizationFilter
 * @author cfondacci
 */
public class SynchronizationFilter extends IDNamedObservable implements ISynchronizationFilter {

	private static final Log LOGGER = LogFactory.getLog(SynchronizationFilter.class);

	/** Type of the elements to filter */
	private IElementType type;
	/** Reference of the container defining this filter */
	private IReference containerRef;

	public SynchronizationFilter(IVersionable<IVersionContainer> container, String regExpName,
			IElementType type) {
		setName(regExpName);
		setContainerRef(container.getReference());
		setType(type);
	}

	protected SynchronizationFilter() {
	}

	@Override
	public IElementType getType() {
		return type;
	}

	@Override
	public void setType(IElementType type) {
		this.type = type;
	}

	@Override
	public IReference getContainerRef() {
		return containerRef;
	}

	@Override
	public void setContainerRef(IReference ref) {
		this.containerRef = ref;
	}

	@Override
	public boolean match(ITypedObject object) {
		if (object == null)
			return false;
		if (object.getType() == getType() || getType() == null) {
			if (object instanceof INamedObject) {
				final INamedObject named = (INamedObject) object;
				try {
					return Pattern.matches(getName(), named.getName());
				} catch (RuntimeException e) {
					LOGGER.error("Unable to compile synchronization filter '" + getName() + "': "
							+ e.getMessage(), e);
				}
			}
		}
		return false;
	}

}
