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
package com.nextep.designer.dbgm.sql;

import java.util.ArrayList;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;

/**
 * @author Christophe Fondacci
 */
public abstract class AbstractSelectStatement extends Observable implements ISelectStatement {

	private List<TableAlias> fromTables;
	private List<ColumnAlias> selectedCols;
	private String postFromFrag;
	private boolean parsed;
	private IParseData data;
	/** The SQL string accessible to implementors */
	protected String sql;

	public AbstractSelectStatement(String sql) {
		fromTables = new ArrayList<TableAlias>();
		selectedCols = new ArrayList<ColumnAlias>();
		setSQL(sql);
	}

	/**
	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#addFromTable(com.nextep.designer.dbgm.sql.TableAlias)
	 */
	@Override
	public void addFromTable(TableAlias t) {
		if (!fromTables.contains(t)) {
			fromTables.add(t);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#addSelectedColumn(com.nextep.designer.dbgm.sql.ColumnAlias)
	 */
	@Override
	public void addSelectedColumn(ColumnAlias c) {
		if (!selectedCols.contains(c)) {
			selectedCols.add(c);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#getFromTables()
	 */
	@Override
	public List<TableAlias> getFromTables() {
		return fromTables;
	}

	/**
	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#getPostFromFragment()
	 */
	@Override
	public String getPostFromFragment() {
		return postFromFrag;
	}

	/**
	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#getSelectedColumns()
	 */
	@Override
	public List<ColumnAlias> getSelectedColumns() {
		return selectedCols;
	}

	/**
	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#removeFromTable(com.nextep.designer.dbgm.sql.TableAlias)
	 */
	@Override
	public void removeFromTable(String tableName, String tableAlias) {
		TableAlias a = new TableAlias(tableName);
		a.setAlias(tableAlias);
		fromTables.remove(a);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#removeSelectedColumn(com.nextep.designer.dbgm.sql.ColumnAlias)
	 */
	@Override
	public void removeSelectedColumn(String columnName, String columnAlias) {
		ColumnAlias a = new ColumnAlias();
		a.setColumnName(columnName);
		a.setColumnAlias(columnAlias);
		selectedCols.remove(a);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#setPostFromFragment(java.lang.String)
	 */
	@Override
	public void setPostFromFragment(String postFrom) {
		postFromFrag = postFrom;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#setSQL(java.lang.String)
	 */
	@Override
	public void setSQL(String sql) {
		this.sql = sql;
		setParsed(false);
		clear();
		DBGMHelper.parse(this);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public String getSql() {
		return sql;
	}

	protected void clear() {
		fromTables.clear();
		selectedCols.clear();
		postFromFrag = ""; //$NON-NLS-1$
	}

	@Override
	public IParseData getParseData() {
		return data;
	}

	@Override
	public void setParseData(IParseData parseData) {
		this.data = parseData;
	}

	@Override
	public boolean isParsed() {
		return parsed;
	}

	@Override
	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

	@Override
	public void setSql(String sql) {
		setSQL(sql);
	}

}
