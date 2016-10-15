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
package com.nextep.designer.util;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.CoreMessages;

/**
 * Convenience assertion class
 * 
 * @author Christophe Fondacci
 */
public final class Assert {

	private Assert() {
	}

	public static void notNull(Object o, String message) throws ErrorException {
		if (o == null) {
			throw new ErrorException(message);
		}
	}

	public static void instanceOf(Object o, Class<?> instanceOfClass, String message)
			throws ErrorException {
		notNull(instanceOfClass, CoreMessages.getString("assert.instanceOf.nullClassError")); //$NON-NLS-1$
		notNull(o, message);
		if (!instanceOfClass.isAssignableFrom(o.getClass())) {
			throw new ErrorException(message);
		}
	}

	public static void equals(Object expected, Object actual, String message) throws ErrorException {
		if ((expected == null || actual == null) && (expected != actual)) {
			throw new ErrorException(message);
		} else if (!expected.equals(actual)) {
			throw new ErrorException(message);
		}
	}
}
