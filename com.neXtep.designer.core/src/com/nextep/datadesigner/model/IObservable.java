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
package com.nextep.datadesigner.model;

import java.util.Collection;

/**
 * Defines an observable object.
 *
 * @author Christophe Fondacci
 *
 */
public interface IObservable {
	/**
	 * Adds a listener to the view
	 *
	 * @param listener
	 */
	public void addListener(IEventListener listener);
	/**
	 * Removes a listener from the view
	 *
	 * @param listener
	 */
	public void removeListener(IEventListener listener);
	/**
	 * Notifies the listeners of changes on this view
	 *
	 */
	public void notifyListeners(ChangeEvent event, Object o);
	/**
	 * @return a collection of all registered listeners
	 */
	public Collection<IEventListener> getListeners();
	/**
	 * Adds a listener to this observable. This method will delay
	 * the listener registration so that all current notifications
	 * will finish. The listener will be added immediately after.<br>
	 * This allows callers to add a listener within an event handling,
	 * avoiding the ConcurrentModificationException.
	 *
	 * @param listener new listener
	 * @param delayed set to true to delay listener registration, false equals to calling addListener
	 */
//	public void addListener(IEventListener listener, boolean delayed);
}
