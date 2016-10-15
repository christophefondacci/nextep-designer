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

import java.sql.SQLException;
import java.util.Iterator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;

public class DeleteRowHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection && !sel.isEmpty()) {
			final IWorkbenchPart part = HandlerUtil.getActivePart(event);
			final ISQLQuery query = (ISQLQuery) part.getAdapter(ISQLQuery.class);
			if (query != null && !query.isRunning()) {
				final IStructuredSelection s = (IStructuredSelection) sel;
				final ISQLClientService sqlClientService = SQLClientPlugin
						.getService(ISQLClientService.class);
				Iterator<?> selIt = s.iterator();
				while (selIt.hasNext()) {
					Object obj = selIt.next();
					if (obj instanceof ISQLRowResult) {
						try {
							sqlClientService.deleteQueryValue(query, (ISQLRowResult) obj);
						} catch (SQLException e) {
							throw new ErrorException("Could not delete the row: " + e.getMessage(),
									e);
						}
					}
				}
			}
		}
		return null;
	}
}
