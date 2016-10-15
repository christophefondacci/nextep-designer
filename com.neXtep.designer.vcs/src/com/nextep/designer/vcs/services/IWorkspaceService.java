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
package com.nextep.designer.vcs.services;

import java.util.Collection;
import java.util.List;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.IWorkspaceListener;

/**
 * This interface provides facility methods to interact with the current view.
 * 
 * @author Christophe Fondacci
 */
public interface IWorkspaceService {

	/**
	 * Creates a new workspace instance.
	 * 
	 * @return a new {@link IWorkspace} instance
	 */
	IWorkspace createWorkspace();

	/**
	 * Retrieves the current repository view of the workbench.
	 * 
	 * @return the current {@link IWorkspace}
	 */
	IWorkspace getCurrentWorkspace();

	/**
	 * Defines the current view.
	 * 
	 * @param currentView the new {@link IWorkspace} becoming the current view
	 */
	void setCurrentWorkspace(IWorkspace currentView);

	/**
	 * Retrieves the current view database targets.
	 * 
	 * @return a {@link ITargetSet} providing all targets associated with the current view
	 */
	ITargetSet getCurrentViewTargets();

	/**
	 * Changes the current workspace.
	 * 
	 * @param workspaceId identifier of new workspace to initialize
	 */
	void changeWorkspace(UID workspaceId, IProgressMonitor monitor);

	/**
	 * Adds a view listener which will be notified of view events.
	 * 
	 * @param listener the {@link IWorkspaceListener} to add
	 */
	void addWorkspaceListener(IWorkspaceListener listener);

	/**
	 * Removes the specified listener from view notifications
	 * 
	 * @param listener the {@link IWorkspaceListener} to remove from notifications
	 */
	void removeWorkspaceListener(IWorkspaceListener listener);

	/**
	 * @return the current repository user
	 */
	IRepositoryUser getCurrentUser();

	/**
	 * Defines the currently connected repository user
	 * 
	 * @param user connected user
	 */
	void setCurrentUser(IRepositoryUser user);

	/**
	 * Moves the specified collection of elements to the given container.
	 * 
	 * @param versionToMove the collection of {@link IVersionable} elements to move
	 * @param targetContainer target {@link IVersionContainer} to move elements to
	 */
	void move(Collection<IVersionable<?>> versionsToMove, IVersionContainer targetContainer,
			IProgressMonitor monitor);

	/**
	 * Removes the set of specified elements, raising exception if some elements cannot be removed.
	 * 
	 * @param elementsToRemove array of elements to remove from current workspace
	 */
	void remove(IProgressMonitor monitor, IReferenceable... elementsToRemove);

	/**
	 * Finds a workspace unique ID from its name.
	 * 
	 * @param name name of the workspace to find
	 * @return the UID of the corresponding workspace or null if not found
	 */
	UID findWorkspaceId(String name);

	List<IReferencer> getRemainingDependencies(List<IReferencer> initialDeletedReferencers,
			MultiValueMap invRefMap, IReferenceable... elementsToRemove);
}
