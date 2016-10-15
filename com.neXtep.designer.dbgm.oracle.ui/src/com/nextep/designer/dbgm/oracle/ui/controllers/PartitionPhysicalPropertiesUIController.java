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
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.external.PartitionPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;
import com.nextep.designer.dbgm.oracle.ui.impl.PartitionPhysicalPropertiesEditor;
import com.nextep.designer.dbgm.oracle.ui.impl.PhysicalPropertiesNavigator;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public class PartitionPhysicalPropertiesUIController extends AbstractUIController {

	public PartitionPhysicalPropertiesUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new PartitionPhysicalPropertiesEditor((IPartitionPhysicalProperties) content, this);
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
		return new PhysicalPropertiesNavigator((IPartitionPhysicalProperties) model, this);
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
		IPartition p = (IPartition) parent;
		// Ensuring modifiable parent index
		p = VCSUIPlugin.getVersioningUIService().ensureModifiable(p);
		// Checking pre-existence of physical properties
		if (p.getPhysicalProperties() != null) {
			throw new ErrorException(
					"Physical properties are already defined for this table and you can only have one definition per table. Please edit existing definition instead.");
		}
		// We're clear, creating new instance
		IPartitionPhysicalProperties props = new PartitionPhysicalProperties();
		props.setParent(p);
		p.setPhysicalProperties(props);
		// Saving
		save(props);
		// Returning new created instance
		return p;
	}

	@Override
	public String getEditorId() {
		return TypedFormRCPEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);
		final IBasicTable table = coreService.getFirstTypedParent((IParentable<?>) model,
				IBasicTable.class);
		if (table != null) {
			return new TypedEditorInput(table);
		} else {
			final IIndex index = coreService.getFirstTypedParent((IParentable<?>) model,
					IIndex.class);
			return new TypedEditorInput(index);
		}
	}

	@Override
	public void defaultOpen(ITypedObject model) {
		if (model instanceof IPhysicalProperties) {
			super.defaultOpen(((IPhysicalProperties) model).getParent());
		}
		super.defaultOpen(model);
	}
}
