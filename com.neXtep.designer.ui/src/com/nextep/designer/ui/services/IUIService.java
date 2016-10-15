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
package com.nextep.designer.ui.services;

import java.util.List;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.ui.model.IUIComponent;

/**
 * Provides common UI services available to the neXtep platform. Callers willing version-aware UI
 * components should prefer the VCS ICommonUIService implementation of this interface.
 * 
 * @author Christophe Fondacci
 * @since 1.0.7
 */
public interface IUIService {

	String PAGE_ID_PREFIX = "com.neXtep.designer.ui.typedPage."; //$NON-NLS-1$

	/**
	 * Retrieves the list of UI components which contribute to the edition of elements of the given
	 * type in the specified vendor context.
	 * 
	 * @param type the {@link IElementType} to retrieve UI editors for
	 * @param vendor the {@link DBVendor} for which editors should be retrieved
	 * @return a list of {@link IUIComponent}, fragments of the type editor, sorted in the order
	 *         that should be used to display them
	 */
	List<IUIComponent> getEditorComponentsFor(IElementType type, DBVendor vendor);

	/**
	 * A helper method to retrieve UI editor components of an element. This method will generally be
	 * equivalent to calling :<br>
	 * <code>uiService.getEditorComponentsFor(object.getType());</code>
	 * 
	 * @param object the {@link ITypedObject} to edit
	 * @param vendor the {@link DBVendor} for which editors should be retrieved
	 * @return a list of {@link IUIComponent}, fragments of the type editor, sorted in the order
	 *         that should be used to display them
	 */
	List<IUIComponent> getEditorComponentsFor(ITypedObject object, DBVendor vendor);

	/**
	 * Binds the controller of the specified model object as a listener to model change events.
	 * 
	 * @param model the model object to bind controller to, as an object so that all checks / casts
	 *        are done by the service implementation rather than by the caller
	 */
	void bindController(Object model);
}
