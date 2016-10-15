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
package com.nextep.datadesigner.dbgm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;

/**
 * @author Christophe Fondacci
 */
public class View extends SynchedVersionable<IView> implements IView {

	private static final Log log = LogFactory.getLog(View.class);
	private String sql;
	private List<String> columnAliases = new ArrayList<String>();
	private Set<ITrigger> triggers;

	public View() {
		nameHelper.setFormatter(DBGMHelper.getCurrentVendor().getNameFormatter());
		triggers = new HashSet<ITrigger>();
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IView#getColumns()
	 */
	@Override
	public List<IBasicColumn> getColumns() {
		// TODO should parse the view and link to referenced table columns
		return null;
	}

	/**
	 * @deprecated use standard abstract method getSql() instead
	 */
	@Deprecated
	@Override
	public String getSQLDefinition() {
		return sql;
	}

	/**
	 * @deprecated use standard abstract method setSql() instead
	 */
	@Deprecated
	@Override
	public void setSQLDefinition(String sql) {
		setSql(sql);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IView#addColumnAlias(java.lang.String)
	 */
	@Override
	public void addColumnAlias(String alias) {
		columnAliases.add(alias);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IView#getColumnAliases()
	 */
	@Override
	public List<String> getColumnAliases() {
		return columnAliases;
	}

	/**
	 * Hibernate alias setter
	 * 
	 * @param aliases
	 */
	protected void setColumnAliases(List<String> aliases) {
		this.columnAliases = aliases;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IView#removeColumnAlias(java.lang.String)
	 */
	@Override
	public void removeColumnAlias(int i) {
		columnAliases.remove(i);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IView#setColumnAlias(int, java.lang.String)
	 */
	@Override
	public void setColumnAlias(int i, String alias) {
		if (!columnAliases.isEmpty() && i < columnAliases.size()) {
			columnAliases.remove(i);
			columnAliases.add(i, alias);

		} else {
			// Filling any missing alias
			for (int k = columnAliases.size() - 1; k < i - 1; k++) {
				columnAliases.add("");
			}
			columnAliases.add(alias);
		}
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#addTrigger(com.nextep.datadesigner.dbgm.model.ITrigger)
	 */
	@Override
	public void addTrigger(ITrigger trigger) {
		if (triggers.add(trigger)) {
			notifyListeners(ChangeEvent.TRIGGER_ADDED, trigger);
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#getTriggers()
	 */
	@Override
	public Collection<ITrigger> getTriggers() {
		return triggers;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IBasicTable#removeTrigger(com.nextep.datadesigner.dbgm.model.ITrigger)
	 */
	@Override
	public void removeTrigger(ITrigger trigger) {
		if (triggers.remove(trigger)) {
			notifyListeners(ChangeEvent.TRIGGER_REMOVED, trigger);
		}
	}

	@Override
	public String getSql() {
		return sql;
	}

	@Override
	public void addColumn(IBasicColumn column) {
		log.debug("Trying to add a view column [" + column.toString() + "] : void operation");
	}

	@Override
	public void removeColumn(IBasicColumn column) {
		log.debug("Trying to remove a view column [" + column.toString() + "] : void operation");
	}

	@Override
	public void setSql(String sql) {
		this.sql = sql;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

}
