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
package com.nextep.datadesigner.gui.model;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Widget;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.ListeningConnector;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public abstract class ListeningControlledConnector<T extends Widget, U extends IConnector<?, ?>>
		extends ListeningConnector<T, U> {

	private ITypedObjectUIController controller;

	/**
	 * Default constructor which must be called by implementors.
	 * 
	 * @param model object model
	 * @param controller controller of the whole object
	 */
	protected ListeningControlledConnector(IObservable model, ITypedObjectUIController controller) {
		super(model);
		// Setting the controller pointer
		this.controller = controller;
		// Now that it is initialized, we can register our controller listener
		addControllerListener(model);
	}

	/**
	 * Adds listeners on the specified model
	 * 
	 * @param model model object to listen to
	 */
	protected void addListeners(IObservable model) {
		super.addListeners(model);
		addControllerListener(model);
	}

	/**
	 * Externalized controller listeners registration since it will have no effect when called by
	 * the superclass constructor because the controller variable will not be initialized
	 * 
	 * @param model observable object model
	 * @param delayed should the method delay the listener registration
	 */
	protected void addControllerListener(IObservable model) {
		if (controller != null && model != null) {
			// Registering our controller as a listener to our model
			Designer.getListenerService().registerListener(this, model, controller);
		}
	}

	@Override
	public void widgetDisposed(DisposeEvent event) {
		Designer.getListenerService().unregisterListeners(this);
	}

	/**
	 * @return the controller of this connector as an event listener
	 */
	protected ITypedObjectUIController getController() {
		return controller;
	}

	/**
	 * Defines the controller of this display connector. This method should be used while
	 * initializing outside constructor. This method will have no effect when a controller has
	 * already been defined for this instance.
	 * 
	 * @param controller
	 */
	protected void setController(ITypedObjectUIController controller) {
		if (this.controller == null) {
			this.controller = controller;
			addControllerListener((IObservable) getModel());
		}
	}
}
