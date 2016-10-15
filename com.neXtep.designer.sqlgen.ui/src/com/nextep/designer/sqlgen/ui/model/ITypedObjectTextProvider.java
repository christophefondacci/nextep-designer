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
package com.nextep.designer.sqlgen.ui.model;

import java.util.List;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.sqlgen.ui.editors.SQLEditor;

/**
 * This interface is an abstractions for contributions to the SQL editors of the
 * neXtep environment. This interface provides entry points method to :<br>
 * - Transform text elements back to concrete typed elements<br>
 * - Lists all textual representation of current view's elements<br>
 * - Opens an element<br>
 * <br>
 * This interface is typically used by {@link SQLEditor} for completion proposals
 * and hyperlinking.
 * 
 * @author Christophe Fondacci
 *
 */
public interface ITypedObjectTextProvider {

	/**
	 * Lists all provided elements
	 * @return a list of String representing the textual representation of the
	 * 		  element.
	 */
	public List<String> listProvidedElements();
	/**
	 * Retrieves the icon image which should be displayed with this element.
	 * @param element textual representation of the element.
	 * @return the icon image (16x16)
	 */
//	public Image getImageFor(String element);
	/**
	 * Retrieves the element instance referenced by the specified textual representation.
	 * 
	 * @param elementName textual representation of the element
	 * @return the element instance
	 */
	public ITypedObject getElement(String elementName);
	/**
	 * Opens the specified element. If you already have a {@link ITypedObject} you
	 * should rather open it through its {@link INavigatorConnector#defaultAction()}
	 * method. 
	 * 
	 * @param elementName textual representation of the element to open
	 * @return a boolean indicating if the element has been opened. Since there 
	 * 		   could be multiple text provider, the framework will iterate through
	 * 		   all of them until the first provider returns <code>true</code> to 
	 * 		   this method.
	 */
	public boolean open(String elementName);
}
