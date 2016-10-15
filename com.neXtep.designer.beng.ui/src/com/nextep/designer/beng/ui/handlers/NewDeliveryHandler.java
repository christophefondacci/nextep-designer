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
package com.nextep.designer.beng.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.ui.handlers.NewTypedInstanceHandler;

/**
 * An extension of the default typed handler to avoir the 
 * "ensureModifiable" call. A delivery does not need to be
 * created on a checked out container. 
 * 
 * @author Christophe Fondacci
 *
 */
public class NewDeliveryHandler extends NewTypedInstanceHandler {

	/**
	 * @see com.nextep.designer.vcs.ui.handlers.NewTypedInstanceHandler#newInstance(org.eclipse.core.commands.ExecutionEvent, java.lang.Object)
	 */
	@Override
	protected Object newInstance(ExecutionEvent event, Object parent) {
		String typeId = event.getParameter("com.neXtep.designer.core.typeId"); //$NON-NLS-1$
		if(typeId == null || "".equals(typeId)) { //$NON-NLS-1$
			throw new ErrorException("No command typeId provided, cannot execute.");
		} else {
			return UIControllerFactory.getController(IElementType.getInstance(typeId)).newInstance(parent);
		}
	}
}
