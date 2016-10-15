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
package com.nextep.datadesigner.vcs.gui.external;

import com.nextep.datadesigner.gui.model.IDynamicListProvider;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.exception.UnmodifiableObjectException;
import com.nextep.designer.vcs.services.IDependencyService;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

public abstract class DynamicListProvider implements IDynamicListProvider {

	@Override
	public void add(Object fromObject) {
		if (fromObject != null) {
			fromObject = isAddRemoveAllowed(fromObject);
			UIControllerFactory.getController(getType()).newInstance(fromObject);
		}
	}

	/**
	 * @return the type of objects to instantiate
	 */
	public abstract IElementType getType();

	/**
	 * Should we authorize execution of add / remove action, return the modifiable object or raise
	 * an {@link UnmodifiableObjectException}
	 * 
	 * @param fromObject object which would be modified
	 * @return <code>true</code> if we can execute, else <code>false</code>
	 */
	public Object isAddRemoveAllowed(Object fromObject) {
		return VCSPlugin.getService(IVersioningService.class).ensureModifiable(fromObject);
	}

	@Override
	public void remove(Object fromObject, Object toRemove) {
		if (fromObject != null) {
			if (toRemove instanceof IReferenceable) {
				VCSUIPlugin.getService(IWorkspaceUIService.class).remove((IReferenceable) toRemove);
			} else {
				// This else clause is only here for compatibility but should never get called
				fromObject = isAddRemoveAllowed(fromObject);
				// Checking that we can remove the object
				if (toRemove instanceof IReferenceable) {
					VCSPlugin.getService(IDependencyService.class).checkDeleteAllowed(
							(IReferenceable) toRemove);
				}
				ControllerFactory.getController(getType()).modelDeleted(toRemove);
			}
		}
	}

}
