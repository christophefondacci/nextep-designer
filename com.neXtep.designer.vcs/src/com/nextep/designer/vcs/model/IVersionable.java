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

import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ISharedRepositoryObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.vcs.services.IVersioningService;

/**
 * This interface defines elements which can be version controlled. Any element needing to be stored
 * and version controlled need to implement this interface.
 * 
 * @author Christophe Fondacci
 */
@SuppressWarnings("rawtypes")
public interface IVersionable<T> extends ITypedObject, INamedObject, IdentifiedObject, IObservable,
		Comparable, IReferenceable, IReferencer, IReferenceContainer, ISharedRepositoryObject {

	String VERSIONABLE_TYPE_ID = "VERSIONABLE"; //$NON-NLS-1$

	/**
	 * Retrieves the version of this element
	 * 
	 * @return the {@link IVersionInfo} providing information regarding the version of this element
	 */
	IVersionInfo getVersion();

	/**
	 * Defines the version of this element.
	 * 
	 * @param version the {@link IVersionInfo} of this element.
	 */
	void setVersion(IVersionInfo version);

	/**
	 * Retrieves the wrapped ILockable object
	 * 
	 * @return the wrapped versionned object
	 */
	ILockable<T> getVersionnedObject();

	/**
	 * Retrieves the container in which this object is located.
	 * 
	 * @return the IVersionContainer object containing this object
	 */
	IVersionContainer getContainer();

	/**
	 * Defines the container owning this object
	 * 
	 * @param container IVersionContainer which will contain this versionable
	 */
	void setContainer(IVersionContainer container);

	/**
	 * Commits the element and releases any locks using the specified activity. This method must not
	 * be called directly. Committing an element must be done using the {@link IVersioningService}.
	 * 
	 * @param context the versioning context
	 * @return the committed element
	 */
	IVersionable<T> checkIn(IVersioningOperationContext context);

	/**
	 * Checks out the current element using the specified activity. The only purpose of this method
	 * is to create a new exact copy of the current object. This method must not be called directly
	 * and is only internally called by the {@link IVersioningService}.
	 * 
	 * @param context the {@link IVersioningOperationContext}
	 * @return the checked out version of the current element
	 */
	IVersionable<T> checkOut(IVersioningOperationContext context);

	/**
	 * Puts the current element on the specified branch.
	 * 
	 * @param newBranch new version branch to use for this element
	 */
	void deBranch(IVersionBranch newBranch);

	/**
	 * Defines the policy of versioning for this element.
	 * 
	 * @param policy the {@link VersionPolicy}
	 */
	void setVersionPolicy(VersionPolicy policy);

	long getId();

	void setId(long id);
}
