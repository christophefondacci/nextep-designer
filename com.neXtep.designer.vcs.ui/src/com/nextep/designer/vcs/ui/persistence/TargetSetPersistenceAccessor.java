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
package com.nextep.designer.vcs.ui.persistence;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.osgi.service.prefs.BackingStoreException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IPersistenceAccessor;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class TargetSetPersistenceAccessor implements IPersistenceAccessor<ITargetSet> {

	private static final Log LOGGER = LogFactory.getLog(TargetSetPersistenceAccessor.class);

	private static final String KEY_LOGIN = "login"; //$NON-NLS-1$
	private static final String KEY_DATABASE = "db"; //$NON-NLS-1$
	private static final String KEY_INSTANCE = "instance"; //$NON-NLS-1$
	private static final String KEY_SCHEMA = "schema"; //$NON-NLS-1$
	private static final String KEY_SERVER = "server"; //$NON-NLS-1$
	private static final String KEY_PORT = "port"; //$NON-NLS-1$
	private static final String KEY_VENDOR = "vendor"; //$NON-NLS-1$
	private static final String KEY_TNSALIAS = "tnsalias"; //$NON-NLS-1$
	private static final String KEY_PASSWORD = "metadata"; //$NON-NLS-1$
	private static final String KEY_PASSWORD_SAVED = "savePassword"; //$NON-NLS-1$
	private static final String KEY_SSO = "ssoAuthenticate"; //$NON-NLS-1$

	@Override
	public void delete(ITargetSet element) {
		// Cannot delete a target set
	}

	@Override
	public boolean isHandledForLoad(IElementType typeToLoad, ITypedObject... parents) {
		return (parents != null && parents.length == 1 && parents[0] instanceof IWorkspace);
	}

	@Override
	public Collection<ITargetSet> load(IElementType typeToLoad, ITypedObject... parents) {
		return loadAll(typeToLoad);
	}

	@Override
	public Collection<ITargetSet> loadAll(IElementType typeToLoad) {
		final ITargetSet targetSet = getTargetSet(VCSPlugin.getViewService().getCurrentWorkspace());
		return Arrays.asList(targetSet);
	}

	@Override
	public void save(ITargetSet element) {
		saveTargetSet(element);
	}

	/**
	 * Saves the specified target set in the workspace preferences in a secured way. Any previous
	 * connection definition for the current view / user will be replaced by the one specified
	 * 
	 * @param set the {@link ITargetSet} to save
	 */
	private void saveTargetSet(ITargetSet set) {
		final XMLMemento m = XMLMemento.createWriteRoot("targetSet"); //$NON-NLS-1$
		final Collection<IConnection> connections = set.getConnections();
		for (IConnection c : connections) {
			saveState(c, m);
		}
		StringWriter writer = new StringWriter();
		try {
			m.save(writer);
		} catch (IOException e) {
			throw new ErrorException(VCSUIMessages.getString("helper.connection.saveFail"), e); //$NON-NLS-1$
		}
		IPreferenceStore store = VCSUIPlugin.getDefault().getPreferenceStore();
		store.putValue(getPreferenceKeyForTargetSet(VCSPlugin.getViewService()
				.getCurrentWorkspace()), writer.toString());
		try {
			new InstanceScope().getNode(VCSUIPlugin.PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			throw new ErrorException(e);
		}
	}

	/**
	 * Retrieves the target set for the current view / user from the workspace
	 * 
	 * @return the current target set containing all user connections
	 */
	private ITargetSet getTargetSet(IWorkspace view) {
		final ITargetSet set = CorePlugin.getTypedObjectFactory().create(ITargetSet.class);
		set.setView(view);
		IPreferenceStore store = VCSUIPlugin.getDefault().getPreferenceStore();
		String preference = store.getString(getPreferenceKeyForTargetSet(view));
		StringReader reader = new StringReader(preference);
		try {
			IMemento memento = XMLMemento.createReadRoot(reader);
			if (memento != null) {
				List<IConnection> connections = restoreState(memento);
				set.setConnections(connections);
			}
		} catch (WorkbenchException e) {
			LOGGER.warn(VCSUIMessages.getString("helper.connection.undefined")); //$NON-NLS-1$
		}
		return set;
	}

	private String getPreferenceKeyForTargetSet(IWorkspace view) {
		StringBuffer buf = new StringBuffer();
		IConnection repositoryConnection = CorePlugin.getRepositoryService()
				.getRepositoryConnection();
		if (repositoryConnection != null) {
			buf.append(repositoryConnection.getDatabase()
					+ "." + repositoryConnection.getServerIP() //$NON-NLS-1$
					+ "." + repositoryConnection.getLogin() + "."); ////$NON-NLS-1$ //$NON-NLS-2$ 
		}
		IRepositoryUser user = VersionHelper.getCurrentUser();
		if (user != null) {
			buf.append(user.getUID().toString() + "."); //$NON-NLS-1$
		}
		if (view != null) {
			buf.append(view.getUID().toString() + "."); //$NON-NLS-1$
		}
		buf.append("connections"); //$NON-NLS-1$
		return buf.toString();
	}

	private void saveState(IConnection c, IMemento m) {
		IMemento connectionRoot = m.createChild("connection"); //$NON-NLS-1$
		connectionRoot.putString(KEY_LOGIN, c.getLogin());
		connectionRoot.putString(KEY_INSTANCE, c.getInstance());
		connectionRoot.putString(KEY_DATABASE, c.getDatabase());
		connectionRoot.putString(KEY_SCHEMA, c.getSchema());
		connectionRoot.putString(KEY_SERVER, c.getServerIP());
		connectionRoot.putString(KEY_PORT, c.getServerPort());
		connectionRoot.putString(KEY_VENDOR, c.getDBVendor().name());
		connectionRoot.putString(KEY_TNSALIAS, c.getTnsAlias());
		connectionRoot.putString(KEY_PASSWORD_SAVED, String.valueOf(c.isPasswordSaved()));
		connectionRoot.putString(KEY_SSO, String.valueOf(c.isSsoAuthentication()));
		String encryptedPassword = ""; //$NON-NLS-1$
		if (c.isPasswordSaved()) {
			try {
				encryptedPassword = getRepositoryService().encryptPassword(c.getPassword());
			} catch (RuntimeException e) {
				LOGGER.error(VCSUIMessages.getString("helper.encryption.failure"), e); //$NON-NLS-1$
			}
		}
		connectionRoot.putString(KEY_PASSWORD, encryptedPassword);
	}

	private List<IConnection> restoreState(IMemento m) {
		final List<IConnection> connections = new ArrayList<IConnection>();
		for (IMemento connectionRoot : m.getChildren("connection")) { //$NON-NLS-1$
			IConnection conn = CorePlugin.getTypedObjectFactory().create(IConnection.class);
			conn.setLogin(connectionRoot.getString(KEY_LOGIN));
			conn.setInstance(connectionRoot.getString(KEY_INSTANCE));
			conn.setDatabase(connectionRoot.getString(KEY_DATABASE));
			conn.setSchema(connectionRoot.getString(KEY_SCHEMA));
			conn.setServerIP(connectionRoot.getString(KEY_SERVER));
			conn.setServerPort(connectionRoot.getString(KEY_PORT));
			conn.setDBVendor(DBVendor.valueOf(connectionRoot.getString(KEY_VENDOR)));
			conn.setTnsAlias(connectionRoot.getString(KEY_TNSALIAS));
			final String savedPwd = connectionRoot.getString(KEY_PASSWORD_SAVED);
			boolean savedPassword = false;
			if (savedPwd != null && !savedPwd.isEmpty()) {
				savedPassword = Boolean.parseBoolean(savedPwd);
			}
			conn.setPasswordSaved(savedPassword);
			String password = ""; //$NON-NLS-1$
			if (savedPassword) {
				String encryptedPassword = connectionRoot.getString(KEY_PASSWORD);
				if (encryptedPassword != null) {
					if (!"".equals(encryptedPassword.trim())) { //$NON-NLS-1$
						try {
							password = getRepositoryService().decrytPassword(encryptedPassword);
						} catch (Exception e) {
							LOGGER.error(VCSUIMessages.getString("helper.encryption.failure"), e); //$NON-NLS-1$
						}
					}
				} else {
					/*
					 * [BGA]: We set the saved password flag to false to handle the special case
					 * when the password has previously been saved with equinox security but is no
					 * more reachable due to migration from equinox security to our own persistence
					 * solution. Instead of trying to connect with an empty password and fail, the
					 * user will be prompted to enter the password again.
					 */
					conn.setPasswordSaved(false);
				}
			}
			conn.setPassword(password);
			final String ssoAuthentication = connectionRoot.getString(KEY_SSO);
			boolean isSso = false;
			if (ssoAuthentication != null && !ssoAuthentication.isEmpty()) {
				isSso = Boolean.parseBoolean(ssoAuthentication);
			}
			conn.setSsoAuthentication(isSso);
			connections.add(conn);
		}
		return connections;
	}

	/**
	 * For future dependency injection
	 * 
	 * @return the {@link IRepositoryService} implementation
	 */
	protected IRepositoryService getRepositoryService() {
		return CorePlugin.getRepositoryService();
	}

}
