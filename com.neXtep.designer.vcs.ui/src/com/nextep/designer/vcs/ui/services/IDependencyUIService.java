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

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.ui.model.DependencyMode;
import com.nextep.designer.vcs.ui.model.IDependencyServiceListener;

/**
 * This interface defines the UI dependency service. This service is a UI-layer service intended to
 * be called from user interaction. It provides features to find / view dependencies pointing to or
 * from a given object.
 * 
 * @author Christophe Fondacci
 */
public interface IDependencyUIService {

	/**
	 * Computes the dependencies for the given element. This method returns void. Entities willing
	 * to be notified of dependencies request need to register as listeners of this service as
	 * dependency computation is done in an asynchronous job.
	 * 
	 * @param element element to compute dependencies for
	 * @param type type of dependencies to compute
	 */
	void computeDependencies(IReferenceable element, DependencyMode mode);

	/**
	 * Computes the dependencies using the current mode
	 * 
	 * @param element
	 */
	void computeDependencies(IReferenceable element);

	/**
	 * Defines the current default dependency mode. This mode will be used when calling the
	 * {@link IDependencyUIService#computeDependencies(IReferenceable)} method without specifying a
	 * dependency mode explicitly
	 * 
	 * @param mode new default mode to use for dependency management
	 */
	void setDependencyMode(DependencyMode mode);

	/**
	 * Retrieves the current default dependency mode.
	 * 
	 * @return the current default dependency mode
	 */
	DependencyMode getDependencyMode();

	/**
	 * Adds a listener to this dependency service. Listeners will be notified of new user requests.
	 * 
	 * @param listener listener to add to the service
	 */
	void addListener(IDependencyServiceListener listener);

	/**
	 * Removes the specified listeners from dependency service notifications.
	 * 
	 * @param listener the listener to remove
	 */
	void removeListener(IDependencyServiceListener listener);
}
