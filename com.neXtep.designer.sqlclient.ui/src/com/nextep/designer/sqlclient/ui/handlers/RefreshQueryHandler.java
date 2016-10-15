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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLQueryListener;
import com.nextep.designer.sqlclient.ui.rcp.SQLResultsView;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;

/**
 * This handler refreshed the current query by running it again from the {@link ISQLClientService} .
 * 
 * @author Christophe Fondacci
 */
public class RefreshQueryHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof SQLResultsView) {
			final SQLResultsView view = (SQLResultsView) part;
			ISQLQuery query = (ISQLQuery) view.getAdapter(ISQLQuery.class);
			if (query != null) {
				SQLClientPlugin.getService(ISQLClientService.class).runQuery(query.getConnection(),
						query.getSql(),
						(ISQLQueryListener) view.getAdapter(ISQLQueryListener.class));
			}
		}
		return null;
	}

}
