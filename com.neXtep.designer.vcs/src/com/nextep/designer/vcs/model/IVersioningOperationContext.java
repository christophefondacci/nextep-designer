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
 * This interface holds information on the context of a running versioning operation. It is used by
 * the framework to pass information (mainly for validation, but this could be extended) to elements
 * willing to contribute to this versioning operation.
 * 
 * @author Christophe Fondacci
 */
public interface IVersioningOperationContext {

	/**
	 * A read-only collection of all versionable targetted by the current operation.
	 * 
	 * @return the collection of {@link IVersionable} which are being processed
	 */
	Collection<IVersionable<?>> getVersionables();

	/**
	 * Returns the current activity which should be used for this versioning operation.
	 * 
	 * @return the {@link IActivity}
	 */
	IActivity getActivity();

	/**
	 * Defines the activity which should be used for this versioning operation
	 * 
	 * @param activity the {@link IActivity} to use
	 */
	void setActivity(IActivity activity);

	/**
	 * The version info which holds the release number to define for the specified versionable
	 * element. Depending on the current operation, the real association might be done before or
	 * after the operation.
	 * 
	 * @param element element which should be associated with this version.
	 * @param version the {@link IVersionInfo} to use for release number and branch info
	 */
	void setTargetVersionInfo(IVersionable<?> element, IVersionInfo version);

	/**
	 * Retrieves the version information to use when processing the specified versionable element.
	 * 
	 * @param element a {@link IVersionable} element from the processed elements list
	 * @return the {@link IVersionInfo} which should be used for this element
	 */
	IVersionInfo getTargetVersionInfo(IVersionable<?> element);

	/**
	 * Informs about the versioning operation for which this context has been initialized.
	 * 
	 * @return the {@link VersioningOperation}
	 */
	VersioningOperation getVersioningOperation();
}
