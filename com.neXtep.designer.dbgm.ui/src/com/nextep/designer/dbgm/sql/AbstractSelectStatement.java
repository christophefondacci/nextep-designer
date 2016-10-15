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
///**
// * Copyright (c) 2008 neXtep Softwares.
// * All rights reserved. Terms of the neXtep licence
// * are available at http://www.nextep-softwares.com
// */
//package com.nextep.designer.dbgm.sql;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.nextep.datadesigner.impl.Observable;
//import com.nextep.datadesigner.model.ChangeEvent;
//
///**
// * @author Christophe Fondacci
// *
// */
//public abstract class AbstractSelectStatement extends Observable implements
//		ISelectStatement {
//	private List<TableAlias> fromTables;
//	private List<ColumnAlias> selectedCols;
//	private String postFromFrag;
//	/** The SQL string accessible to implementors */
//	protected String sql;
//	
//	public AbstractSelectStatement(String sql) {
//		fromTables = new ArrayList<TableAlias>();
//		selectedCols = new ArrayList<ColumnAlias>();
//		setSQL(sql);
//	}
//	/**
//	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#addFromTable(com.nextep.designer.dbgm.sql.TableAlias)
//	 */
//	@Override
//	public void addFromTable(TableAlias t) {
//		fromTables.add(t);
//		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
//	}
//
//	/**
//	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#addSelectedColumn(com.nextep.designer.dbgm.sql.ColumnAlias)
//	 */
//	@Override
//	public void addSelectedColumn(ColumnAlias c) {
//		selectedCols.add(c);
//		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
//	}
//
//	/**
//	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#getFromTables()
//	 */
//	@Override
//	public List<TableAlias> getFromTables() {
//		return fromTables;
//	}
//
//	/**
//	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#getPostFromFragment()
//	 */
//	@Override
//	public String getPostFromFragment() {
//		return postFromFrag;
//	}
//
//	/**
//	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#getSelectedColumns()
//	 */
//	@Override
//	public List<ColumnAlias> getSelectedColumns() {
//		return selectedCols;
//	}
//
//	/**
//	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#removeFromTable(com.nextep.designer.dbgm.sql.TableAlias)
//	 */
//	@Override
//	public void removeFromTable(String tableName, String tableAlias) {
//		TableAlias a = new TableAlias(tableName);
//		a.setAlias(tableAlias);
//		fromTables.remove(a);
//		notifyListeners(ChangeEvent.MODEL_CHANGED,null);
//	}
//
//	/**
//	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#removeSelectedColumn(com.nextep.designer.dbgm.sql.ColumnAlias)
//	 */
//	@Override
//	public void removeSelectedColumn(String columnName, String columnAlias) {
//		ColumnAlias a = new ColumnAlias();
//		a.setColumnName(columnName);
//		a.setColumnAlias(columnAlias);
//		selectedCols.remove(a);
//		notifyListeners(ChangeEvent.MODEL_CHANGED,null);
//	}
//
//	/**
//	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#setPostFromFragment(java.lang.String)
//	 */
//	@Override
//	public void setPostFromFragment(String postFrom) {
//		postFromFrag = postFrom;
//		notifyListeners(ChangeEvent.MODEL_CHANGED,null);
//	}
//
//	/**
//	 * @see com.nextep.designer.dbgm.sql.ISelectStatement#setSQL(java.lang.String)
//	 */
//	@Override
//	public void setSQL(String sql) {
//		this.sql = sql;
//		clear();
//		parse();
//		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
//	}
//	
//	protected void clear() {
//		fromTables.clear();
//		selectedCols.clear();
//		postFromFrag = "";
//	}
//	/**
//	 * Parses the <code>sql</code> protected string variable
//	 * to initialize the structure. 
//	 */
//	protected abstract void parse();
//
//}
