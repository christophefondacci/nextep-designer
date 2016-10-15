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
package com.nextep.designer.testing.model;

import com.nextep.datadesigner.model.ITypedObject;


/**
 * Represents an event occurring during a test.
 * 
 * @author Christophe
 *
 */
public interface ITestEventHandler {

	/**
	 * Handles the event thrown by a tester.
	 * 
	 * @param source object which is being tested
	 * @param event occurred event
	 * @param status status of the test
	 * @param eventDetails details
	 */
	public abstract void handle(ITypedObject source, TestEvent event, TestStatus status, Object eventDetails);
}
