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

import java.text.MessageFormat;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.helpers.NameHelper;

/**
 * This exception is thrown whenever an element is locked (=is not modifiable) but needs to be
 * modified.
 * 
 * @author Christophe Fondacci
 */
public class LockedElementException extends ErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7209394887145475630L;

	public LockedElementException(ILockable<?> object) {
		super(
				MessageFormat
						.format(
								CoreMessages.getString("exception.lockedElement"), NameHelper.getQualifiedName(object))); //$NON-NLS-1$
	}
}
