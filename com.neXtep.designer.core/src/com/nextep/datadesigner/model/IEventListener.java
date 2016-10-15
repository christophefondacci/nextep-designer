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
 * Global generic listener that allows object to
 * communicate changes.
 *
 * @author Christophe Fondacci
 *
 */
public interface IEventListener {

	/**
	 * This method receives events from the observables it is
	 * listening to. The event will be typed as a {@link ChangeEvent}
	 * enum value. The source observable which has fired the event
	 * is sent in the method call with a custom data value to
	 * transmit data.
	 *
	 * @param event event which is fired
	 * @param source the source observable which originated the fired event
	 * @param data a custom data value related to the fired event.
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data);

}
