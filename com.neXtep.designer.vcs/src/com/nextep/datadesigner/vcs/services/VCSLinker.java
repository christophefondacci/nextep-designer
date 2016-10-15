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
package com.nextep.datadesigner.vcs.services;

import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.VersionBranch;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * @author Christophe Fondacci
 */
public class VCSLinker implements IViewLinker {

	/**
	 * @see com.nextep.datadesigner.vcs.services.IViewLinker#getLabel()
	 */
	@Override
	public String getLabel() {
		return "User activities";
	}

	/**
	 * @see com.nextep.datadesigner.vcs.services.IViewLinker#link(com.nextep.designer.vcs.model.IWorkspace)
	 */
	@Override
	public void link(IWorkspace view) {
		// Initializing default branch on main thread
		VersionBranch.reset();
		VersionBranch.getDefaultBranch();
	}

	@Override
	public void relink(ITypedObject o, MultiValueMap invRefMap) {
		// Should never have to relink anything
	}
}
