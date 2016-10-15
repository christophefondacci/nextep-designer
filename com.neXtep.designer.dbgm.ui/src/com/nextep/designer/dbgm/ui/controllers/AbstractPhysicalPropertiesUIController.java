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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.ui.controllers;

import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.external.VersionableDisplayDecorator;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.editors.PhysicalPropertiesEditor;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public abstract class AbstractPhysicalPropertiesUIController extends AbstractUIController {

	public AbstractPhysicalPropertiesUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.PARTITION_ADDED);
		addSaveEvent(ChangeEvent.PARTITION_REMOVED);
		addSaveEvent(ChangeEvent.COLUMN_ADDED);
		addSaveEvent(ChangeEvent.COLUMN_REMOVED);
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return null;
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		IPhysicalProperties props = (IPhysicalProperties) content;
		return new VersionableDisplayDecorator(new PhysicalPropertiesEditor(props, this, false,
				false), VersionHelper.getVersionable(props));
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
	public Object newInstance(Object parent) {
		return emptyInstance(null, parent);
	}

	private IPhysicalProperties create(Object parent) {
		IPhysicalObject t = (IPhysicalObject) parent;
		// Ensuring modifiable parent table
		t = VCSUIPlugin.getVersioningUIService().ensureModifiable(t);
		// Checking pre-existence of physical properties
		if (t.getPhysicalProperties() != null) {
			throw new ErrorException(
					DBGMUIMessages.getString("controller.physProps.alreadyDefinedError")); //$NON-NLS-1$
		}
		// We're clear, creating new instance
		IPhysicalProperties props = CorePlugin.getTypedObjectFactory().create(getClassToCreate());
		props.setParent(t);
		return props;
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		IPhysicalObject t = (IPhysicalObject) parent;
		IPhysicalProperties props = create(parent);
		if (props instanceof INamedObject && name != null) {
			((INamedObject) props).setName(name);
		}
		t.setPhysicalProperties(props);
		save(props);
		return props;
	}

	protected abstract <T extends IPhysicalProperties> Class<T> getClassToCreate();

	@Override
	public String getEditorId() {
		return TypedFormRCPEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new TypedEditorInput(((IPhysicalProperties) model).getParent());
	}
}
