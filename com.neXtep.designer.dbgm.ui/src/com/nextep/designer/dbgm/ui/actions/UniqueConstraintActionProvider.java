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
package com.nextep.designer.dbgm.ui.actions;

import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * This class provides action for the Unique key editor page under the actions of the unique key
 * list.
 * 
 * @author Christophe Fondacci
 */
public class UniqueConstraintActionProvider extends AbstractFormActionProvider {

	@Override
	public Object add(ITypedObject parent) {
		final IBasicTable table = (IBasicTable) parent;
		final ITypedObjectUIController controller = UIControllerFactory.getController(IElementType
				.getInstance(UniqueKeyConstraint.TYPE_ID));
		int keyCount = table.getConstraints().size();
		final ITypedObject o = (ITypedObject) controller.emptyInstance("UK" + keyCount, parent);
		final ITypedObjectUIController objectController = UIControllerFactory.getController(o);
		objectController.defaultOpen(o);
		return o;
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		final IKeyConstraint constraint = (IKeyConstraint) toRemove;
		CorePlugin.getService(IWorkspaceUIService.class).remove(constraint);
	}

}
