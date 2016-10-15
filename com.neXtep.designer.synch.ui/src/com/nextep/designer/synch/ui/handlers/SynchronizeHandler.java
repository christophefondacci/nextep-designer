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
package com.nextep.designer.synch.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.dbgm.ui.services.DBGMUIHelper;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * @author Christophe Fondacci
 */
public class SynchronizeHandler extends AbstractHandler {

	// private static final Log log = LogFactory.getLog(SynchronizeHandler.class);
	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IConnection conn = null;
		// If current selection is a connection and matches default generation target, we use it
		final ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection && !s.isEmpty()) {
			Object model = ((IStructuredSelection) s).getFirstElement();
			if (model instanceof IConnection) {
				conn = (IConnection) model;
				DBGMUIHelper.checkConnectionPassword(conn);
			}
		}
		if (conn == null) {
			conn = DBGMUIHelper.getConnection(SQLGenUtil.getDefaultTargetType());
		}
		ISynchronizationUIService synchService = (ISynchronizationUIService) PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart()
				.getSite().getService(ISynchronizationUIService.class);
		// SynchUIPlugin
		// .getService(ISynchronizationService.class);
		synchService.synchronize(VCSPlugin.getService(IWorkspaceService.class).getCurrentWorkspace(), conn,
				null);
		return null;
	}

}
