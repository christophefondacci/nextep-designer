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
/**
 *
 */
package com.nextep.datadesigner.model;

import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.impl.Referenceable;

/**
 * This interface is implemented by all objects which could be retrieved by reference. A reference
 * has a global repository scope which is larger than the view or version scope. Default
 * implementation is provided by the {@link Referenceable} class which should be used as a delegate
 * or as a base class.<br>
 * <br>
 * For convenience purposes, a pre-built implementation of the {@link IReferenceable} interface is
 * provided in the abstract base class {@link NamedObservable}.
 * 
 * @author Christophe Fondacci
 */
public interface IReferenceable {

	/**
	 * @return the reference of the object
	 */
	public IReference getReference();

	public void setReference(IReference ref);

}
