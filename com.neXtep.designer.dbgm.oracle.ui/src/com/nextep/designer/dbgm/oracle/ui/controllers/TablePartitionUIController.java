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
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.gui.external.VersionableDisplayDecorator;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.oracle.impl.TablePartition;
import com.nextep.designer.dbgm.oracle.impl.external.PartitionPhysicalProperties;
import com.nextep.designer.dbgm.oracle.ui.impl.PartitionNavigator;
import com.nextep.designer.dbgm.oracle.ui.impl.TablePartitionEditor;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.ui.model.base.AbstractUIController;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class TablePartitionUIController extends AbstractUIController {

	public TablePartitionUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		final ITablePartition part = (ITablePartition) content;
		final IBasicTable table = CorePlugin.getService(ICoreService.class).getFirstTypedParent(
				part, IBasicTable.class);

		if (table != null) {
			return new VersionableDisplayDecorator(new TablePartitionEditor(part, this),
					VersionHelper.getVersionable(table));
		} else {
			return new TablePartitionEditor(part, this);
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
		// Type safety
		if (!(parent instanceof IPhysicalProperties)) {
			throw new ErrorException("Cannot create a partition on this object.");
		}
		IPartitionable p = (IPartitionable) parent;
		ITablePartition tp = new TablePartition();
		tp.setParent(p);
		// Initializing empty physical properties for the new partition
		tp.setPhysicalProperties(new PartitionPhysicalProperties());
		newWizardEdition("Table partition creation wizard", initializeEditor(tp));

		save(tp);
		p.addPartition(tp);
		if (p instanceof IdentifiedObject) {
			save((IdentifiedObject) p);
		}

		return tp;
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		// Type safety
		if (!(parent instanceof IPhysicalProperties)) {
			throw new ErrorException("Cannot create a partition on this object.");
		}
		IPartitionable p = (IPartitionable) parent;
		ITablePartition tp = new TablePartition();
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
		return TypedFormRCPEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);
		final IBasicTable parentTable = coreService.getFirstTypedParent((ITablePartition) model,
				IBasicTable.class);
		return new TypedEditorInput(parentTable);
	}
}
