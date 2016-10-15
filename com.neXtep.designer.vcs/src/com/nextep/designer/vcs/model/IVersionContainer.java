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

import java.util.Set;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.model.IVendorOriented;

/**
 * Represents an object which is able to contain IVersionable objects. There are typically two main
 * types of version containers : workspaces and database modules.
 * 
 * @author Christophe Fondacci
 */
public interface IVersionContainer extends INamedObject, IObservable, IdentifiedObject,
		ILockable<IVersionContainer>, IReferencer, IVendorOriented {

	public static final String TYPE_ID = "CONTAINER"; //$NON-NLS-1$

	/**
	 * Adds a versionable to this container
	 * 
	 * @param versionable a IVersionable object to add to this container
	 * @return <code>true</code> if versionable has correctly been added, else <code>false</code>
	 */
	boolean addVersionable(IVersionable<?> versionable, IImportPolicy policy);

	/**
	 * Removes a versionable from this container
	 * 
	 * @param versionable a IVersionable object to remove from this container
	 * @return <code>true</code> if versionable has correctly been removed, else <code>false</code>
	 */
	boolean removeVersionable(IVersionable<?> versionable);

	/**
	 * @return a IVersionable Set representing this container's contents
	 */
	Set<IVersionable<?>> getContents();

	/**
	 * @return the short name of this container
	 */
	String getShortName();

	/**
	 * Defines the short name of the container
	 * 
	 * @param shortName new container short name
	 */
	void setShortName(String shortName);

	/**
	 * Defines the name of the schema attached to this module.
	 * 
	 * @param schemaName the schema name
	 */
	void setSchemaName(String schemaName);

	/**
	 * Returns the name of the attached schema
	 * 
	 * @return the name of the schema attached to this module.
	 */
	String getSchemaName();

	// /**
	// * @return the database vendor of this module
	// */
	// DBVendor getDBVendor();
	//
	// /**
	// * Defines the database vendor of this module
	// *
	// * @param vendor new database vendor
	// */
	// void setDBVendor(DBVendor vendor);

}
