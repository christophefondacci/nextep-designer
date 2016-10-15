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

/**
 * A policy to import versionable elements into a container
 * 
 * @author Christophe Fondacci
 */
public interface IImportPolicy {

	/**
	 * Imports a <code>IVersionable</code> in the given container.<br>
	 * After a call to this method, the caller could consider that the object has been imported to
	 * the view when the method returns <code>true</code>.
	 * 
	 * @param v the versionable to import in container
	 * @param c the container in which the database object will be imported
	 * @param activity activity that will be used to checkout objects if needed
	 * @return <code>true</code> if versionable has been added, else <code>false</code>
	 */
	boolean importVersionable(IVersionable<?> v, IVersionContainer c, IActivity activity);

	/**
	 * Finalizes the import, this method may do nothing but should always be called
	 * 
	 * @since 1.0.7
	 */
	void finalizeImport();
}
