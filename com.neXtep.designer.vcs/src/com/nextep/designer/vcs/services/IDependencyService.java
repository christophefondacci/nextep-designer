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
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.designer.core.model.IReferenceManager;

public interface IDependencyService {

	/**
	 * Checks through the dependency manager if the system can delete the object (or remove it from
	 * the view). It will raise an ErrorException to indicate that the deletion is not allowed on
	 * the given referenceable containing a message with all dependent objects. A delete is not
	 * allowed on any referenceable object which has dependent objects referring to it.<br>
	 * <br>
	 * This method allow callers to pregenerate the dependencies and to pass the reverse dependency
	 * table as an argument.
	 * 
	 * @param ref object to check for deletion
	 * @param dependencies reverse dependencies of this object
	 * @param deletedReferencers is a collection of all referencers about to be deleted. This
	 *        affects the check in that if the deletedReferencers collection contains a found
	 *        dependency, it will be OK. To use while batch deleting elements.
	 * @return a boolean indicating if the delete is allowed
	 * @throw {@link ErrorException} if the delete is not allowed
	 */
	boolean checkDeleteAllowed(IReferenceable ref, Collection<IReferencer> deps,
			Collection<IReferencer> deletedReferencers);

	/**
	 * A convenience method which checks whether deletion of the specified {@link IReferenceable} is
	 * allowed (that is to say will not generate any external reference dependency). The check will
	 * be made assuming the specified list of {@link IReferencer} will all be deleted in a single
	 * pass so that it will return <code>true</code> when :<br>
	 * - the object has no dependency pointing to it<br>
	 * - the object has dependencies pointing to it which are all contained in the
	 * deletedReferencers list<br>
	 * In any other case it will raise.
	 * 
	 * @param ref the {@link IReferenceable} element to check for deletion
	 * @param deletedReferencers a list of all referencers currently being deleted along with the
	 *        specified {@link IReferenceable}
	 * @return <code>true</code> if delete is allowed, raise otherwise
	 * @throws ErrorException when deletion is not allowed so that we're sure that the current
	 *         process will abort, except if it explicitly catches it
	 */
	boolean checkDeleteAllowed(IReferenceable ref, Collection<IReferencer> deletedReferencers);

	/**
	 * Checks through the dependency manager if the system can delete the object (or remove it from
	 * the view). It will raise an ErrorException to indicate that the deletion is not allowed on
	 * the given referenceable containing a message with all dependent objects. A delete is not
	 * allowed on any referenceable object which has dependent objects referring to it.
	 * 
	 * @param ref object to check for deletion
	 * @return a boolean indicating if the delete is allowed
	 * @throw {@link ErrorException} if the delete is not allowed
	 */
	boolean checkDeleteAllowed(IReferenceable ref);

	/**
	 * This method will provide the list of referencers which <u>would</u> remain on the specified
	 * {@link IReferenceable} if all {@link IReferencer} from the given list are removed, assuming
	 * that all references on the element are provided in the <code>dependencies</code> argument.<br>
	 * This method is used by all deletion check methods. A deletion will only be allowed when the
	 * returned collection is empty.
	 * 
	 * @param ref the IReferenceable to check referencers for
	 * @param dependencies current dependencies on the {@link IReferenceable} element
	 * @param deletedReferencers a collection of {@link IReferencer} which will be deleted
	 * @return a collection of {@link IReferencer} which would remain if deletedReferencers are
	 *         removed.
	 */
	Collection<IReferencer> getReferencersAfterDeletion(IReferenceable ref,
			Collection<IReferencer> dependencies, Collection<IReferencer> deletedReferencers);

	/**
	 * This method computes and returns the smallest set of elements which directly reference the
	 * specified object. In other words, this is the set of elements which, if removed, removes all
	 * dependencies to the specified object.<br>
	 * This method differs from the {@link IReferenceManager#getReverseDependencies(IReferenceable)}
	 * as it removes the depdency transitive aspect.<br>
	 * <b>Example :</b><br>
	 * Table A is in module M1 has a primary key PK<br>
	 * Table B is in module M2 has a foreign key FK referencing PK from table A<br>
	 * A call to this method on the PK of Table A would return :<br>
	 * <code>FK</code><br>
	 * While a call to {@link IReferenceManager#getReverseDependencies(IReferenceable)} would return
	 * :<br>
	 * <code>FK, Table B, Module M2</code>
	 * 
	 * @param referenceable the {@link IReferenceable} object to retrieve dependencies for
	 * @return the list of elements directly connected to this object
	 */
	List<IReferencer> getDirectlyDependentObjects(IReferenceable referenceable);

	/**
	 * This method computes and returns the smallest set of elements which directly reference the
	 * specified object. In other words, this is the set of elements which, if removed, removes all
	 * dependencies to the specified object.<br>
	 * This method is designed for performance optimizations where a caller can precompute the
	 * reverse dependencies map and call this method several times without having the cost of all
	 * dependencies computation.<br>
	 * This method differs from the {@link IReferenceManager#getReverseDependencies(IReferenceable)}
	 * as it removes the depdency transitive aspect.<br>
	 * <b>Example :</b><br>
	 * Table A is in module M1 has a primary key PK<br>
	 * Table B is in module M2 has a foreign key FK referencing PK from table A<br>
	 * A call to this method on the PK of Table A would return :<br>
	 * <code>FK</code><br>
	 * While a call to {@link IReferenceManager#getReverseDependencies(IReferenceable)} would return
	 * :<br>
	 * <code>FK, Table B, Module M2</code>
	 * 
	 * @param invRefMap the reverse dependencies map obtained from the {@link IReferenceManager}.
	 * @param referenceable the {@link IReferenceable} object to retrieve dependencies for
	 * @return the list of elements directly connected to this object
	 */
	List<IReferencer> getDirectlyDependentObjects(MultiValueMap invRefMap,
			IReferenceable referenceable);
}
