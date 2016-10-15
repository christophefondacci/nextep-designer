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
package com.nextep.designer.vcs.model.base;

import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.IWorkspaceListener;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * Provides base functionality for {@link IWorkspaceListener} and can absorb framework changes
 * transparently
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractWorkspaceListener implements IWorkspaceListener {

	private IWorkspaceService viewService;

	@Override
	public IWorkspaceService getWorkspaceService() {
		return viewService;
	}

	@Override
	public void setWorkspaceService(IWorkspaceService service) {
		this.viewService = service;
	}

	@Override
	public void workspaceClosed(IWorkspace view) {
	}

	@Override
	public int getPriority() {
		return PRIORITY_OTHER;
	}
}
