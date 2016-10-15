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
package com.nextep.designer.dbgm.oracle.ui.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.ui.impl.OracleClusterEditor;
import com.nextep.designer.dbgm.oracle.ui.impl.OracleClusterNavigator;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

public class OracleClusterUIController extends VersionableController {

	private static final Log log = LogFactory.getLog(OracleClusterUIController.class);

	public OracleClusterUIController() {
		super();
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.COLUMN_REMOVED);
		addSaveEvent(ChangeEvent.COLUMN_ADDED);
		addSaveEvent(ChangeEvent.CONSTRAINT_REMOVED);
		addSaveEvent(ChangeEvent.CONSTRAINT_ADDED);
		addSaveEvent(ChangeEvent.GENERIC_CHILD_ADDED);
		addSaveEvent(ChangeEvent.GENERIC_CHILD_REMOVED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new OracleClusterEditor((IOracleCluster) content, this);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new OracleClusterNavigator((IOracleCluster) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void beforeEdition(IVersionable<?> v, IVersionContainer container, Object parent) {
		v.setName("CLUSTER");
		super.beforeEdition(v, container, parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object newInstance(Object parent) {
		IVersionContainer container = emptyInstanceGetContainer(parent);
		// Checking if parent is modifiable
		container = VCSUIPlugin.getVersioningUIService().ensureModifiable(container);
		// Creating versionable from factory
		IVersionable<?> v = VersionableFactory.createVersionable(getType().getInterface());
		beforeEdition(v, container, parent);
		// Editing
		v.setName(getAvailableName(getType()));
		beforeCreation(v, parent);
		// Registering container & saving
		v.setContainer(container);
		save(v);
		// Adding to container
		container.addVersionable(v, new ImportPolicyAddOnly());
		IVersionable<IOracleCluster> cl = (IVersionable<IOracleCluster>) v;
		// We should index cluster columns
		IIndex index = (IIndex) UIControllerFactory.getController(
				IElementType.getInstance(IIndex.INDEX_TYPE)).emptyInstance(cl.getName() + "_I", cl);
		for (IBasicColumn c : cl.getVersionnedObject().getModel().getColumns()) {
			index.addColumnRef(c.getReference());
		}
		log.info("Index " + index.getName() + " added for indexed cluster " + cl.getName());
		return cl;
	}

	@Override
	public String getEditorId() {
		return TypedFormRCPEditor.EDITOR_ID;
	}
}
