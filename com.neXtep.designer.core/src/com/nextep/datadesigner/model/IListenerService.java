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
package com.nextep.datadesigner.model;

/**
 * A service for listener registration. All listener registration should always use a listener
 * service.
 * 
 * @author Christophe Fondacci
 */
public interface IListenerService {

	final DispatchMode SYNCHED = DispatchMode.SYNCHED;
	final DispatchMode ASYNCHED = DispatchMode.ASYNCHED;

	enum DispatchMode {
		SYNCHED, ASYNCHED
	}

	/**
	 * Indicates how to dispatch event notifications. This is a thread-local definition, meaning
	 * that when a background job process toggles the dispatch mode to ASYNCHRONOUS event
	 * propagation, it does not affect events fired in the UI thread by the user.
	 * 
	 * @param mode dispatch mode being one of the SYNCHED / ASYNCHED constants
	 */
	void setDispatchMode(DispatchMode mode);

	/**
	 * Retrieves the current dispatch mode used by the service
	 * 
	 * @return the current {@link DispatchMode}
	 */
	DispatchMode getDispatchMode();

	/**
	 * Registers a listener from a given instigator.<br>
	 * Listener may not be listening until calling
	 * {@link IListenerService#activateListeners(Object)} on the instigator of this registration. <br>
	 * The lifecycle of the listener will be the same as the instigator which registered it.
	 * 
	 * @param instigator instance of the class registering a listener
	 * @param o the observable object to listen to
	 * @param l the listener instance which should be registered
	 */
	void registerListener(Object instigator, IObservable o, IEventListener l);

	/**
	 * Activates the listeners of a given instigator.
	 * 
	 * @param instigator the instance which has registered its listeners
	 */
	void activateListeners(Object instigator);

	/**
	 * Unregisters all listeners of a given instigator. All registered listeners will be removed.
	 * 
	 * @param instigator instigator which has registered listeners
	 */
	void unregisterListeners(Object instigator);

	/**
	 * @param o
	 * @param l
	 */
	void unregisterListener(IObservable o, IEventListener l);

	/**
	 * Switches all listeners currently observing the old observable to the new specified
	 * observable. All listeners will be transferred from old to new observable and the old
	 * observable will have no more listeners after this call.
	 * 
	 * @param oldObservable
	 * @param newObservable
	 */
	void switchListeners(IObservable oldObservable, IObservable newObservable);

	/**
	 * Notifies listeners of the {@link IObservable} object the given event. An observable should
	 * always delegate notification to this service method for global control over the
	 * notifications.
	 * 
	 * @param observable observable which fires the event
	 * @param event event to fire
	 * @param arg argument to fire along with the event
	 */
	void notifyListeners(IObservable observable, ChangeEvent event, Object arg);

}
