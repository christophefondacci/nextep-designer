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
package com.nextep.designer.core.dao.impl;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.OutOfDateObjectException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.impl.RepositorySynchronizer;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.dao.IIdentifiableDAO;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IReferenceManager;

/**
 * A generic Data Access Object able to provide common features to communicate with the db.
 * 
 * @author Christophe Fondacci
 */
public class IdentifiableDAO implements IIdentifiableDAO {

	private static final Log log = LogFactory.getLog(IdentifiableDAO.class);
	private boolean persisting = false;
	private boolean loading = false;
	private HibernateUtil hUtil;
	private HibernateException lastException;

	public IdentifiableDAO() {
		hUtil = HibernateUtil.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * @see com.nextep.datadesigner.dao.IGenericDAO#getIdMap(java.lang.Class)
	 */
	public Map<UID, IdentifiedObject> getIdMap(Class<?> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Lists all elements of the given class from database using the default Hibernate session.
	 * 
	 * @param clazz class of objects to load
	 * @return a list of all elements found
	 */
	public <T> List<? extends T> loadAll(Class<T> clazz) {
		return loadAll(clazz, hUtil.getSession());
	}

	/**
	 * Loads all elements of the specified class using the provided session
	 * 
	 * @param clazz class of the objects to load from db
	 * @param session Hibernate session to use
	 * @return a list of all objects from this type
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<? extends T> loadAll(Class<T> clazz, Session session) {
		checkExceptions();
		log.debug("Loading all class '" + clazz.getName() + "'");
		if (session == hUtil.getSandBoxSession()) {
			log.debug("USING SANDBOX SESSION");
			session.clear();
			CorePlugin.getService(IReferenceManager.class).flushVolatiles(session);
		}
		Transaction t = session.beginTransaction();
		try {
			return session.createQuery("from " + clazz.getName()).list();
		} finally {
			// This section might be removable since it should only happen when
			// loosing connection...
			if (!t.wasCommitted()) {
				try {
					t.rollback();
				} catch (RuntimeException e) {
					// We don't want a "finally" block to raise any exception which
					// would mask any original exception, we log for debug
					log.error("Error in finally block", e);
				}
			}
		}
	}

	public void delete(IdentifiedObject object) {
		delete(object, hUtil.getSession());
	}

	@Override
	public void delete(IdentifiedObject object, Session session) {
		checkExceptions();
		// Looking for any opened editor and close them
		// Designer.getInstance().getGUI().unplugModel(object);
		// Removing object
		log.debug("Deleting class '" + object.getClass().getName() + "'");
		Transaction t = session.beginTransaction();
		try {
			session.delete(object);
			t.commit();
		} finally {
			// This section might be removable since it should only happen when
			// loosing connection...
			if (!t.wasCommitted()) {
				try {
					t.rollback();
				} catch (RuntimeException e) {
					// We don't want a "finally" block to raise any exception which
					// would mask any original exception, we log for debug
					log.error("Error in finally block", e);
				}
			}
		}
	}

	/**
	 * Displays 4 call stack elements when debugging
	 */
	private void debugStack() {
		if (Designer.isDebugging()) {
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			int i = 0;
			for (StackTraceElement e : stack) {
				log.debug("  " + e.toString());
				if (i++ >= 10) {
					break;
				}
			}
		}
	}

	@Override
	public IdentifiedObject load(Class<?> clazz, UID id, Session session, boolean clearSession) {
		checkExceptions();
		log.debug("Loading class '" + clazz.getName() + "' for ID " + id);
		debugStack();
		if (session == hUtil.getSandBoxSession()) {
			log.debug("USING SANDBOX SESSION");
			if (clearSession) {
				session.clear();
				// Flushing any pre-existing volatile reference
				CorePlugin.getService(IReferenceManager.class).flushVolatiles(session);
			}
		} else {
			if (clearSession) {
				session.clear();
			}
		}
		loading = true;

		Transaction t = session.beginTransaction();
		try {
			// Trying to retrieve in-session object
			IdentifiedObject object = (IdentifiedObject) session.load(clazz, id.rawId());
			// IdentifiedObject object2 = (IdentifiedObject)session.merge(object);
			t.commit();

			return object;
		} catch (HibernateException e) {
			lastException = e;
			throw e;
		} finally {
			loading = false;
			// This section might be removable since it should only happen when
			// loosing connection...
			if (!t.wasCommitted()) {
				try {
					t.rollback();
				} catch (RuntimeException e) {
					// We don't want a "finally" block to raise any exception which
					// would mask any original exception, we log for debug
					log.error("Error in finally block", e);
				}
			}
		}
	}

	@Override
	public List<?> loadForeignKey(Class<?> clazz, UID id, String fkName) {
		return loadForeignKey(clazz, id, fkName, true);
	}

	@Override
	public List<?> loadForeignKey(Class<?> clazz, UID id, String fkName, boolean sandBox) {
		return loadForeignKey(clazz, id, fkName, sandBox, sandBox);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<?> loadForeignKey(Class<?> clazz, UID id, String fkName, boolean sandBox,
			boolean clearSession) {
		checkExceptions();
		log.debug("Loading class '" + clazz.getName() + "' by foreign key <" + fkName + "> ID "
				+ id);
		Session session = null;
		if (sandBox) {
			session = hUtil.getSandBoxSession();
			log.debug("USING SANDBOX SESSION");
			if (clearSession) {
				session.clear();
				CorePlugin.getService(IReferenceManager.class).flushVolatiles(session);
			}
		} else {
			session = hUtil.getSession();
		}
		loading = true;
		debugStack();
		Transaction t = session.beginTransaction();
		try {
			// Trying to retrieve in-session object
			List l = session
					.createQuery("from " + clazz.getName() + " as cl where cl." + fkName + "=:fkID")
					.setLong("fkID", id != null ? id.rawId() : null).list();
			// IdentifiedObject object2 = (IdentifiedObject)session.merge(object);
			t.commit();
			return l;
		} catch (HibernateException e) {
			log.error("Hibernate exception", e);
			lastException = e;
			throw e;
		} finally {
			loading = false;
			// This section might be removable since it should only happen when
			// loosing connection...
			if (!t.wasCommitted()) {
				try {
					t.rollback();
				} catch (RuntimeException e) {
					// We don't want a "finally" block to raise any exception which
					// would mask any original exception, we log for debug
					log.error("Error in finally block", e);
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<?> loadWhere(Class<?> clazz, String columnName, String columnValue) {
		checkExceptions();
		log.debug("Loading class '" + clazz.getName() + "' WHERE " + columnName + "='"
				+ columnValue + "'");
		Session session = hUtil.getSandBoxSession();
		log.debug("USING SANDBOX SESSION");
		loading = true;
		session.clear();
		Transaction t = session.beginTransaction();
		try {
			// Trying to retrieve in-session object
			List l = session
					.createQuery(
							"from " + clazz.getName() + " as cl where cl." + columnName
									+ "=:columnValue").setString("columnValue", columnValue).list();
			// IdentifiedObject object2 = (IdentifiedObject)session.merge(object);
			t.commit();
			return l;
		} finally {
			loading = false;
			// This section might be removable since it should only happen when
			// loosing connection...
			if (!t.wasCommitted()) {
				try {
					t.rollback();
				} catch (RuntimeException e) {
					// We don't want a "finally" block to raise any exception which
					// would mask any original exception, we log for debug
					log.error("Error in finally block", e);
				}
			}
		}
	}

	public IdentifiedObject load(Class<?> clazz, UID id) {
		Session session = hUtil.getSession(); // getSessionFactory().getCurrentSession();
		return load(clazz, id, session, false);
	}

	@Override
	public void save(IdentifiedObject object, boolean forceSave) {
		save(object, forceSave, hUtil.getSession(), false);
	}

	/**
	 * Saves the specified object to the database.
	 * 
	 * @param object object to save
	 * @param forceSave
	 */
	@Override
	public void save(IdentifiedObject object, boolean forceSave, Session session,
			boolean clearSession) {
		checkExceptions();
		log.debug("Saving class '" + object.getClass().getName() + "' for ID " + object.getUID());
		// Cannot save a locked element, unless explicitely forcing this save
		if (object instanceof ILockable<?> && !forceSave) {
			if (((ILockable<?>) object).updatesLocked()) {
				log.debug("Skipped save on locked object : " + NameHelper.getQualifiedName(object));
				return;
			}
		}
		if ((!persisting && !loading) || forceSave) {
			persisting = true;
			debugStack();
			// Debug traces and session clear (only on sandbox as we cannot clear
			// the main session).
			if (session == hUtil.getSandBoxSession()) {
				log.debug("Save uses SANDBOX session");
				if (clearSession) {
					session.clear();
				}
			}
			// Ensuring we are working on a synchronized object
			try {
				RepositorySynchronizer.synchronize(object, session);
			} catch (OutOfDateObjectException e) {
				persisting = false;
				throw e;
			}
			Transaction t = session.beginTransaction();
			try {
				RepositorySynchronizer.upgrade(object);
				session.saveOrUpdate(object);
				t.commit();

			} catch (HibernateException e) {
				log.error("Hibernate exception occurred:", e);
				lastException = e;
				throw e;
			} finally {
				persisting = false;
				// // This section might be removable since it should only happen when
				// // loosing connection...
				// if(!t.wasCommitted()) {
				// try {
				// t.rollback();
				// } catch(RuntimeException e) {
				// // We don't want a "finally" block to raise any exception which
				// // would mask any original exception, we log for debug
				// log.error("Error in finally block",e);
				// }
				// }
			}
		} else {
			log.debug("Save collision, skipping class '" + object.getClass().getName()
					+ "' for ID " + object.getUID());
		}
	}

	public void save(IdentifiedObject object) {
		save(object, false);
	}

	@Override
	public boolean isPersisting() {
		return persisting;
	}

	@Override
	public void refresh(IdentifiedObject o) {
		try {
			HibernateUtil.getInstance().setRefreshing(true);
			HibernateUtil.getInstance().getSession().refresh(o);
		} finally {
			HibernateUtil.getInstance().setRefreshing(false);
		}
	}

	@Override
	public void clearException() {
		lastException = null;
		persisting = false;
		loading = false;
	}

	private void checkExceptions() {
		if (lastException != null) {
			throw new ErrorException(
					"The repository has been locked to prevent data corruptions because an exception was raised by"
							+ " the previous repository action. Please exit and restart designer or reload your view.",
					lastException);
		}
	}
}
