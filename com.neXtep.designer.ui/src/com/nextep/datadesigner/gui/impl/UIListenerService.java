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
package com.nextep.datadesigner.gui.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import com.nextep.datadesigner.gui.model.IConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public class UIListenerService implements IListenerService {

	private static final Log log = LogFactory.getLog(UIListenerService.class);
	final private MultiValueMap listenersMap = new MultiValueMap();
	final private Map<ListeningPair, Object> listeningPairInstigators = new HashMap<ListeningPair, Object>();
	private boolean isBlocked = false;
	private List<InternalEvent> bufferedEvents;
	private DispatchMode dispatchMode = DispatchMode.SYNCHED;

	public UIListenerService() {
		bufferedEvents = new LinkedList<InternalEvent>();
	}

	/**
	 * An interface to define delayed events
	 */
	private interface InternalEvent {

		void execute(boolean uiThread);
	}

	/**
	 * Buffered switch listener event
	 */
	private class SwitchListenerEvent implements InternalEvent {

		private IObservable oldObservable, newObservable;

		public SwitchListenerEvent(IObservable oldObservable, IObservable newObservable) {
			this.oldObservable = oldObservable;
			this.newObservable = newObservable;
		}

		@Override
		public void execute(boolean uiThread) {
			doSwitchListeners(oldObservable, newObservable, uiThread);
		}
	}

	/**
	 * Delayed register listener event
	 */
	private class RegisterListenerEvent implements InternalEvent {

		private Object instigator;
		private IObservable observable;
		private IEventListener listener;

		public RegisterListenerEvent(Object instigator, IObservable observable,
				IEventListener listener) {
			this.instigator = instigator;
			this.observable = observable;
			this.listener = listener;
		}

		@Override
		public void execute(boolean uiThread) {
			doRegisterListener(instigator, observable, listener);
		}
	}

	/**
	 * Delayed unregister listener event
	 */
	private class UnregisterListenerEvent implements InternalEvent {

		private IObservable observable;
		private IEventListener listener;

		public UnregisterListenerEvent(IObservable observable, IEventListener listener) {
			this.observable = observable;
			this.listener = listener;
		}

		@Override
		public void execute(boolean uiThread) {
			doUnregisterListener(observable, listener);
		}
	}

	private class UnregisterInstigatorEvent implements InternalEvent {

		private Object instigator;

		public UnregisterInstigatorEvent(Object instigator) {
			this.instigator = instigator;
		}

		@Override
		public void execute(boolean uiThread) {
			doUnregisterListeners(instigator);
		}
	}

	private class ActivateListenersEvent implements InternalEvent {

		private Object instigator;

		public ActivateListenersEvent(Object instigator) {
			this.instigator = instigator;
		}

		@Override
		public void execute(boolean uiThread) {
			doActivateListeners(instigator);
		}
	}

	private class NotificationEvent implements InternalEvent {

		private IObservable observable;
		private ChangeEvent event;
		private Object arg;

		public NotificationEvent(IObservable observable, ChangeEvent event, Object arg) {
			super();
			this.observable = observable;
			this.event = event;
			this.arg = arg;
		}

		@Override
		public void execute(boolean uiThread) {
			doNotifyListeners(observable, event, arg, uiThread);
		}
	}

	/**
	 * A pair class for storing {@link IObservable} / {@link IEventListener} couples in the hash
	 * map.
	 * 
	 * @author Christophe Fondacci
	 */
	private class ListeningPair {

		private IObservable observable;
		private IEventListener listener;

		public ListeningPair(IObservable observable, IEventListener listener) {
			this.observable = observable;
			this.listener = listener;
		}

		public IObservable getObservable() {
			return observable;
		}

		public IEventListener getListener() {
			return listener;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ListeningPair) {
				ListeningPair lp = (ListeningPair) obj;
				return getObservable() == lp.getObservable() && getListener() == lp.getListener();
			}
			return false;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return (observable != null ? observable.hashCode() : 0)
					+ (listener != null ? listener.hashCode() : 0);
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IListenerService#activateListeners(java.lang.Object)
	 */
	@Override
	public void activateListeners(Object instigator) {
		if (isBlocked) {
			bufferedEvents.add(new ActivateListenersEvent(instigator));
		} else {
			doActivateListeners(instigator);
		}
	}

	@SuppressWarnings("unchecked")
	private void doActivateListeners(Object instigator) {
		Collection<ListeningPair> listeners = listenersMap.getCollection(instigator);
		if (listeners == null)
			return;
		for (ListeningPair lp : listeners) {
			if (lp.getObservable() != null) {
				lp.getObservable().addListener(lp.getListener());
			} else {
				log.debug("WARN: Null observable detected"); //$NON-NLS-1$
			}
		}

		// Handling connector registration to auto unregister listeners
		if (instigator instanceof IConnector<?, ?>) {
			handleSWTListener(instigator, ((IConnector<?, ?>) instigator).getSWTConnector());
		} else if (instigator instanceof Widget) {
			handleSWTListener(instigator, (Widget) instigator);
		}
	}

	protected void handleSWTListener(final Object instigator, final Widget widget) {
		widget.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				unregisterListeners(instigator);
			}
		});
	}

	/**
	 * @see com.nextep.datadesigner.model.IListenerService#registerListener(com.nextep.datadesigner.model.IObservable,
	 *      com.nextep.datadesigner.model.IEventListener)
	 */
	@Override
	public void registerListener(final Object instigator, final IObservable o,
			final IEventListener l) {
		if (isBlocked) {
			bufferedEvents.add(new RegisterListenerEvent(instigator, o, l));
		} else {
			doRegisterListener(instigator, o, l);
		}
	}

	private void doRegisterListener(final Object instigator, final IObservable o,
			final IEventListener l) {
		Runnable runnable = new Runnable() {

			public void run() {
				// Storing listener
				final ListeningPair pair = new ListeningPair(o, l);
				// ==> For listener switch, delayed activation and instigator unregistration
				listenersMap.put(instigator, pair);
				// ==> To fastly unregister listeners
				listeningPairInstigators.put(pair, instigator);
				// Activating if we have a Widget instigator
				if (instigator instanceof Widget) {
					activateListeners(instigator);
				} else if (instigator instanceof IConnector<?, ?>) {
					if (((IConnector<?, ?>) instigator).isInitialized()) {
						activateListeners(instigator);
					}
				} else {
					// Adding listener only if not a connector and not a widget
					if (o != null) {
						o.addListener(l);
					}
				}

			}
		};
		exec(runnable, true);
	}

	/**
	 * @see com.nextep.datadesigner.model.IListenerService#unregisterListener(com.nextep.datadesigner.model.IObservable,
	 *      com.nextep.datadesigner.model.IEventListener)
	 */
	@Override
	public void unregisterListener(IObservable o, IEventListener l) {
		if (isBlocked) {
			bufferedEvents.add(new UnregisterListenerEvent(o, l));
		} else {
			doUnregisterListener(o, l);
		}
	}

	private void doUnregisterListener(final IObservable o, final IEventListener l) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// Checking if we should remove a controller listener
				if (l instanceof ITypedObjectUIController) {
					// We only remove controller listener on volatile referenceables
					if (o instanceof IReferenceable) {
						if (((IReferenceable) o).getReference() != null
								&& ((IReferenceable) o).getReference().isVolatile()) {
							unregisterInstigatorListener(o, l);
						}
					}
				} else {
					if (o != null) {
						unregisterInstigatorListener(o, l);
					}
				}
			}
		};
		exec(runnable, true);
	}

	/**
	 * Unregisters the listener and ensures the listening pair (observable, listener) is removed
	 * from registry.
	 * 
	 * @param o observable for which you want to unregister a listener
	 * @param l the removed listener
	 */
	private void unregisterInstigatorListener(IObservable o, IEventListener l) {
		// Removing listener
		o.removeListener(l);
		// Cleaning registry
		final ListeningPair listeningPair = new ListeningPair(o, l);
		final Object instigator = listeningPairInstigators.get(listeningPair);
		listenersMap.remove(instigator, listeningPair);
		listeningPairInstigators.remove(listeningPair);
	}

	/**
	 * @see com.nextep.datadesigner.model.IListenerService#unregisterListeners(java.lang.Object)
	 */
	@Override
	public void unregisterListeners(Object instigator) {
		if (isBlocked) {
			bufferedEvents.add(new UnregisterInstigatorEvent(instigator));
		} else {
			doUnregisterListeners(instigator);
		}
	}

	@SuppressWarnings("unchecked")
	private void doUnregisterListeners(final Object instigator) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				Collection<ListeningPair> listeners = listenersMap.getCollection(instigator);
				if (listeners == null)
					return;
				for (ListeningPair lp : listeners) {
					if (lp.getObservable() != null && lp.getListener() != null) {
						lp.getObservable().removeListener(lp.getListener());
					}
				}
				listenersMap.remove(instigator);

			}
		};
		exec(runnable, true);
	}

	@Override
	public void switchListeners(IObservable oldObservable, IObservable newObservable) {
		if (isBlocked) {
			bufferedEvents.add(new SwitchListenerEvent(oldObservable, newObservable));
		} else {
			doSwitchListeners(oldObservable, newObservable, true);
		}
	}

	private void doSwitchListeners(final IObservable oldObservable,
			final IObservable newObservable, boolean uiThread) {
		final Runnable runnable = new Runnable() {

			@Override
			public void run() {
				switchObservableListeners(oldObservable, newObservable);
				// Switching inner model references
				if ((oldObservable instanceof IReferenceContainer) && newObservable != null) {
					final Map<IReference, IReferenceable> oldRefMap = ((IReferenceContainer) oldObservable)
							.getReferenceMap();
					final Map<IReference, IReferenceable> newRefMap = ((IReferenceContainer) newObservable)
							.getReferenceMap();
					for (IReference r : oldRefMap.keySet()) {
						final IReferenceable oldObj = oldRefMap.get(r);
						final IReferenceable newObj = newRefMap.get(r);
						if (oldObj != newObj) {
							if (oldObj instanceof IObservable) {
								// If the reference exists in new object we switch, else we
								// unregister
								// listeners
								if (newObj != null) {
									switchObservableListeners((IObservable) oldObj,
											(IObservable) newObj);
								} else {
									for (IEventListener l : new ArrayList<IEventListener>(
											((IObservable) oldObj).getListeners())) {
										unregisterListener((IObservable) oldObj, l);
									}
								}
							}
						}
					}
				}

			}
		};
		exec(runnable, uiThread);
	}

	private void exec(Runnable runnable, boolean uiThread) {
		if (uiThread) {
			// switch (dispatchMode) {
			// case SYNCHED:
			Display.getDefault().syncExec(runnable);
			// break;
			// case ASYNCHED:
			// Display.getDefault().asyncExec(runnable);
			// break;
			// }
		} else {
			runnable.run();
		}
	}

	@SuppressWarnings("unchecked")
	private void switchObservableListeners(IObservable oldObservable, IObservable newObservable) {
		log.debug(MessageFormat
				.format("Switching all listeners from <{0}> to <{1}>", oldObservable.toString(), newObservable.toString())); //$NON-NLS-1$

		for (IEventListener listener : new ArrayList<IEventListener>(oldObservable.getListeners())) {
			ListeningPair oldListeningPair = new ListeningPair(oldObservable, listener);
			ListeningPair newListeningPair = new ListeningPair(newObservable, listener);
			final Object instigator = listeningPairInstigators.get(oldListeningPair);

			// Unregistering listeners map
			listenersMap.remove(instigator, oldListeningPair);
			// Registering new listener map
			listenersMap.put(instigator, newListeningPair);

			// Unregistering reverse listening pair map
			listeningPairInstigators.remove(oldListeningPair);
			listeningPairInstigators.put(newListeningPair, instigator);

			// Removing old observable listener
			oldObservable.removeListener(listener);
			// And adding the listener to new observable
			newObservable.addListener(listener);

			// Handling model update
			if (listener instanceof IModelOriented<?>) {
				// Since we need to make sure we finish our listener notification, we intercept any
				// exception raised
				try {
					((IModelOriented) listener).setModel(newObservable);
				} catch (RuntimeException e) {
					log.debug("Error occurred when setting observable model : " + e.getMessage(), e); //$NON-NLS-1$
				}
			}
		}

	}

	/**
	 * A convenience method to log a model change.
	 * 
	 * @param newModel new model which has replaced the former one
	 */
	protected void logChangedModel(Object oldModel, Object newModel, Object listener) {
		String type = "<Untyped>"; //$NON-NLS-1$
		if (newModel instanceof ITypedObject) {
			type = ((ITypedObject) newModel).getType().getName();
		}
		log.debug(MessageFormat.format(
				"Switching model - {0} model changed from <{1}> to <{2}> on listener <{3}>", //$NON-NLS-1$
				type, oldModel, newModel, listener));
	}

	@Override
	public void notifyListeners(IObservable observable, ChangeEvent event, Object arg) {
		if (log.isDebugEnabled()) {
			log.debug("Notify: event notification of " + event.name() + " on listeners of " //$NON-NLS-1$ //$NON-NLS-2$
					+ observable);
		}
		if (isBlocked) {
			bufferedEvents.add(new NotificationEvent(observable, event, arg));
		} else {
			doNotifyListeners(observable, event, arg, true);
		}
	}

	private void doNotifyListeners(final IObservable observable, final ChangeEvent event,
			final Object arg, final boolean uiThread) {
		final Runnable runnable = new Runnable() {

			@Override
			public void run() {
				if (log.isDebugEnabled()) {
					log.debug("DoNotify[" + uiThread + "]: notifying " + event.name() + " event on listeners of " //$NON-NLS-1$ //$NON-NLS-2$
							+ observable);
				}
				for (IEventListener l : new ArrayList<IEventListener>(observable.getListeners())) {
					try {
						if (log.isDebugEnabled()) {
							log.debug("  >> Notifying listener " + l); //$NON-NLS-1$
						}
						l.handleEvent(event, observable, arg);
					} catch (RuntimeException e) {
						// We silently log the exception as we need to fire all events anyway
						log.error("Problems while propagating UI event: " + e.getMessage(), e);
					}
				}

			}
		};
		exec(runnable, uiThread);
	}

	@Override
	public void setDispatchMode(DispatchMode mode) {
		this.dispatchMode = mode;
		// If SYNCHED is asked, we make sure we are synched
		if (mode == DispatchMode.SYNCHED) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					while (Display.getCurrent().readAndDispatch()) {
					}
				}
			});
		}
	}

	@Override
	public DispatchMode getDispatchMode() {
		return dispatchMode;
	}
}
