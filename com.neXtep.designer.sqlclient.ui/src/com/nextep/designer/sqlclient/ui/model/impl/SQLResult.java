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
package com.nextep.designer.sqlclient.ui.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLResult;
import com.nextep.designer.sqlclient.ui.model.ISQLResultListener;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;

public class SQLResult implements ISQLResult {

	private List<ISQLRowResult> rows;
	private Set<ISQLResultListener> listeners;
	private ISQLQuery query;

	public SQLResult(ISQLQuery query) {
		this.query = query;
		listeners = Collections.synchronizedSet(new HashSet<ISQLResultListener>());
		rows = new LinkedList<ISQLRowResult>();

	}

	@Override
	public List<ISQLRowResult> getRows() {
		return rows;
	}

	@Override
	public void addRow(ISQLRowResult row) {
		rows.add(row);
		rowAdded(row);
	}

	@Override
	public void removeRow(ISQLRowResult row) {
		rows.remove(row);
		for (ISQLResultListener l : new ArrayList<ISQLResultListener>(listeners)) {
			l.rowsRemoved(this, row);
		}

	}

	@Override
	public void addListener(ISQLResultListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ISQLResultListener listener) {
		listeners.remove(listener);
	}

	@Override
	public ISQLQuery getSQLQuery() {
		return query;
	}

	private void rowAdded(ISQLRowResult row) {
		for (ISQLResultListener l : new ArrayList<ISQLResultListener>(listeners)) {
			l.rowsAdded(this, row);
		}
	}

}
