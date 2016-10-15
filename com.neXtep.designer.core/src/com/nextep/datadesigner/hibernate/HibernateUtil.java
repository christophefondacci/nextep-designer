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
package com.nextep.datadesigner.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.def.DefaultPostLoadEventListener;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.services.IRepositoryService;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class HibernateUtil {

	private static final Log LOGGER = LogFactory.getLog(HibernateUtil.class);
	private static final String EXTENSION_ID = "com.neXtep.designer.core.persistenceFile"; //$NON-NLS-1$
	public static final List<Session> allSessions = new ArrayList<Session>();

	public static ThreadLocal<Session> sandBoxSession = new ThreadLocal<Session>();

	private SessionFactory sessionFactory;
	public Session mainSession = null;
	private Configuration hibernateConfig;
	/** Monitor to use for reporting load progress */
	private static IProgressMonitor monitor;
	private static HibernateUtil instance = null;
	/** This boolean indicates we are doing a refresh */
	private boolean isRefreshing = false;

	private HibernateUtil() {
		try {
			if (monitor != null) {
				monitor.setTaskName(CoreMessages.getString("hibernateInit")); //$NON-NLS-1$
			} else {
				LOGGER.info(CoreMessages.getString("hibernateInit")); //$NON-NLS-1$
			}
			final IRepositoryService repositoryService = CorePlugin.getRepositoryService();
			final IConnection conn = repositoryService.getRepositoryConnection();
			final String user = conn.getLogin();
			final String password = conn.getPassword();

			// Creating SessionFactory from hibernate.cfg.xml
			Configuration conf = new Configuration();
			addDynamicHibernateMappings(conf);
			hibernateConfig = conf.configure();
			final Properties p = hibernateConfig.getProperties();
			final DBVendor repositoryVendor = conn.getDBVendor();

			// Building connector's information
			final IDatabaseConnector connector = repositoryService.getRepositoryConnector();
			final String connectionUrl = connector.getConnectionURL(conn);
			final String driverClass = connector.getJDBCDriverClassName();

			// Setting hibernate properties
			p.setProperty("hibernate.connection.url", connectionUrl); //$//$NON-NLS-1$
			p.setProperty("hibernate.connection.driver_class", driverClass); //$NON-NLS-1$

			if (!conn.isSsoAuthentication()) {
				p.setProperty("hibernate.connection.username", user); //$NON-NLS-1$
				p.setProperty("hibernate.connection.password", password); //$NON-NLS-1$
			}

			// FIXME [BGA] Add schema information or find a way to make Hibernate call a
			// doPostConnectionSettings method

			switch (repositoryVendor) {
			case ORACLE:
				p.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle9iDialect"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case MYSQL:
				p.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case POSTGRE:
				p.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case DB2:
				p.setProperty("hibernate.dialect", "org.hibernate.dialect.DB2Dialect"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case MSSQL:
				p.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect"); //$//$NON-NLS-1$ //$//$NON-NLS-2$
				break;
			}
			hibernateConfig.setProperties(p);
			// Adding listeners
			addConfigurationListeners(hibernateConfig);

			sessionFactory = hibernateConfig.buildSessionFactory();
			// Main thread sessions
			sandBoxSession.set(sessionFactory.openSession());
			mainSession = sessionFactory.openSession();
			allSessions.add(sandBoxSession.get());
			allSessions.add(mainSession);
			if (monitor != null) {
				monitor.setTaskName(CoreMessages.getString("hibernateInitEnd")); //$NON-NLS-1$
			}
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println(CoreMessages.getString("hibernate.session.creationFailed") + ex); //$NON-NLS-1$
			ex.printStackTrace();
			throw new ExceptionInInitializerError(ex);
		}
	}

	private void addDynamicHibernateMappings(Configuration c) {
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(EXTENSION_ID,
				"file", "*"); //$NON-NLS-1$//$NON-NLS-2$
		for (IConfigurationElement elt : elts) {
			String mappingFile = elt.getAttribute("file"); //$NON-NLS-1$
			// We need to check this for a difference between debug and runtime mode :
			// By default eclipse's resource picker from the extension definition appends a "src/"
			// prefix to the resource location. While it works fine in debug, "src" is not an
			// existing runtime folder and cannot be resolved at runtime.
			if (mappingFile != null && mappingFile.startsWith("src/")) { //$NON-NLS-1$
				mappingFile = mappingFile.substring(4);
			}
			c.addResource(mappingFile);
		}
	}

	/**
	 * Adds the appropriate hibernate listeners for nextep
	 * 
	 * @param hibernateConfig the hibernate configuration
	 */
	private void addConfigurationListeners(Configuration hibernateConfig) {
		// Following listeners are CRITIC!
		// They are required to maintain proper reference management (upon which the whole
		// environment is based).
		// Basics are :
		// - A reference loaded from the main session (UI thread no sandbox) is NON VOLATILE
		// - A reference loaded from any other session is VOLATILE
		// AND
		// - A referenceable (not a reference!) inserted or saved in the main session becomes
		// NON-VOLATILE and gets re-referenced
		// =========================
		// References need to always have ID (thus needs to be saved) for correct hash code of every
		// object in
		// the workspace.

		// Here is the loading part
		PostLoadEventListener[] stack = { new PostLoadEventListener() {

			private static final long serialVersionUID = 1L;
			int counter = 0;

			@Override
			public void onPostLoad(PostLoadEvent event) {
				if (monitor != null) {
					if (counter++ % 15 == 0)
						monitor.worked(15);
				}
				Object o = event.getEntity();
				if (o instanceof IReferenceable) {
					final IReferenceable ref = (IReferenceable) o;
					if (!(o instanceof IReference)) {
						// If the load was generated by our sandbox session we reference a volatile
						// instance
						if (event.getSession() == sandBoxSession.get()) {
							LOGGER.trace("Referencing VOLATILE " + o.getClass().getName() //$NON-NLS-1$
									+ " with reference: " + ref.getReference()); //$NON-NLS-1$
							CorePlugin.getService(IReferenceManager.class).volatileReference(
									ref.getReference(), ref, event.getSession());
						} else {
							LOGGER.trace("Referencing " + o.getClass().getName() + " with reference: " //$NON-NLS-1$ //$NON-NLS-2$
									+ ref.getReference());
							// If we are loading in the main session we have a non volatile
							// reference
							if (ref.getReference() != null) {
								ref.getReference().setVolatile(false);
							}
							CorePlugin.getService(IReferenceManager.class).reference(
									ref.getReference(), ref, isRefreshing);
						}
					} else {
						// Loading a reference from main sessions => Non volatile reference
						if (event.getSession() != sandBoxSession.get()) {
							((IReference) ref).setVolatile(false);
						} else {
							((IReference) ref).setVolatile(true);
						}
					}
				}
			}
		}, new DefaultPostLoadEventListener() };
		hibernateConfig.getEventListeners().setPostLoadEventListeners(stack);

		PostInsertEventListener[] insertStack = { new PostInsertEventListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onPostInsert(PostInsertEvent event) {
				Object o = event.getEntity();
				if (o instanceof IReferenceable && event.getSession() != sandBoxSession.get()
						&& !(o instanceof IReference)) {
					final IReference r = ((IReferenceable) o).getReference();
					if (r != null) {
						r.setVolatile(false);
						CorePlugin.getService(IReferenceManager.class).reference(r,
								(IReferenceable) o);
					}
				}
			}
		} };
		hibernateConfig.getEventListeners().setPostInsertEventListeners(insertStack);
	}

	public static HibernateUtil getInstance() {
		if (instance == null) {
			instance = new HibernateUtil();
		}
		return instance;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @return the current session (thread local)
	 */
	public Session getSession() {
		return mainSession;
	}

	/**
	 * Closes and reopens the current session. Implemented for use before a repository merge
	 * operation where some same objects may be loaded in a sandbox and default session, thus
	 * creating some hibernate exceptions.
	 * 
	 * @return the reopened session
	 */
	public Session reloadSession() {
		Session s = mainSession;
		if (s != null) {
			s.flush();
			s.close();
			allSessions.remove(s);
		}
		s = sessionFactory.openSession();
		mainSession = s;
		allSessions.add(s);
		return s;
	}

	/**
	 * This method handles the case when we loose connection with the repository database. <br>
	 * <b>MUST BE CALLED WITHIN THE UI THREAD</b>
	 */
	public void reconnectAll() {
		// Closing everything we can
		for (Session s : new ArrayList<Session>(allSessions)) {
			try {
				s.close();
			} catch (RuntimeException e) {
				// It may always fail, but there is not much we could do
				// Even logging would be useless and would complexify our logs...
			}
			allSessions.remove(s);
		}
		// Don't do that at home...
		// We don't want any old session to be reused since we may have been
		// disconnected from the network. This is to avoid having the same critical
		// network message for every repository connection we are using
		sandBoxSession = new ThreadLocal<Session>();
		// Closing our session factory
		CorePlugin.getService(IReferenceManager.class).flush();
		sessionFactory.close();
		// addConfigurationListeners(hibernateConfig);
		sessionFactory = hibernateConfig.buildSessionFactory();
		// Reconnecting our main session
		Session s = sessionFactory.openSession();
		mainSession = s;
		allSessions.add(s);
		Session sandbox = sessionFactory.openSession();
		sandBoxSession.set(sandbox);
		allSessions.add(sandbox);
	}

	/**
	 * @return a sandbox session for temporary trashable operations.
	 */
	public Session getSandBoxSession() {
		Session s = sandBoxSession.get();
		// if(s!=null && !s.isOpen()) {
		// allSessions.remove(s);
		// }
		if (s == null) { // || !s.isOpen()) {
			s = sessionFactory.openSession();
			sandBoxSession.set(s);
			allSessions.add(s);
		}
		return s;
	}

	public void clearAllSessions() {
		for (Session s : new ArrayList<Session>(allSessions)) {
			s.clear();
		}
		CorePlugin.getIdentifiableDao().clearException();
	}

	/**
	 * Activates / deactivates refresh mode. The refresh mode will update references instead of
	 * creating duplicates
	 * 
	 * @param refreshing new refresh state
	 */
	public void setRefreshing(boolean refreshing) {
		this.isRefreshing = refreshing;
	}

	/**
	 * Attaches a monitor to the hibernate utility so that progress could be reported on load
	 * operations.
	 * 
	 * @param monitor the monitor to report progress on or <code>null</code> to remove attached
	 *        monitor
	 */
	public static void setMonitor(IProgressMonitor monitor) {
		if (HibernateUtil.monitor == null || monitor == null) {
			HibernateUtil.monitor = monitor;
		}
	}

}
