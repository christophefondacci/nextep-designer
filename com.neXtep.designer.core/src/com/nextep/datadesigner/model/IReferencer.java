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
package com.nextep.datadesigner.model;

import java.util.Collection;

/**
 * An interface for referencer objects. A referencer
 * is an element which has dependencies to {@link IReferenceable}
 * elements through their reference.
 *
 * @author Christophe Fondacci
 *
 */
public interface IReferencer {

	/**
	 * @return a collection of all dependencies to references. The returned
	 * collection should be dedicated to dependencies and a new Collection
	 * instance should be created each time this method is called.
	 */
	public abstract Collection<IReference> getReferenceDependencies();
	/**
	 * Updates the given old reference dependency with the new one specified.
	 * This could happen while merging external (i.e. database) referencers
	 * with the repository.
	 *
	 * @param oldRef old dependency reference to replace
	 * @param newRef new dependency reference
	 * @return a boolean indicating if the reference has been updated or not
	 */
	public abstract boolean updateReferenceDependencies(IReference oldRef, IReference newRef);
}
