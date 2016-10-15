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
package com.nextep.installer.exception;

/**
 * A root exception for all exceptions fired by the installer.
 * 
 * @author Christophe Fondacci
 */
public class InstallerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -177526975618314130L;

	public InstallerException(String message) {
		super(message);
	}

	public InstallerException(String message, Throwable cause) {
		super(message, cause);
	}

	public InstallerException(Throwable t) {
		super(t);
	}
}
