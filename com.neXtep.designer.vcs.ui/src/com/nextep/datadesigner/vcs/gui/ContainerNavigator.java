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
package com.nextep.datadesigner.vcs.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.exception.NoSuchConnectorException;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.controllers.ContainerUIController;
import com.nextep.designer.vcs.ui.controllers.VersionableController;

/**
 * Navigator GUI for a container object.
 * 
 * @author Christophe Fondacci
 */
public class ContainerNavigator extends TypedNavigator implements IEventListener {

	private static final Log log = LogFactory.getLog(ContainerNavigator.class);

	public ContainerNavigator(IVersionContainer container, ContainerUIController controller) {
		super(container, controller);
	}

	@Override
	public void initializeChildConnectors() {
		IVersionContainer container = (IVersionContainer) getModel();
		// Sorting contents
		List<IVersionable<?>> contents = new ArrayList<IVersionable<?>>(container.getContents());
		Collections.sort(contents);
		// Adding subconnectors
		for (IVersionable<?> v : contents) {
			this.addConnector(new VersionableNavigator(v, VersionableController.getInstance()));// ElementTypeHelper.getNavigatorInstance(v.getType(),v.getVersionnedObject()));
			// Forcing consistency
			if (v.getContainer() != container) {
				try {
					Observable.deactivateListeners();
					v.setContainer(container);
				} finally {
					Observable.activateListeners();
				}

			}
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return ImageFactory.getImage(IElementType.getInstance(IVersionContainer.TYPE_ID)
				.getCategoryIcon());
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getType()
	 */
	public IElementType getType() {
		return IElementType.getInstance(IVersionContainer.TYPE_ID);
	}

	public void handleEvent(ChangeEvent event, IObservable source, Object o) {
		IVersionContainer container = (IVersionContainer) getModel();
		if (source != null && source != container) {
			container = (IVersionContainer) source;
			log.debug("ContainerNavigator: Switched versionable");
		}
		switch (event) {
		case VERSIONABLE_ADDED:
			IVersionable<?> v = (IVersionable<?>) o;
			// Adding to container
			// container.addVersionable(v); //TODO: transfer operation to ContainerController
			this.addConnector(new VersionableNavigator(v, VersionableController.getInstance()));
			// controller.modelChanged(v);
			// controller.modelChanged(container);
			// initialize();
			break;
		case VERSIONABLE_REMOVED:
			try {
				INavigatorConnector c = this.getConnector((IVersionable<?>) o);
				this.removeConnector(c);
			} catch (NoSuchConnectorException e) {
				// Already removed, nothing to do
			}
			break;

		}
		refreshConnector();
	}

}
