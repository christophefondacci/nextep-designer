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
package com.nextep.datadesigner.impl;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ISynchronizable;
import com.nextep.datadesigner.model.SynchStatus;
import com.nextep.designer.core.helpers.NameHelper;

/**
 * Default IObservable implementation.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public abstract class Observable implements IObservable {

	private static final Log log = LogFactory.getLog(Observable.class);
	/** The snapshot of all registered listeners (for debug) */
	private static MultiValueMap snapshot = new MultiValueMap();
	private Set<IEventListener> listeners = new HashSet<IEventListener>();
	private Set<IEventListener> delayedListeners = new HashSet<IEventListener>();
	private Set<IEventListener> delayedRemoval = new HashSet<IEventListener>();
	/** This flag indicates whether Observable objects will notify their listeners or not */
	private static boolean notificationsActive = true;
	/** This flag indicates whether a notification is in progress */
	private Stack<Boolean> notificationInProgress = new Stack<Boolean>();

	/**
	 * @see com.nextep.datadesigner.model.IObservable#addListener(com.nextep.datadesigner.model.IEventListener)
	 */
	public final void addListener(IEventListener listener) {
		if (listener == null) {
			throw new ErrorException(this.getClass().getName() + ": Null listener added.");
		}
		if (isNotifying()) {
			log.debug("Added DELAYED listener <" + listener.getClass().getName() + "> to class <"
					+ this.getClass().getName() + ">");
			synchronized (delayedListeners) {
				delayedListeners.add(listener);
			}
		} else {
			synchronized (listeners) {
				boolean wasAdded = listeners.add(listener);
				// Storing snapshot
				if (Designer.isDebugging() && wasAdded) {
					snapshot.put(this, listener);
				}
			}
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#notifyListeners(com.nextep.datadesigner.model.ChangeEvent,
	 *      java.lang.Object)
	 */
	public final void notifyListeners(ChangeEvent event, Object o) {
		if (!notificationsActive) {
			return;
		}
		// Model with throws events are considered unsynched
		if (this instanceof ISynchronizable) {
			((ISynchronizable) this).setSynched(SynchStatus.UNKNOWN);
		}
		// Notifying listeners
		try {
			notificationInProgress.push(true);
			// Delegating the effective notifications to our global listener service
			final IListenerService listenerService = Designer.getListenerService();
			if (!getListeners().isEmpty()
					|| listenerService.getDispatchMode() != IListenerService.SYNCHED) {
				listenerService.notifyListeners(this, event, o);
			}
		} catch (ConcurrentModificationException e) {
			log.error("Concurrent modification from <" + this.getClass() + ":, event= "
					+ event.name());
			throw e;
		} finally {
			notificationInProgress.pop();
		}
		if (!isNotifying()) {
			// Removing delayed removals
			synchronized (delayedRemoval) {
				for (IEventListener l : delayedRemoval) {
					removeListener(l);
				}
				// Clearing all delayed listeners since we've processed them
				delayedRemoval.clear();
			}
			// Eventually adding delayed listeners
			synchronized (delayedListeners) {
				for (IEventListener l : delayedListeners) {
					addListener(l);
				}
				delayedListeners.clear();
			}
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#removeListener(com.nextep.datadesigner.model.IEventListener)
	 */
	public final void removeListener(IEventListener listener) {
		// final MultiValueMap debugMap = snapshot;
		if (isNotifying()) {
			log.debug("DELAYED listener removal <" + listener.getClass().getName() + "> to class <"
					+ this.getClass().getName() + ">");
			synchronized (delayedRemoval) {
				delayedRemoval.add(listener);
			}
		} else {
			synchronized (listeners) {
				boolean isRemoved = listeners.remove(listener);
				if (Designer.isDebugging() && isRemoved) {
					/* final Object o = */snapshot.remove(this, listener);
				}
			}
		}
	}

	/**
	 * Indicates this {@link IObservable} is currently notifying
	 * 
	 * @return <code>true</code> if a notification is in progress
	 */
	private boolean isNotifying() {
		return !notificationInProgress.empty();
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#getListeners()
	 */
	public Collection<IEventListener> getListeners() {
		return listeners;
	}

	/**
	 * Globally deactivates all notifications
	 */
	public static void deactivateListeners() {
		notificationsActive = false;
	}

	/**
	 * Globally activates all listeners
	 */
	public static void activateListeners() {
		notificationsActive = true;
	}

	/**
	 * @return a snapshot of all currently registered listener
	 */
	public static Object getSnapshot() {
		MultiValueMap snap = new MultiValueMap();
		snap.putAll(snapshot);
		return snap;
	}

	/**
	 * Dumps the listeners registration difference between the specified snapshot and the current
	 * snaphsot.
	 * 
	 * @param snapshot old snapshot
	 */
	@SuppressWarnings("unchecked")
	public static void dumpSnapshotDelta(Object snapshot) {
		MultiValueMap initial = (MultiValueMap) snapshot;
		MultiValueMap current = (MultiValueMap) getSnapshot();
		log.debug(">>>> DUMPING OBSERVABLE SNAPSHOT DIFFERENCE <<<<");
		log.debug("Initial listeners: " + initial.totalSize());
		log.debug("Current listeners: " + current.totalSize());
		boolean showWarning = (initial.totalSize() != current.totalSize());
		// Removing all identical listener registrations
		for (Object o : initial.keySet()) {
			Collection<IEventListener> listeners = (Collection<IEventListener>) initial.get(o);
			for (IEventListener l : listeners) {
				current.remove(o, l);
			}
		}
		// Our current map now only contains differences, we dump it
		log.debug("Residual listeners: " + current.totalSize());
		for (Object o : current.keySet()) {
			String name = NameHelper.getQualifiedName(o);

			log.debug("- Observable <" + name + "> has:");
			Collection<IEventListener> listeners = (Collection<IEventListener>) current.get(o);
			for (IEventListener l : listeners) {
				log.debug("    * Listener <" + l.toString() + "> of class ["
						+ l.getClass().getName() + "]");
			}
		}
		if (showWarning) {
			log.warn("Some listeners have not been released");
		}
		log.debug(">>>> DUMPING ENDS <<<<");
	}

	/**
	 * Helper method to fire the specified <code>ChangeEvent</code> if the two specified
	 * <code>Object</code> instances are not equal. This method should be called by each setter
	 * method in model implementations to avoid propagating unnecessary change events.
	 * 
	 * @param before an <code>Object</code> to compare to the after instance.
	 * @param after an <code>Object</code> to compare to the before instance.
	 * @param event the {@link ChangeEvent} to fire if the specified <code>Object</code> instances
	 *        are different.
	 */
	protected void notifyIfChanged(Object before, Object after, ChangeEvent event) {
		notifyIfChanged(before, after, event, after);
	}

	/**
	 * Helper method to fire the specified <code>ChangeEvent</code> if the two specified
	 * <code>Object</code> instances are not equal. This method should be called by each setter
	 * method in model implementations to avoid propagating unnecessary change events.
	 * 
	 * @param before an <code>Object</code> to compare to the after instance.
	 * @param after an <code>Object</code> to compare to the before instance.
	 * @param event the {@link ChangeEvent} to fire if the specified <code>Object</code> instances
	 *        are different.
	 */
	protected void notifyIfChanged(Object before, Object after, ChangeEvent event, Object arg) {
		if ((before != null && before.equals(after)) || (null == after && null == before))
			return;
		notifyListeners(event, arg);
	}
}
