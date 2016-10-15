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
package com.nextep.datadesigner.gui.model;

import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.designer.ui.model.IUIComponent;

/**
 * This interface defines an entity which will be able to be connected to the whole IDE GUI.
 * 
 * @author Christophe_Fondacci
 */
public interface IDisplayConnector extends IConnector<Control, IDisplayConnector>, IEventListener,
		DisposeListener, IUIComponent {

	/**
	 * Focuses on the connector. For containers connector, a child object could be provided. If so,
	 * the connector will focus on itself and show the child connector.
	 * 
	 * @param childFocus child connector to be shown.
	 */
	public abstract void focus(IDisplayConnector childFocus);

}
