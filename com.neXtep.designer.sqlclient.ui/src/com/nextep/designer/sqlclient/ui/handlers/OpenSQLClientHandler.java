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
package com.nextep.designer.sqlclient.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.dbgm.ui.services.DBGMUIHelper;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;

/**
 * Opens a new SQL client. If current selection is a {@link IConnection} it will be used and opened,
 * else if there is a single connection definition in the workspace it will be used and opened, else
 * user will be queried for a connection and editor will open with the user selection.
 * 
 * @author Christophe Fondacci
 */
public class OpenSQLClientHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IConnection conn = null;
		// If a connection is the current selection, we open SQL client with it
		final ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection) {
			final Object model = ((IStructuredSelection) s).getFirstElement();
			if (model instanceof IConnection) {
				// This is a connection, so we'll use this one
				conn = (IConnection) model;
				DBGMUIHelper.checkConnectionPassword(conn);
			}
		}
		// Otherwise we query the user for a connection
		if (conn == null) {
			conn = DBGMUIHelper.getConnection(null);
		}
		SQLClientPlugin.getService(ISQLClientService.class).openSqlClientEditor(conn);
		return null;
	}

}
