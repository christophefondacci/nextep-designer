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
package com.nextep.datadesigner.sqlgen.impl;

import java.math.BigDecimal;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.impl.SelfControlVersionable;

/**
 * This class wraps a versionable around a {@link ISQLScript}.<br>
 * Because most of the time, SQL scripts are used internally in memory for SQL generation or for the
 * SQL client, we don't need to bother with version management.<br>
 * When the scripts need to be imported in a view or container, they only need to be wrapped by this
 * class.<br>
 * 
 * @author Christophe Fondacci
 */
public class VersionedSQLScript extends SelfControlVersionable<ISQLScript> implements ISQLScript,
		ILockable<ISQLScript> {

	private ISQLScript s;
	private IEventListener eventDispatcher;

	public VersionedSQLScript() {
		s = new SQLScript();
		// We're listening to the wrapped events to notify listeners on this wrapper
		eventDispatcher = new IEventListener() {

			@Override
			public void handleEvent(ChangeEvent event, IObservable source, Object data) {
				notifyListeners(event, data);
			}
		};
		s.addListener(eventDispatcher);
	}

	@Override
	public IElementType getType() {
		return s.getType();
	}

	@Override
	public ISQLScript appendSQL(String appendedSQL) {
		return s.appendSQL(appendedSQL);
	}

	@Override
	public ISQLScript appendSQL(char c) {
		return s.appendSQL(c);
	}

	@Override
	public ISQLScript appendSQL(BigDecimal d) {
		return s.appendSQL(d);
	}

	@Override
	public ISQLScript appendSQL(Long l) {
		return s.appendSQL(l);
	}

	@Override
	public ISQLScript appendSQL(int i) {
		return s.appendSQL(i);
	}

	@Override
	public void appendScript(ISQLScript script) {
		s.appendScript(script);
	}

	@Override
	public String getDirectory() {
		return s.getDirectory();
	}

	@Override
	public String getFilename() {
		return s.getFilename();
	}

	@Override
	public String getSql() {
		return s.getSql();
	}

	@Override
	public ScriptType getScriptType() {
		return s.getScriptType();
	}

	@Override
	public boolean isExternal() {
		return s.isExternal();
	}

	@Override
	public void setDirectory(String directory) {
		s.setDirectory(directory);
	}

	@Override
	public void setExternal(boolean isExternal) {
		s.setExternal(isExternal);
	}

	@Override
	public void setSql(String sql) {
		s.setSql(sql);
	}

	@Override
	public void setScriptType(ScriptType scriptType) {
		s.setScriptType(scriptType);
	}

	@Override
	public String getName() {
		return s.getName();
	}

	@Override
	public void setName(String name) {
		s.setName(name);
	}

	@Override
	public String getAbsolutePathname() {
		return s.getAbsolutePathname();
	}

	protected void setSqlScript(ISQLScript script) {
		if (s != null) {
			s.removeListener(eventDispatcher);
		}
		this.s = script;
		s.addListener(eventDispatcher);
	}

	public ISQLScript getSqlScript() {
		return s;
	}

}
