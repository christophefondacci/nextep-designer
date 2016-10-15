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
package com.nextep.designer.ui.model;

import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.model.base.AbstractUIController;

public interface ITypedObjectUIController extends IEventListener, ITypedObject {

	/**
	 * Initializes a new navigator instance for the specified object. A navigator connector is a
	 * connector that allows an object to be displayed in the main navigator of the IDE environment.
	 * All object model should propose a navigator to be properly displayed in the tree navigator.
	 * 
	 * @param model model to display in the navigator
	 * @return the related navigator connector
	 * @deprecated all navigators are now handled through eclipse common navigator framework or
	 *             jface
	 */
	@Deprecated
	INavigatorConnector initializeNavigator(Object model);

	/**
	 * Initializes a new editor GUI for the specified object (called content).
	 * 
	 * @param content content to display in the requested editor
	 * @return a IDisplayConntector representing the editor
	 * @deprecated every editor is provided through the editor ID and editor input or through
	 *             dynamic forms contributions.
	 */
	@Deprecated
	IDisplayConnector initializeEditor(Object content);

	/**
	 * Initializes a new property GUI for the specified object (called content).
	 * 
	 * @param content content for which the property window should be displayed
	 * @return a IDisplayConnector representing the property editor
	 * @deprecated not used and will be removed
	 */
	@Deprecated
	IDisplayConnector initializeProperty(Object content);

	/**
	 * Initializes a new graphical GUI for the specified object (called content). A graphical GUI is
	 * the graphical representation of this object.
	 * 
	 * @param content content for which the new graphical editor will be generated
	 * @return a IDisplayConnector representing the graphical editor
	 * @deprecated not used and will be removed
	 */
	@Deprecated
	IDisplayConnector initializeGraphical(Object content);

	/**
	 * Requests the creation of a new instance of an object to the controller. An optional parent
	 * object should be provided. The implementation of this method will have the responsability of
	 * instantiate and initialize the requested object.<br>
	 * This method should persist the newly created object before releasing it to the "wild world".
	 * 
	 * @param parent parent object of the new requested instance
	 * @return the new instance
	 */
	Object newInstance(Object parent);

	Object emptyInstance(String name, Object parent);

	/**
	 * @return whether this element is directly editable by the user in a standalone editor
	 */
	boolean isEditable();

	/**
	 * @return the RCP editor identifier which can edit typed elements controlled by this
	 *         controller. The {@link AbstractUIController} provides a default editor which wraps
	 *         the {@link IDisplayConnector} returned by the
	 *         {@link ITypedObjectUIController#initializeEditor(Object)} method. This default editor
	 *         may fit for most purposes.
	 * @since 1.0.3
	 */
	String getEditorId();

	/**
	 * @param the {@link ITypedObject} that needs to be edited
	 * @return the RCP {@link IEditorInput} of edited elements. The {@link AbstractUIController}
	 *         provides a generic {@link TypedEditorInput} which may fit for most purposes.
	 *         Overriding this method implies that you provide a custom editor id by overriding the
	 *         {@link ITypedObjectUIController#getEditorId()} method as well.
	 * @since 1.0.3
	 */
	IEditorInput getEditorInput(ITypedObject model);

	/**
	 * Assigns the type this UI controller handles.
	 * 
	 * @param type handled {@link IElementType} of this controller
	 */
	void setType(IElementType type);

	/**
	 * This method executes the default opening method for the current type.
	 */
	void defaultOpen(ITypedObject model);
}
