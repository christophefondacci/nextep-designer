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
/**
 *
 */
package com.nextep.designer.core.model.impl;

import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;

/**
 * Default implementation of the {@link IConnection} interface.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class Connection extends NamedObservable implements IConnection {

	private String login;
	private boolean isSso;
	private String password;
	private boolean isSaved;
	private String instance;
	private String database;
	private String serverIP;
	private String serverPort;
	private DBVendor vendor;
	private UID id;
	private String tnsAlias;
	private String schema;

	public Connection() {
	}

	@Override
	public String getLogin() {
		return login;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getInstance() {
		return instance;
	}

	@Override
	public String getDatabase() {
		return database;
	}

	@Override
	public String getServerIP() {
		return serverIP;
	}

	@Override
	public String getServerPort() {
		return serverPort;
	}

	@Override
	public void setLogin(String login) {
		final String old = this.login;
		this.login = login;
		notifyIfChanged(old, login, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setPassword(String password) {
		final String old = this.password;
		this.password = password;
		notifyIfChanged(old, password, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setInstance(String instance) {
		final String old = this.instance;
		this.instance = instance;
		notifyIfChanged(old, instance, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setDatabase(String database) {
		final String old = this.database;
		this.database = database;
		notifyIfChanged(old, database, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setServerIP(String serverIP) {
		final String old = this.serverIP;
		this.serverIP = serverIP;
		notifyIfChanged(old, serverIP, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public void setServerPort(String port) {
		final String old = this.serverPort;
		this.serverPort = port;
		notifyIfChanged(old, port, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(IConnection.TYPE_ID);
	}

	@Override
	public UID getUID() {
		return id;
	}

	@Override
	public void setUID(UID id) {
		this.id = id;
	}

	protected long getId() {
		if (id == null) {
			return 0L;
		}
		return id.rawId();
	}

	protected void setId(long id) {
		setUID(new UID(id));
	}

	@Override
	public DBVendor getDBVendor() {
		return vendor;
	}

	@Override
	public void setDBVendor(DBVendor vendor) {
		if (this.vendor != vendor) {
			this.vendor = vendor;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		super.checkConsistency();
		if (!isSsoAuthentication()) {
			if (isEmpty(getLogin())) {
				throw new InconsistentObjectException(
						CoreMessages.getString("consistency.connection.noLogin")); //$NON-NLS-1$
			}
		}
		if (isEmpty(getDatabase())) {
			throw new InconsistentObjectException(
					CoreMessages.getString("consistency.connection.noDatabase")); //$NON-NLS-1$
		}
		if (isEmpty(getServerIP())) {
			throw new InconsistentObjectException(
					CoreMessages.getString("consistency.connection.noServer")); //$NON-NLS-1$
		}
		if (isEmpty(getServerPort())) {
			throw new InconsistentObjectException(
					CoreMessages.getString("consistency.connection.noPort")); //$NON-NLS-1$
		}
	}

	private boolean isEmpty(String s) {
		return s == null || (s != null && "".equals(s.trim())); //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return (isSsoAuthentication() ? "[" + (getDBVendor() == DBVendor.MSSQL ? "Windows " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "SSO]" : getLogin()) //$NON-NLS-1$
				+ "@" + getDatabase() + (getServerIP() != null ? " (" + getServerIP() + ")" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Override
	public String getTnsAlias() {
		return tnsAlias;
	}

	@Override
	public void setTnsAlias(String tnsAlias) {
		this.tnsAlias = tnsAlias;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public String getSchema() {
		return schema;
	}

	@Override
	public void setSchema(String schema) {
		this.schema = schema;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public boolean isPasswordSaved() {
		return isSaved;
	}

	@Override
	public void setPasswordSaved(boolean saved) {
		boolean old = this.isSaved;
		this.isSaved = saved;
		notifyIfChanged(old, saved, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public boolean isSsoAuthentication() {
		return isSso;
	}

	@Override
	public void setSsoAuthentication(boolean sso) {
		boolean old = this.isSso;
		this.isSso = sso;
		notifyIfChanged(old, sso, ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public int hashCode() {
		// FIXME [BGA] Why this hash code has been set to 1 ?
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IConnection) {
			final IConnection other = (IConnection) obj;
			if (obj == this) {
				return true;
			}
			return notNull(getLogin()).equals(notNull(other.getLogin()))
					&& notNull(getInstance()).equals(notNull(other.getInstance()))
					&& notNull(getDatabase()).equals(notNull(other.getDatabase()))
					&& notNull(getSchema()).equals(notNull(other.getSchema()))
					&& notNull(getServerIP()).equals(notNull(other.getServerIP()))
					&& notNull(getServerPort()).equals(notNull(other.getServerPort()))
					&& (isSsoAuthentication() == other.isSsoAuthentication());
		}
		return false;
	}

	private String notNull(String s) {
		return s == null ? "" : s; //$NON-NLS-1$
	}

}
