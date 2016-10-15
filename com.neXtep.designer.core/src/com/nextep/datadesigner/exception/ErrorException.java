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
package com.nextep.datadesigner.exception;

/**
 * Exception thrown by the datadesigner application
 * when a user operation has generated an unexpected
 * error.
 * <br><br>
 * <b>Note that this is a RuntimeException</b>
 *
 * @author Christophe Fondacci
 *
 */
public class ErrorException extends DesignerException {
	static final long serialVersionUID = 1L;
	public ErrorException(String message) {
		super(message);
	}
	public ErrorException(Throwable e) {
		super(e);
	}
	public ErrorException(String message, Throwable e) {
		super(message,e);
	}
}
