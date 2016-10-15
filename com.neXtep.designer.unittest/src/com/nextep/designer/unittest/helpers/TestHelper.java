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
package com.nextep.designer.unittest.helpers;

import org.junit.Assert;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

public final class TestHelper {

	/**
	 * @return the first container of the current view. If the container is checked in, it will be
	 *         checked out automatically
	 */
	public static IVersionContainer getFirstContainer() {
		IVersionable<?> parent = VCSPlugin.getViewService().getCurrentWorkspace().getContents()
				.iterator().next();
		parent = VCSUIPlugin.getVersioningUIService().ensureModifiable(parent);
		return (IVersionContainer) parent.getVersionnedObject().getModel();
	}

	/**
	 * Retrieves a versionable in the specified container from its name.<br>
	 * Test will fail if no such versionable can be found.
	 * 
	 * @param name name of the versionable to look for
	 * @param type the type of element to look for
	 * @param container container to search into
	 * @return the corresponding versionable
	 */
	public static IVersionable<?> getVersionableByName(String name, IElementType type,
			IVersionContainer container) {
		for (IVersionable<?> v : container.getContents()) {
			if (v.getName().equalsIgnoreCase(name) && v.getType() == type) {
				return v;
			}
		}
		Assert.fail("Versionable '" + name + "' not found");
		return null;
	}
}
