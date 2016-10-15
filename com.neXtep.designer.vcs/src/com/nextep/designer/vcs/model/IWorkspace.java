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

import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ISharedRepositoryObject;

/**
 * Represents a view on versioned objects. Views are containers of IVersionable objects and should
 * be used as workspaces.<br>
 * A view is built from a set of load rules that are computed at the view initialization or refresh
 * to determine contents TODO : add load rule support
 * 
 * @author Christophe Fondacci
 */
public interface IWorkspace extends IVersionContainer, IReferencer, ISharedRepositoryObject,
		IReferenceContainer {

	final String TYPE_ID = "VIEW"; //$NON-NLS-1$

	long getId();

	void setId(long id);

	/**
	 * Defines whether the current view requires immediate import after opening the workbench.
	 * 
	 * @param needsImportOnOpen <code>true</code> to request import at view opening, else
	 *        <code>false</code>
	 */
	void setImportOnOpenNeeded(boolean needsImportOnOpen);

	/**
	 * Informs whether the current view needs to be synchronized when opened.
	 * 
	 * @return <code>true</code> if import is required at view opening, else <code>false</code>
	 */
	boolean isImportOnOpenNeeded();
}
