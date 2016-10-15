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
package com.nextep.designer.vcs.ui.controllers;

import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.gui.ContainerEditorGUI;
import com.nextep.datadesigner.vcs.gui.ContainerNavigator;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class ContainerUIController extends VersionableController {

	// private static final Log log = LogFactory.getLog(ContainerUIController.class);

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new ContainerEditorGUI((IVersionContainer) content, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		return null;
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		return null;
	}

	@Override
	protected void beforeCreation(IVersionable<?> v, Object parent) {
		IVersionContainer c = (IVersionContainer) v.getVersionnedObject().getModel();
		c.setDBVendor(((IVersionContainer) parent).getDBVendor());
	}

	@Override
	protected void beforeEdition(IVersionable<?> v, IVersionContainer container, Object parent) {
		IVersionContainer c = (IVersionContainer) v.getVersionnedObject().getModel();
		c.setDBVendor(container.getDBVendor());
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new ContainerNavigator((IVersionContainer) model, this);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case VERSIONABLE_ADDED:
			save((IdentifiedObject) data);
			save((IdentifiedObject) source);
			break;
		case VERSIONABLE_REMOVED:
		case MODEL_CHANGED:
			save((IdentifiedObject) source);
			break;
		}
	}

	@Override
	public String getEditorId() {
		return "com.neXtep.designer.dbgm.ui.typedFormEditor"; //$NON-NLS-1$
	}
}
