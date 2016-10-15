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
package com.nextep.designer.vcs.model;

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * Listener on view events. Implementors should not implement this interface directly, please extend
 * AbstractViewListener instead.
 * 
 * @author Christophe Fondacci
 */
public interface IWorkspaceListener {

	int PRIORITY_INTERNAL = 0;
	int PRIORITY_UI = 10;
	int PRIORITY_OTHER = 20;

	/**
	 * Callback method called when the view need to be closed either because we are exiting or
	 * because we are about to switch to another view.
	 * 
	 * @param view {@link IWorkspace} view being closed
	 */
	void workspaceClosed(IWorkspace view);

	/**
	 * Callback method called when the view has changed. Implementor must report progress as soon as
	 * the processing is not immediate.
	 * 
	 * @param oldView previous {@link IWorkspace}
	 * @param newView new loaded {@link IWorkspace}
	 * @param monitor monitor to report progress to, used by the global view change monitor to
	 *        inform user about current load progress.
	 */
	void workspaceChanged(IWorkspace oldView, IWorkspace newView, IProgressMonitor monitor);

	/**
	 * Defines the view service which call this listener
	 * 
	 * @param service the {@link IWorkspaceService} to which this listener is connected
	 */
	void setWorkspaceService(IWorkspaceService service);

	/**
	 * Retrieves the {@link IWorkspaceService} to which this listener is connected
	 * 
	 * @return the {@link IWorkspaceService} to which this listener is registered
	 */
	IWorkspaceService getWorkspaceService();

	/**
	 * Retrieves this listener priority. The priority is used to sequence the call of listeners. The
	 * default is PRIORITY_OTHER.
	 * 
	 * @return this listener's priority
	 */
	int getPriority();

}
