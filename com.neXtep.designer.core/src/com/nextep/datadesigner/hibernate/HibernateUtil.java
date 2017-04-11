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
import org.hibernate.HibernateException;
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
	private static final List<Session> ALL_SESSIONS = new ArrayList<Session>();
	private static final IReferenceManager REFERENCE_MANAGER = CorePlugin
			.getService(IReferenceManager.class);

	private Configuration hibernateConfig;
	private SessionFactory sessionFactory;

	private static ThreadLocal<Session> sandBoxSession = new ThreadLocal<Session>();
	public Session mainSession = null;

	/** This boolean indicates we are doing a refresh */
	private boolean isRefreshing = false;

	/** Monitor to use for reporting load progress */
	private static IProgressMonitor monitor;

	private HibernateUtil() {
		initializeSessions();
	}

	private static class HibernateUtilHolder {

		private static final HibernateUtil INSTANCE = new HibernateUtil();
	}

	public static HibernateUtil getInstance() {
		return HibernateUtilHolder.INSTANCE;
	}

	private void initializeSessions() {
		try {
			setMonitorTaskName(CoreMessages.getString("hibernateInit")); //$NON-NLS-1$

			final IRepositoryService repositoryService = CorePlugin.getRepositoryService();

			// Creating SessionFactory from hibernate.cfg.xml
			Configuration conf = new Configuration();
			addDynamicHibernateMappings(conf);
			hibernateConfig = conf.configure();
			final Properties p = hibernateConfig.getProperties();

			// Setting Hibernate properties
			final IConnection conn = repositoryService.getRepositoryConnection();
			final IDatabaseConnector<?> connector = repositoryService.getRepositoryConnector();
			p.setProperty("hibernate.connection.url", connector.getConnectionURL(conn)); //$NON-NLS-1$
			p.setProperty("hibernate.connection.driver_class", connector.getJDBCDriverClassName()); //$NON-NLS-1$

			if (!conn.isSsoAuthentication()) {
				p.setProperty("hibernate.connection.username", conn.getLogin()); //$NON-NLS-1$
				p.setProperty("hibernate.connection.password", conn.getPassword()); //$NON-NLS-1$
			}

			final String schema = conn.getSchema();
			if (schema != null && !"".equals(schema)) { //$NON-NLS-1$
				p.setProperty("hibernate.default_schema", conn.getSchema()); //$NON-NLS-1$
			}

			final DBVendor dbVendor = conn.getDBVendor();
			switch (dbVendor) {
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
				p.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
			hibernateConfig.setProperties(p);

			// Adding listeners
			addConfigurationListeners(hibernateConfig);
			initSessionFactory();

			// Opening main and sandbox sessions
			mainSession = openCachedSession();
			openSandBoxSession();

			setMonitorTaskName(CoreMessages.getString("hibernateInitEnd")); //$NON-NLS-1$
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println(CoreMessages.getString("hibernate.session.creationFailed") + ex); //$NON-NLS-1$
			ex.printStackTrace();
			throw new ExceptionInInitializerError(ex);
		}
	}

	private void addDynamicHibernateMappings(Configuration c) {
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(EXTENSION_ID,
				"file", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		for (IConfigurationElement elt : elts) {
			String mappingFile = elt.getAttribute("file"); //$NON-NLS-1$
			/*
			 * We need to check this for a difference between debug and runtime mode : By default
			 * eclipse's resource picker from the extension definition appends a "src/" prefix to
			 * the resource location. While it works fine in debug, "src" is not an existing runtime
			 * folder and cannot be resolved at runtime.
			 */
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
		// object in the workspace.

		// Here is the loading part
		PostLoadEventListener[] loadEventStack = { new PostLoadEventListener() {

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
						IReference oRef = ref.getReference();

						// If the load was generated by our sandbox session we reference a volatile
						// instance
						if (event.getSession() == sandBoxSession.get()) {
							LOGGER.trace("Referencing VOLATILE " + o.getClass().getName() //$NON-NLS-1$
									+ " with reference: " + oRef); //$NON-NLS-1$
							REFERENCE_MANAGER.volatileReference(oRef, ref, event.getSession());
						} else {
							LOGGER.trace("Referencing " + o.getClass().getName() //$NON-NLS-1$
									+ " with reference: " + oRef); //$NON-NLS-1$
							// If we are loading in the main session we have a non volatile
							// reference
							if (oRef != null) {
								oRef.setVolatile(false);
							}
							REFERENCE_MANAGER.reference(oRef, ref, isRefreshing);
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
		hibernateConfig.getEventListeners().setPostLoadEventListeners(loadEventStack);

		PostInsertEventListener[] insertEventStack = { new PostInsertEventListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onPostInsert(PostInsertEvent event) {
				Object o = event.getEntity();
				if (o instanceof IReferenceable && event.getSession() != sandBoxSession.get()
						&& !(o instanceof IReference)) {
					final IReference r = ((IReferenceable) o).getReference();
					if (r != null) {
						r.setVolatile(false);
						REFERENCE_MANAGER.reference(r, (IReferenceable) o);
					}
				}
			}
		} };
		hibernateConfig.getEventListeners().setPostInsertEventListeners(insertEventStack);
	}

	private void initSessionFactory() {
		if (sessionFactory != null) {
			try {
				sessionFactory.close();
			} catch (HibernateException he) {
				LOGGER.warn("Problem occured while closing SessionFactory " + he.getMessage()); //$NON-NLS-1$
			}

			// Flushing all references from the Reference Manager
			REFERENCE_MANAGER.flush();
		}

		sessionFactory = hibernateConfig.buildSessionFactory();
	}

	/**
	 * Open a session and add the newly created session to the sessions list managed by this
	 * HibernateUtil.
	 * 
	 * @return a new session
	 */
	private Session openCachedSession() {
		Session s = sessionFactory.openSession();
		ALL_SESSIONS.add(s);
		return s;
	}

	/**
	 * Open a new sandbox session.
	 * 
	 * @return a new session
	 */
	private Session openSandBoxSession() {
		Session sbs = openCachedSession();
		sandBoxSession.set(sbs);
		return sbs;
	}

	/**
	 * @return the current main session
	 */
	public Session getSession() {
		return mainSession;
	}

	/**
	 * @return a sandbox session (thread local) for temporary trashable operations
	 */
	public Session getSandBoxSession() {
		Session sbs = sandBoxSession.get();
		if (sbs == null) {
			sbs = openSandBoxSession();
		}
		return sbs;
	}

	/**
	 * This method handles the case when we loose connection with the repository database. <br>
	 * <b>MUST BE CALLED WITHIN THE UI THREAD</b>
	 */
	public void reconnectAll() {
		// Closing everything we can
		for (Session s : new ArrayList<Session>(ALL_SESSIONS)) {
			try {
				s.close();
			} catch (RuntimeException e) {
				// It may always fail, but there is not much we could do
				// Even logging would be useless and would complexify our logs...
			}
			ALL_SESSIONS.remove(s);
		}

		// Don't do that at home...
		/*
		 * We don't want any old session to be reused since we may have been disconnected from the
		 * network. This is to avoid having the same critical network message for every repository
		 * connection we are using.
		 */
		sandBoxSession = new ThreadLocal<Session>();

		// Resetting our SessionFactory
		initSessionFactory();

		// Reconnecting our main and sandbox sessions
		mainSession = openCachedSession();
		openSandBoxSession();
	}

	public void clearAllSessions() {
		for (Session s : new ArrayList<Session>(ALL_SESSIONS)) {
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

	private void setMonitorTaskName(String name) {
		if (monitor != null) {
			monitor.setTaskName(name);
		} else {
			LOGGER.info(name);
		}
	}

}
