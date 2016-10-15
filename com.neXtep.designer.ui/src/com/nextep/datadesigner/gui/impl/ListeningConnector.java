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
package com.nextep.datadesigner.gui.impl;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IConnector;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;

/**
 * A connector which listens to its underlying object model. Most of all it will provide a generic
 * implementation for handling the model switch properly.
 * 
 * @author Christophe Fondacci
 */
public abstract class ListeningConnector<T extends Widget, U extends IConnector<?, ?>> implements
		IConnector<T, U>, IEventListener, DisposeListener {

	// private static final Log log = LogFactory.getLog(ListeningConnector.class);
	private IEventListener modelListener;
	/** Our observable model */
	private IObservable model;

	/**
	 * Default constructor.
	 * 
	 * @param model observable model on which this connector is built
	 */
	protected ListeningConnector(IObservable model) {
		// Registering listeners
		addListeners(model);
		// Registering model
		setModel(model);
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#getModel()
	 */
	@Override
	public Object getModel() {
		return model;
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(final Object model) {
		if (this.model != model) {
			this.model = (IObservable) model;
			// Ensuring a thread safe event dispatch
			if (isInitialized()) {
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						refreshConnector();
					}
				});
			}
		}
	}

	/**
	 * Adds listeners on the specified model
	 * 
	 * @param model model object to listen to
	 */
	protected void addListeners(IObservable model) {
		if (model != null) {
			// Registering the model listener
			modelListener = createModelListener(model);
			// Patch for ConcurrentModification bug:
			// the listeners now automatically delay if they are notifying
			Designer.getListenerService().registerListener(this, model, modelListener);
		}
	}

	/**
	 * Creates and returns a new model listeners. This method may be extended to add custom model
	 * listeners. By default, the model listener is the connector itself.
	 * 
	 * @param model model to listen to
	 * @return a new IEventListener which listens to the model
	 */
	protected IEventListener createModelListener(IObservable model) {
		return this;
		// new IEventListener() {
		//
		// @Override
		// public void handleEvent(final ChangeEvent event, final IObservable source,
		// final Object data) {
		// Display.getDefault().syncExec(new Runnable() {
		//
		// @Override
		// public void run() {
		// ListeningConnector.this.handleEvent(event, source, data);
		// }
		// });
		// }
		// };
	}

	/**
	 * Default unimplemented feature
	 * 
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#removeConnector(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	public void removeConnector(U child) {
		throw new ErrorException("RemoveConnector: Unimplemented feature.");
	}

	/**
	 * Default unimplemented feature
	 * 
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#addConnector(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	public void addConnector(U child) {
		throw new ErrorException("AddConnector: Unimplemented feature.");
	}

	/**
	 * Default unimplemented feature
	 * 
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectors()
	 */
	public Collection<U> getConnectors() {
		return Collections.emptyList();
		// throw new ErrorException(this.getClass().getName() +
		// " - getConnectors: Unimplemented feature.");
	}

	/**
	 * Default implementation of the connector disposal
	 * 
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent event) {
		releaseConnector();
	}

	/**
	 * Default implementation of the connector release which dispatches the release to child
	 * connectors.
	 * 
	 * @see com.nextep.datadesigner.gui.model.IConnector#releaseConnector()
	 */
	public void releaseConnector() {
		for (IConnector<?, ?> c : getConnectors()) {
			c.releaseConnector();
		}
	}

	/**
	 * A helper method to ensure not null string while refreshing SWT widgets.
	 * 
	 * @param input input string to display which might be <code>null</code>
	 * @return a non-null string which might be empty
	 */
	protected String notNull(String input) {
		return input == null ? "" : input;
	}

	protected String strVal(Object o) {
		return o == null ? "" : notNull(o.toString());
	}
}
