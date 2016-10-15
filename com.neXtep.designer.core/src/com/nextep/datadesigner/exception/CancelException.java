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

import com.nextep.designer.core.CoreMessages;

/**
 * Exception thrown by the datadesigner application when an operation has been cancelled by the
 * user. <br>
 * <br>
 * <b>Note that this is a RuntimeException</b>
 * 
 * @author Christophe Fondacci
 */
public class CancelException extends DesignerException {

	static final long serialVersionUID = 1L;

	public CancelException() {
		this(CoreMessages.getString("exception.cancel.defaultMessage")); //$NON-NLS-1$
	}

	public CancelException(String message) {
		super(message);
	}

	public CancelException(Throwable e) {
		super(e);
	}

	public CancelException(String message, Throwable e) {
		super(message, e);
	}
}
