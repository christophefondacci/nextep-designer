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
package com.nextep.designer.core.model;

import com.nextep.datadesigner.exception.InconsistentObjectException;

/**
 * A checked object is an object which needs to
 * verify some rules to be consistent.
 * Such objects may be inconsistents or consistents
 * depending on the result of the checkConsistency
 * method.
 * The framework will performs these checks before
 * authorizing some actions such as object creation
 * and object generation.
 *
 * @author Christophe Fondacci
 *
 */
public interface ICheckedObject {

	/**
	 * The method called by the framework to determine
	 * if the object is consistent. The method may raise
	 * an exception to indicate an unconsistent state
	 * and should indicate a user-readable reason of the
	 * inconsistency.
	 *
	 * @throws InconsistentObjectException
	 */
	public abstract void checkConsistency() throws InconsistentObjectException;
}
