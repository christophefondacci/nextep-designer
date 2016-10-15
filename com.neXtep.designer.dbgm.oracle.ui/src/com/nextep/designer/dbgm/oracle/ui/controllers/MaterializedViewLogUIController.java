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

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.dbgm.oracle.ui.impl.MaterializedViewLogEditor;
import com.nextep.designer.dbgm.oracle.ui.impl.MaterializedViewLogNavigator;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

public class MaterializedViewLogUIController extends VersionableController {

	public MaterializedViewLogUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new MaterializedViewLogEditor((IMaterializedViewLog) content, this);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new MaterializedViewLogNavigator((IMaterializedViewLog) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void beforeEdition(IVersionable<?> v, IVersionContainer container, Object parent) {
		final IMaterializedViewLog log = (IMaterializedViewLog) v.getVersionnedObject().getModel();
		log.setTableReference(((IBasicTable) parent).getReference());
		super.beforeEdition(v, container, parent);
	}

	@Override
	protected void beforeCreation(IVersionable<?> v, Object parent) {
		final IMaterializedViewLog log = (IMaterializedViewLog) v.getVersionnedObject().getModel();
		log.setTableReference(((IBasicTable) parent).getReference());
	}

	@Override
	protected IVersionContainer emptyInstanceGetContainer(Object parent) {
		if (((ITypedObject) parent).getType() == IElementType.getInstance(IBasicTable.TYPE_ID)) {
			return VersionHelper.getVersionable(parent).getContainer();
		} else {
			return (IVersionContainer) parent;
		}
	}
}
