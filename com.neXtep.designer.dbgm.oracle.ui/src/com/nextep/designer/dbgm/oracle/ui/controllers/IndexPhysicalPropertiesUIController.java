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

import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.external.VersionableDisplayDecorator;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.external.OracleIndexPhysicalProperties;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.dbgm.oracle.ui.impl.IndexPhysicalPropertiesEditor;
import com.nextep.designer.dbgm.oracle.ui.impl.PhysicalPropertiesNavigator;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public class IndexPhysicalPropertiesUIController extends AbstractUIController {

	public IndexPhysicalPropertiesUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.PARTITION_ADDED);
		addSaveEvent(ChangeEvent.PARTITION_REMOVED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		IIndexPhysicalProperties props = (IIndexPhysicalProperties) content;
		return new VersionableDisplayDecorator(new IndexPhysicalPropertiesEditor(props, this),
				VersionHelper.getVersionable(props.getParent()));
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeGraphical(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new PhysicalPropertiesNavigator((IPhysicalProperties) model, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	@Override
	public Object newInstance(Object parent) {
		return emptyInstance(null, parent);
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		IPhysicalObject i = (IPhysicalObject) parent;
		// Ensuring modifiable parent index
		i = VCSUIPlugin.getVersioningUIService().ensureModifiable(i);
		// Checking pre-existence of physical properties
		if (i.getPhysicalProperties() != null) {
			throw new ErrorException(
					DBOMUIMessages.getString("controller.indPhysProps.alreadyDefined")); //$NON-NLS-1$
		}
		// We're clear, creating new instance
		IPhysicalProperties props = new OracleIndexPhysicalProperties();
		props.setParent(i);
		i.setPhysicalProperties(props);
		save(props);
		return props;
	}

	@Override
	public String getEditorId() {
		return TypedFormRCPEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		ITypedObject parent = null;
		if (model instanceof IPhysicalProperties) {
			parent = ((IPhysicalProperties) model).getParent();
		} else if (model instanceof IPhysicalObject) {
			parent = model;
		}

		if (parent instanceof IIndex) {
			parent = ((IIndex) parent).getIndexedTable();
		} else if (parent instanceof UniqueKeyConstraint) {
			parent = ((UniqueKeyConstraint) parent).getParent();
		}
		return new TypedEditorInput(parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void defaultOpen(ITypedObject model) {
		if (model instanceof IParentable<?>) {
			super.defaultOpen(((IParentable<ITypedObject>) model).getParent());
		}
		super.defaultOpen(model);
	}
}
