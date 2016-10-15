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
package com.nextep.designer.vcs.ui.services;

import java.util.Collection;
import org.eclipse.jface.viewers.StructuredViewer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Provides UI-oriented services for repository workspace manipulation
 * 
 * @author Christophe Fondacci
 */
public interface IWorkspaceUIService {

	/**
	 * Changes the current view.
	 * 
	 * @param viewId identifier of new view to initialize
	 */
	void changeWorkspace(UID viewId);

	/**
	 * Moves the specified collection of elements to the given container.
	 * 
	 * @param versionToMove the collection of {@link IVersionable} elements to move
	 * @param targetContainer target {@link IVersionContainer} to move elements to
	 */
	void move(Collection<IVersionable<?>> elementsToMove, IVersionContainer targetContainer);

	/**
	 * Removes the specified elements from the current workspace.
	 * 
	 * @param elementsToRemove set of elements to remove
	 */
	void remove(IReferenceable... elementsToRemove);

	/**
	 * Refreshes the navigator of the given object, if existing in the navigator hierarchy. Note
	 * that the actual execution of the refresh may be delayed and asynchronous so callers should
	 * not assume that the UI component is refreshed after calling this method but should only
	 * assume that it will be refreshed soon.
	 * 
	 * @param o object whose navigator is to be refreshed
	 */
	void refreshNavigatorFor(Object o);

	/**
	 * Registers the given viewer as the viewer for the version navigator.
	 * 
	 * @param viewer the JFace viewer to register
	 */
	void registerVersionNavigatorViewer(StructuredViewer viewer);
}
