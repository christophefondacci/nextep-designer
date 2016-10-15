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

import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * Any element which contains a {@link IUIComponent} should provide it to the framework through this
 * interface.
 * 
 * @author Christophe Fondacci
 */
public interface IUIComponentContainer {

	/**
	 * Retrieves the underlying UI component.
	 * 
	 * @return the {@link IUIComponent}
	 */
	IUIComponent getUIComponent();

	/**
	 * Runs the provided runnable. Depending on the container, this method will be hooked to some
	 * native runnable support. When no native runnable context is available, a job will be used to
	 * execute the process.
	 * 
	 * @param block waits for the process to terminate before returning
	 * @param cancellable is the process cancellable (might be not used with all implementations)
	 * @param runnable task to execute
	 */
	void run(boolean block, boolean cancellable, IRunnableWithProgress runnable);

	/**
	 * Sets the current error message to display in this container. Components may call this method
	 * to set an error message which will be displayed differently depending on the container
	 * implementation (wizard, dialog, editors).
	 * 
	 * @param message the error message to display
	 */
	void setErrorMessage(String message);
}
