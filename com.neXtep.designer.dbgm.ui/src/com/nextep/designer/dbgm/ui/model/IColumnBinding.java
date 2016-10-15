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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.ui.model;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.IMarker;

/**
 * @author Christophe Fondacci
 */
public interface IColumnBinding extends ITypedObject {

	/**
	 * Retrieves the source column of this binding
	 * 
	 * @return the {@link IBasicColumn} corresponding to the source column of the binding
	 */
	IBasicColumn getColumn();

	/**
	 * Retrieves the associated column of this binding
	 * 
	 * @return the {@link IBasicColumn} corresponding to the column bound to the source
	 */
	IBasicColumn getAssociatedColumn();

	/**
	 * An optional marker for this binding
	 * 
	 * @return a {@link IMarker} informing about binding problem, or <code>null</code> if nothing is
	 *         marked
	 */
	IMarker getMarker();

}
