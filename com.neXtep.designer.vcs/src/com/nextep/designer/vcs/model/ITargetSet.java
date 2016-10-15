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
package com.nextep.designer.vcs.model;

import java.util.Collection;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IConnectionContainer;
import com.nextep.designer.core.model.TargetType;

/**
 * @author Christophe Fondacci
 */
public interface ITargetSet extends IdentifiedObject, IObservable, ITypedObject,
		IConnectionContainer {

	static final String TYPE_ID = "TARGET_SET"; //$NON-NLS-1$

	/**
	 * Retrieves the collection of connections associated with the specified target. Kept for future
	 * user, should not be called yet. Existing calls may not be removed as we may introduce this
	 * notion soon but with another approach.
	 * 
	 * @param targetType type of target to get connections for
	 * @return the collection of {@link IConnection} for the specified target type.
	 */
	Collection<IConnection> getTarget(TargetType targetType);

	/**
	 * Retrieves the view to which these targets are associated.
	 * 
	 * @return the view which is associated with this target, or <code>null</code> if none
	 */
	IWorkspace getView();

	/**
	 * Defines the view to which this target set is associated
	 */
	void setView(IWorkspace view);

}
