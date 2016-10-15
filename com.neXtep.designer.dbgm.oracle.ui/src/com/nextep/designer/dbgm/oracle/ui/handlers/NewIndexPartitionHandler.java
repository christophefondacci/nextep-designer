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
package com.nextep.designer.dbgm.oracle.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.vcs.ui.handlers.NewTypedInstanceHandler;

/**
 * A specific handler for new index partition to workaround bug DES-851 and disable the creation of
 * index partition on unique constraint physical properties.
 * 
 * @author Christophe Fondacci
 */
public class NewIndexPartitionHandler extends NewTypedInstanceHandler {

	protected Object newInstance(ExecutionEvent event, Object parent) {
		if (parent instanceof IPhysicalProperties) {
			final IPhysicalObject physObj = CorePlugin.getService(ICoreService.class)
					.getFirstTypedParent((IParentable<?>) parent, IPhysicalObject.class);
			if (physObj instanceof UniqueKeyConstraint) {
				throw new ErrorException(
						DBOMUIMessages.getString("handler.newPartition.unsupportedOnUK")); //$NON-NLS-1$
			}
		}
		return super.newInstance(event, parent);
	}
}
