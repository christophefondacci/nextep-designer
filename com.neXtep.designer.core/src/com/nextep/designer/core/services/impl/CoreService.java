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
package com.nextep.designer.core.services.impl;

import java.util.HashMap;
import java.util.Map;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.core.model.IResourceLocator;
import com.nextep.designer.core.services.ICoreService;

/**
 * Default {@link ICoreService} implementation
 * 
 * @author Christophe Fondacci
 */
public class CoreService implements ICoreService {

	private Map<String, IResourceLocator> resourcesMap = new HashMap<String, IResourceLocator>();

	@Override
	public boolean isLocked(Object o) {
		final ILockable<?> lockable = getLockable(o);
		if (lockable != null) {
			return lockable.updatesLocked();
		}
		return false;
	}

	@Override
	public ILockable<?> getLockable(Object o) {
		if (o instanceof ILockable<?>) {
			return (ILockable<?>) o;
		} else if (o instanceof IParentable<?>) {
			final Object parent = ((IParentable<?>) o).getParent();
			return getLockable(parent);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ITypedObject> T getFirstTypedParent(IParentable<?> p, Class<T> parentClass) {
		if (p != null) {
			Object parent = p.getParent();
			if (parent != null) {
				if (parentClass.isAssignableFrom(parent.getClass())) {
					return (T) parent;
				} else if (parent instanceof IParentable<?>) {
					return getFirstTypedParent((IParentable<?>) parent, parentClass);
				}
			}
		}
		return null;
	}

	@Override
	public void registerResource(String resourceKey, IResourceLocator locator) {
		resourcesMap.put(resourceKey, locator);
	}

	@Override
	public IResourceLocator getResource(String resourceKey) {
		return resourcesMap.get(resourceKey);
	}

}
