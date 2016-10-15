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
package com.nextep.datadesigner.exception;

/**
 * This exception is thrown when an object is out of date
 * with the underlying repository database.<br>
 * Out of date objects are immediately refreshed with repository
 * information before throwing this exception.
 * @author Christophe
 *
 */
public class OutOfDateObjectException extends ErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2499659710302111343L;
	private Object staleObject;
	public OutOfDateObjectException(Object o, String message) {
		super(message);
		this.staleObject = o;
	}
	public Object getStaleObject() {
		return staleObject;
	}
}
