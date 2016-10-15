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

import java.util.Collection;

/**
 * This class defines the listener interface for versioning operations. Generic methods are provided
 * to hook processing before and after unitary action (on a per-IVersionable object basis) and/or
 * before and after service action (considering a set of {@link IVersionable}).<br>
 * Listeners should be declared as OSGi services and injected in the service implementation.
 * 
 * @author Christophe Fondacci
 */
public interface IVersioningListener {

	/**
	 * This method is called before processing a {@link IVersionable} for the specified operation.
	 * 
	 * @param operation the {@link VersioningOperation} about to be performed on the object
	 * @param v the {@link IVersionable} being processed
	 */
	void handleBeforeVersionableOperation(VersioningOperation operation, IVersionable<?> v);

	/**
	 * This method is called after processing a {@link IVersionable} for the specified operation.
	 * 
	 * @param operation the {@link VersioningOperation} about to be performed on the object
	 * @param v the {@link IVersionable} being processed
	 */
	void handleAfterVersionableOperation(VersioningOperation operation, IVersionable<?> v);

	/**
	 * This method is called before a service operation. A service operation takes a set of
	 * {@link IVersionable} elements and iterates over every item.
	 * 
	 * @param operation the {@link VersioningOperation} about to be executed by the service
	 * @param versionables the collection of {@link IVersionable} the service will process
	 */
	void handleBeforeServiceOperation(VersioningOperation operation,
			Collection<IVersionable<?>> versionables);

	/**
	 * This method is called after a service operation. A service operation takes a set of
	 * {@link IVersionable} elements and iterates over every item.
	 * 
	 * @param operation the {@link VersioningOperation} about to be executed by the service
	 * @param versionables the collection of {@link IVersionable} the service will process
	 */
	void handleAfterServiceOperation(VersioningOperation operation,
			Collection<IVersionable<?>> versionables);
}
