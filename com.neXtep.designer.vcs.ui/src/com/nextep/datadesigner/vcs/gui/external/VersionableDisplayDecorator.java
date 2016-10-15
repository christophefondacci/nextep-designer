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
package com.nextep.datadesigner.vcs.gui.external;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This decorator wraps a display connector to ensure proper model changes propagations for non
 * versioned elements which are contained in a versioned object. The underlying connector will not
 * have to bother with its related versionable.
 * 
 * @author Christophe Fondacci
 */
public class VersionableDisplayDecorator extends ControlledDisplayConnector {

	private static final Log log = LogFactory.getLog(VersionableDisplayDecorator.class);
	private IDisplayConnector connector;

	public VersionableDisplayDecorator(IDisplayConnector connector, IVersionable<?> versionable) {
		super(versionable, null);
		this.connector = connector;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		throw new ErrorException("Unimplemented feature, should never be called");
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#create(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control create(Composite parent) {
		Control c = connector.create(parent);
		initialize();
		Designer.getListenerService().activateListeners(this);
		return c;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return connector.getSWTConnector();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		return connector.getTitle();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		if (connector.getSWTConnector() != null && !connector.getSWTConnector().isDisposed()) {
			connector.refreshConnector();
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// Nothing to dispatch
		switch (event) {
		case UPDATES_LOCKED:
			refreshConnector();
			break;
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(Object model) {
		super.setModel(model);
		if (connector != null) {
			// This is our versionable listener, so we know we have a versionable
			IVersionable<?> v = (IVersionable<?>) model;
			Map<IReference, IReferenceable> refMap = v.getReferenceMap();
			// We locate our unversioned model in our new parent versionable
			IReference unversionedModelRef = ((IReferenceable) connector.getModel()).getReference();
			IReferenceable newUnversionnedModel = refMap.get(unversionedModelRef);
			// If the unversioned model reference does not exist we should remove the connector
			if (newUnversionnedModel == null) {
				// log.warn("!SHOULD Remove connector <" + connector.getClass().getName()
				// +"> which does no more exist in new parent model!");
				for (IEditorReference ref : PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().getEditorReferences()) {
					try {
						final IEditorInput input = ref.getEditorInput();
						if (input instanceof TypedEditorInput) {
							if (connector.getModel() == ((TypedEditorInput) input).getModel()) {
								PlatformUI.getWorkbench().getActiveWorkbenchWindow()
										.getActivePage().closeEditor(ref.getEditor(true), false);
								break;
							}
						}
					} catch (PartInitException e) {
						log.error("Unable to close editor.");
					}
				}
				// At least we dispose the connector
				// this.getSWTConnector().dispose();
			} else {
				log.debug("Switching unversionned model from <" + connector.getModel() + "> to <"
						+ newUnversionnedModel + ">");
				connector.setModel(newUnversionnedModel);
			}
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return connector.getConnectorIcon();
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#addConnector(com.nextep.datadesigner.gui.model.IConnector)
	 */
	@Override
	public void addConnector(IDisplayConnector child) {
		connector.addConnector(child);
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#getConnectors()
	 */
	@Override
	public Collection<IDisplayConnector> getConnectors() {
		return connector.getConnectors();
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#releaseConnector()
	 */
	@Override
	public void releaseConnector() {
		connector.releaseConnector();
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#removeConnector(com.nextep.datadesigner.gui.model.IConnector)
	 */
	@Override
	public void removeConnector(IDisplayConnector child) {
		connector.removeConnector(child);
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#getModel()
	 */
	@Override
	public Object getModel() {
		// TODO: Check correct behaviour on versioning operations
		return connector.getModel();
	}

	@Override
	public void widgetDisposed(DisposeEvent event) {
		// Unregister listeners instigated by our wrapped connector
		Designer.getListenerService().unregisterListeners(connector);
		super.widgetDisposed(event);
	}

}
