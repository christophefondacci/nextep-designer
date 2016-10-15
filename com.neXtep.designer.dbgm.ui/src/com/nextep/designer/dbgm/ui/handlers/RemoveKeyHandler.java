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
/**
 *
 */
package com.nextep.designer.dbgm.ui.handlers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * A specific handler which removes key constraints
 * 
 * @author Christophe Fondacci
 */
public class RemoveKeyHandler extends AbstractHandler {

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final List<?> models = UIHelper.getSelectedModel(window);
		final List<IReferenceable> keysToRemove = new ArrayList<IReferenceable>();
		for (Object model : models) {
			if (model instanceof IKeyConstraint) {
				keysToRemove.add((IKeyConstraint) model);
			}
		}
		getViewUIService().remove(keysToRemove.toArray(new IReferenceable[keysToRemove.size()]));
		return null;
	}

	private IWorkspaceUIService getViewUIService() {
		return VCSUIPlugin.getService(IWorkspaceUIService.class);
	}
}
