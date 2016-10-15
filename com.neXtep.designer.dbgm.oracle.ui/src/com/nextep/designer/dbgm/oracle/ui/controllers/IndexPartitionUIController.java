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
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.gui.external.VersionableDisplayDecorator;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IIndexPartition;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.oracle.impl.IndexPartition;
import com.nextep.designer.dbgm.oracle.impl.external.PartitionPhysicalProperties;
import com.nextep.designer.dbgm.oracle.ui.impl.IndexPartitionEditor;
import com.nextep.designer.dbgm.oracle.ui.impl.PartitionNavigator;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class IndexPartitionUIController extends AbstractUIController {

	public IndexPartitionUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		IIndexPartition part = (IIndexPartition) content;
		IIndex index = CorePlugin.getService(ICoreService.class).getFirstTypedParent(part,
				IIndex.class);

		if (index != null) {
			return new VersionableDisplayDecorator(new IndexPartitionEditor(part, this),
					VersionHelper.getVersionable(index));
		} else {
			return new IndexPartitionEditor(part, this);
		}
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new PartitionNavigator((IPartition) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		return null;
	}

	@Override
	public Object newInstance(Object parent) {
		return emptyInstance(getAvailableName(getType()), parent);
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		// Type safety
		if (!(parent instanceof IPartitionable)) {
			throw new ErrorException("Cannot create a partition on this object.");
		}
		IPartitionable p = (IPartitionable) parent;
		IIndexPartition tp = new IndexPartition();
		tp.setName(name);
		tp.setParent(p);
		// Initializing empty physical properties for the new partition
		tp.setPhysicalProperties(new PartitionPhysicalProperties());

		save(tp);
		p.addPartition(tp);
		if (p instanceof IdentifiedObject) {
			save((IdentifiedObject) p);
		}

		return tp;
	}

	@Override
	public String getEditorId() {
		final ITypedObjectUIController controller = UIControllerFactory.getController(IElementType
				.getInstance(IIndex.INDEX_TYPE));
		return controller.getEditorId();
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);
		final IIndex parentIndex = coreService.getFirstTypedParent((IIndexPartition) model,
				IIndex.class);
		final ITypedObjectUIController controller = UIControllerFactory.getController(IElementType
				.getInstance(IIndex.INDEX_TYPE));
		return controller.getEditorInput(parentIndex);
	}
}
