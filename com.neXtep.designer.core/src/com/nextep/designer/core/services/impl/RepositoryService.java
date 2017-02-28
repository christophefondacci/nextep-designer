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
package com.nextep.designer.core.services.impl;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.osgi.service.prefs.BackingStoreException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.core.model.IRepositoryProperty;
import com.nextep.designer.core.model.impl.RepositoryProperty;
import com.nextep.designer.core.preferences.DesignerCoreConstants;
import com.nextep.designer.core.services.IRepositoryService;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class RepositoryService implements IRepositoryService {

	private static final Log LOGGER = LogFactory.getLog(RepositoryService.class);

	private IConnection repositoryConnection;
	private PBEParameterSpec pbeParamSpec;
	private PBEKeySpec pbeKeySpec;
	private SecretKey key;
	private static final String ALGORITHM = "PBEWithMD5AndDES"; //$NON-NLS-1$

	/**
	 * Encryption constants. Since we are an open-source project, security is broken since anyone
	 * could access this class to know the decoding arguments, passphrases and arguments.
	 */
	private final byte[] salt = { (byte) 0xc9, (byte) 0xa3, (byte) 0x21, (byte) 0x2d, (byte) 0x7e,
			(byte) 0x79, (byte) 0xee, (byte) 0x99 };
	private final int iterations = 27;
	private final String encryptionPassword = "nXp48sU-mfq9yu&-bgcf10"; //$NON-NLS-1$

	public RepositoryService() {
		pbeParamSpec = new PBEParameterSpec(salt, iterations);
		pbeKeySpec = new PBEKeySpec(encryptionPassword.toCharArray());
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
			key = factory.generateSecret(pbeKeySpec);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(CoreMessages.getString("repositoryService.encryptionNotFound"), e); //$NON-NLS-1$
		} catch (InvalidKeySpecException e) {
			LOGGER.error(CoreMessages.getString("repositoryService.encryptionKeyFail"), e); //$NON-NLS-1$
		}
	}

	/**
	 * @return a database connector initialized to connect to the current repository
	 */
	@Override
	public IDatabaseConnector getRepositoryConnector() {
		final IConnection conn = getRepositoryConnection();
		// Loading all views
		IDatabaseConnector dbConnector = CorePlugin.getConnectionService().getDatabaseConnector(
				conn);
		return dbConnector;
	}

	@Override
	public DBVendor getRepositoryVendor() {
		IEclipsePreferences store = new InstanceScope().getNode(CorePlugin.PLUGIN_ID);
		final String dbVendor = store.get(DesignerCoreConstants.REP_DB_VENDOR_PROPERTY, DBVendor
				.getDefaultVendor().name());
		return DBVendor.valueOf(dbVendor);
	}

	@Override
	public String decrytPassword(String encryptedPassword) {
		Cipher cipher = getCipher();
		if (cipher != null) {
			try {
				cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
				final byte[] bytesPassword = Base64.decode(encryptedPassword);
				final byte[] decryptedPassword = cipher.doFinal(bytesPassword);
				return new String(decryptedPassword);
			} catch (InvalidKeyException e) {
				throw new ErrorException(
						CoreMessages.getString("repositoryService.invalidEncryptionKey"), e); //$NON-NLS-1$
			} catch (IllegalBlockSizeException e) {
				throw new ErrorException(
						CoreMessages.getString("repositoryService.encodingProblem"), e); //$NON-NLS-1$
			} catch (BadPaddingException e) {
				throw new ErrorException(
						CoreMessages.getString("repositoryService.encodingProblem"), e); //$NON-NLS-1$
			} catch (InvalidAlgorithmParameterException e) {
				throw new ErrorException(
						CoreMessages.getString("repositoryService.encodingProblem"), e); //$NON-NLS-1$
			} catch (Base64DecodingException e) {
				throw new ErrorException(
						CoreMessages.getString("repositoryService.encodingProblem"), e); //$NON-NLS-1$
			}
		}
		return encryptedPassword;
	}

	@Override
	public String encryptPassword(String password) {
		Cipher cipher = getCipher();
		if (cipher != null) {
			try {
				cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
				final byte[] encryptedPassword = cipher.doFinal(password.getBytes());
				return Base64.encode(encryptedPassword);
			} catch (InvalidKeyException e) {
				throw new ErrorException(
						CoreMessages.getString("repositoryService.invalidEncryptionKey"), e); //$NON-NLS-1$
			} catch (IllegalBlockSizeException e) {
				throw new ErrorException(
						CoreMessages.getString("repositoryService.encodingProblem"), e); //$NON-NLS-1$
			} catch (BadPaddingException e) {
				throw new ErrorException(
						CoreMessages.getString("repositoryService.encodingProblem"), e); //$NON-NLS-1$
			} catch (InvalidAlgorithmParameterException e) {
				throw new ErrorException(
						CoreMessages.getString("repositoryService.encodingProblem"), e); //$NON-NLS-1$
			}
		}
		return password;
	}

	private Cipher getCipher() {
		try {
			return Cipher.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(CoreMessages.getString("repositoryService.encryptionMethodUnavailable"), e); //$NON-NLS-1$
		} catch (NoSuchPaddingException e) {
			LOGGER.error(
					CoreMessages.getString("repositoryService.encryptionPaddingUnavailable"), e); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public IConnection getRepositoryConnection() {
		final IEclipsePreferences store = new InstanceScope().getNode(CorePlugin.PLUGIN_ID);

		final String vendor = store.get(DesignerCoreConstants.REP_DB_VENDOR_PROPERTY, DBVendor
				.getDefaultVendor().name());
		final String login = store.get(DesignerCoreConstants.REP_USER_PROPERTY, ""); //$NON-NLS-1$
		final String server = store.get(DesignerCoreConstants.REP_SERVER_PROPERTY, ""); //$NON-NLS-1$
		final String schema = store.get(DesignerCoreConstants.REP_SCHEMA_PROPERTY, ""); //$NON-NLS-1$
		final String port = store.get(DesignerCoreConstants.REP_PORT_PROPERTY, ""); //$NON-NLS-1$
		final String instance = store.get(DesignerCoreConstants.REP_INSTANCE_PROPERTY, ""); //$NON-NLS-1$
		final String database = store.get(DesignerCoreConstants.REP_DATABASE_PROPERTY, ""); //$NON-NLS-1$
		final String serviceName = store.get(DesignerCoreConstants.REP_TNS_PROPERTY, ""); //$NON-NLS-1$
		final boolean savedPassword = store.getBoolean(
				DesignerCoreConstants.REP_PASSWORD_SAVED_PROPERTY, true);
		String password = store.get(DesignerCoreConstants.REP_PASSWORD_PROPERTY, ""); //$NON-NLS-1$
		if (savedPassword) {
			try {
				password = decrytPassword(password);
			} catch (Exception e) {
				// We might fall here when trying to decrypt a clear password (from a previous
				// release)
				// => We consider it is empty so that the connection will fail and will be handled
				// properly
				password = ""; //$NON-NLS-1$
				LOGGER.error("Unable to decrypt password: password reset", e);
			}
		}
		final boolean isSso = store.getBoolean(DesignerCoreConstants.REP_SSO_PROPERTY, false);

		/*
		 * No longer using equinox security here as it contains too many bugs when used in a Splash
		 * screen context (i.e. before workbench is opened). Uncomment this once equinox security
		 * will become safe.
		 */

		// final ISecurePreferences root = SecurePreferencesFactory.getDefault();
		//		final ISecurePreferences connectionNode = root.node("repository/connection"); //$NON-NLS-1$
		// String password = null;
		// try {
		//			password = connectionNode.get(DesignerCoreConstants.REP_PASSWORD_PROPERTY, ""); //$NON-NLS-1$
		// } catch (StorageException e) {
		// throw new ErrorException(CoreMessages
		//					.getString("connection.editor.passwordStorageError"), e); //$NON-NLS-1$
		// }

		if (repositoryConnection == null) {
			repositoryConnection = CorePlugin.getTypedObjectFactory().create(IConnection.class);
		}
		if (vendor != null && !"".equals(vendor.trim())) { //$NON-NLS-1$
			repositoryConnection.setDBVendor(DBVendor.valueOf(vendor));
		}
		repositoryConnection.setSsoAuthentication(isSso);
		repositoryConnection.setLogin((isSso ? "" : login)); //$NON-NLS-1$
		repositoryConnection.setPasswordSaved(savedPassword & !isSso);
		if (savedPassword & !isSso) {
			repositoryConnection.setPassword(password);
		}
		repositoryConnection.setSchema(schema);
		repositoryConnection.setServerIP(server);
		repositoryConnection.setServerPort(port);
		repositoryConnection.setInstance(instance);
		repositoryConnection.setDatabase(database);
		repositoryConnection.setTnsAlias(serviceName);

		return repositoryConnection;
	}

	@Override
	public void setRepositoryConnection(IConnection conn) {
		final IEclipsePreferences store = new InstanceScope().getNode(CorePlugin.PLUGIN_ID);

		store.put(DesignerCoreConstants.REP_SSO_PROPERTY,
				String.valueOf(conn.isSsoAuthentication()));
		store.put(DesignerCoreConstants.REP_DB_VENDOR_PROPERTY, conn.getDBVendor().name());
		store.put(DesignerCoreConstants.REP_USER_PROPERTY, conn.getLogin());
		store.put(DesignerCoreConstants.REP_PASSWORD_SAVED_PROPERTY,
				String.valueOf(conn.isPasswordSaved()));
		store.put(DesignerCoreConstants.REP_SERVER_PROPERTY, conn.getServerIP());
		store.put(DesignerCoreConstants.REP_PORT_PROPERTY, conn.getServerPort());
		store.put(DesignerCoreConstants.REP_INSTANCE_PROPERTY, notNull(conn.getInstance()));
		store.put(DesignerCoreConstants.REP_DATABASE_PROPERTY, conn.getDatabase());
		store.put(DesignerCoreConstants.REP_TNS_PROPERTY, notNull(conn.getTnsAlias()));
		store.put(DesignerCoreConstants.REP_SCHEMA_PROPERTY, notNull(conn.getSchema()));
		store.put(DesignerCoreConstants.REP_PASSWORD_PROPERTY,
				conn.isPasswordSaved() ? encryptPassword(notNull(conn.getPassword())) : ""); //$NON-NLS-1$

		try {
			store.flush();
		} catch (BackingStoreException e) {
			throw new ErrorException(e);
		}

		/*
		 * No longer using equinox security here as it contains too many bugs when used in a Splash
		 * screen context (i.e. before workbench is opened). Uncomment this once equinox security
		 * will become safe.
		 */

		// // Always saving as even when isPasswordSaved() returns false we should
		// // blank the password in our secured store
		// final ISecurePreferences root = SecurePreferencesFactory.getDefault();
		//		final ISecurePreferences connectionNode = root.node("repository/connection"); //$NON-NLS-1$
		// try {
		// connectionNode.put(DesignerCoreConstants.REP_PASSWORD_PROPERTY,
		//					conn.isPasswordSaved() ? conn.getPassword() : "", true); //$NON-NLS-1$
		// root.flush();
		// } catch (StorageException e) {
		// throw new ErrorException(CoreMessages
		//					.getString("connection.editor.passwordStorageError"), e); //$NON-NLS-1$
		// } catch (IOException e) {
		// throw new ErrorException(CoreMessages
		//					.getString("connection.editor.passwordStorageError"), e); //$NON-NLS-1$
		// } catch (RuntimeException e) {
		// throw new ErrorException(CoreMessages
		//					.getString("connection.editor.passwordStorageError"), e); //$NON-NLS-1$
		// }
		this.repositoryConnection = conn;
	}

	@Override
	public String getProperty(String name) {
		IRepositoryProperty p = getRepositoryProperty(name);
		if (p != null) {
			return p.getValue();
		} else {
			return null;
		}
	}

	private IRepositoryProperty getRepositoryProperty(String name) {
		Session s = HibernateUtil.getInstance().getSandBoxSession();
		s.clear();
		Query q = s.createQuery("from RepositoryProperty where name=:name").setString("name", name); //$NON-NLS-1$ //$NON-NLS-2$
		return (IRepositoryProperty) q.uniqueResult();
	}

	@Override
	public void setProperty(String name, String value) {
		IRepositoryProperty property = getRepositoryProperty(name);
		if (property == null) {
			property = new RepositoryProperty(name, value);
		}
		property.setValue(value);
		CorePlugin.getIdentifiableDao().save(property, false,
				HibernateUtil.getInstance().getSandBoxSession(), true);
	}

	private String notNull(String s) {
		return s == null ? "" : s; //$NON-NLS-1$
	}

}
