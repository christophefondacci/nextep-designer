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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.designer.sqlclient.ui.model.INextepMetadata;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLResult;
import com.nextep.designer.sqlclient.ui.model.impl.SQLRowResult;

public class InsertRowHandler extends AbstractHandler {

	private final static Log LOGGER = LogFactory.getLog(InsertRowHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		final ISQLQuery query = (ISQLQuery) part.getAdapter(ISQLQuery.class);
		if (query != null) {
			final ISQLResult result = query.getResult();
			final INextepMetadata md = query.getMetadata();
			if (result != null && md != null) {
				final SQLRowResult newRow = new SQLRowResult();
				for (int i = 1; i <= md.getColumnCount(); i++) {
					newRow.addValue(null);
					newRow.addSqlType(md.getColumnType(i));
				}
				newRow.setPending(true);
				result.addRow(newRow);
			}
		}
		return null;
	}

}
