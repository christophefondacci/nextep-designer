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
package com.nextep.designer.ui.model;

import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * A child component is a UI component whose model is driven by another part. It typically is an
 * abstraction of the master / details UI pattern, where a list of elements can be edited in another
 * section of a page. This other "edition" section is a child component, driven by the
 * ISelectionProvider of the master component.
 * 
 * @author Christophe Fondacci
 */
public interface IUIChildComponent extends IUIComponent {

	/**
	 * Defines the selection provider
	 * 
	 * @param selectionProvider the {@link ISelectionProvider} to use for master / details pattern
	 */
	void setSelectionProvider(ISelectionProvider selectionProvider);

	/**
	 * Retrieves the selection provider from which the model could be retrieved, following the
	 * master / details pattern.
	 * 
	 * @return the {@link ISelectionProvider} pointing to the model that this component deisplays
	 */
	ISelectionProvider getSelectionProvider();
}
