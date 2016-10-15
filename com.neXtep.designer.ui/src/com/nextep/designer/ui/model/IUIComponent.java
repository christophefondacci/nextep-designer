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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A UI component is a very simple abstraction of any user interface. Every neXtep UI interaction is
 * made through an implementation of an UI component. It allows to decouple completely the component
 * from its context by allowing to develop generic components which could be embedded in dialogs,
 * editors, property pages, wizards in the exact same way.
 * 
 * @author Christophe Fondacci
 */
public interface IUIComponent {

	/**
	 * Creates a new UI component in the parent composite
	 * 
	 * @param parent the parent SWT container
	 * @return the SWT control containing our UI component
	 */
	Control create(Composite parent);

	/**
	 * Called when this UI component is disposed, allowing implementation to release resources
	 */
	void dispose();

	/**
	 * Defines the container in which the UI component is embed.
	 * 
	 * @param container the {@link IUIComponentContainer} containing this component
	 */
	void setUIComponentContainer(IUIComponentContainer container);

	/**
	 * Retrieves the container in which the UI component is embed.
	 * 
	 * @return the {@link IUIComponentContainer} containing this component or <code>null</code>
	 */
	IUIComponentContainer getUIComponentContainer();
}
