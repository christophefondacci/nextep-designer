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
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.dbgm.ui.services.DBGMUIHelper;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;

/**
 * @author Christophe Fondacci
 */
public class ViewSQLDataHandler extends AbstractHandler {

	/**
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		if (!sel.isEmpty() && (sel instanceof IStructuredSelection)) {
			Object selObject = ((IStructuredSelection) sel).getFirstElement();
			if (selObject instanceof IBasicTable) {
				final IConnection conn = DBGMUIHelper.getConnection(null);
				final ISQLClientService clientService = SQLClientPlugin
						.getService(ISQLClientService.class);
				clientService.openSqlClientEditor(conn, "select * from "
						+ ((IBasicTable) selObject).getName());
			}
		}
		return null;
	}

}
