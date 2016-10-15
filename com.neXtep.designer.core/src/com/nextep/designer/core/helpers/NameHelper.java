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
package com.nextep.designer.core.helpers;

import java.util.ArrayList;
import java.util.List;

import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * Utility class providing helpful methods to manipulate names in neXtep.
 * 
 * @author Christophe Fondacci
 */
public final class NameHelper {

	private NameHelper() {
	}

	/**
	 * Builds a list containing the names of the passed {@link INamedObject} as
	 * plain String.
	 * 
	 * @param objects
	 *            collection of {@link INamedObject} to unwrap names
	 * @return a list of objects names
	 */
	public static List<String> buildNameList(List<? extends INamedObject> objects) {
		final List<String> names = new ArrayList<String>(objects.size());
		for (INamedObject namedObj : objects) {
			names.add(namedObj.getName());
		}
		return names;
	}

	/**
	 * Given an object this method will generate the most qualified name String
	 * which can identify this object.
	 * 
	 * @param o
	 *            object to name
	 * @return the most qualified name
	 */
	public static String getQualifiedName(Object o) {
		return NameHelper.getQualifiedName(o, false);
	}

	/**
	 * Given an object this method will generate the most qualified name String
	 * which can identify this object. A flag allows callers to indicate whether
	 * they want the reference id to appear in the returned string.
	 * 
	 * @param o
	 *            object to name
	 * @param withReferenceId
	 *            when set to true any {@link IReferenceable} element will have
	 *            its reference identifier in the returned name
	 * @return the most qualified name
	 */
	public static String getQualifiedName(Object o, boolean withReferenceId) {
		String name = ""; //$NON-NLS-1$
		if (o instanceof ITypedObject) {
			name += ((ITypedObject) o).getType().getName().toLowerCase() + " "; //$NON-NLS-1$
		}
		if (o instanceof INamedObject) {
			name += ((INamedObject) o).getName();
		}
		if (withReferenceId) {
			if (o instanceof IReferenceable) {
				IReference r = ((IReferenceable) o).getReference();
				if (r != null && !r.isVolatile()) {
					name += "@" + r.getUID(); //$NON-NLS-1$
				}
			}
		}
		return name;
	}

	/**
	 * A generic method that exposes the name of any object
	 * 
	 * @param o
	 *            the object to get the name of
	 * @return the name of this object
	 */
	public static String getName(Object o) {
		if (o instanceof INamedObject) {
			return ((INamedObject) o).getName();
		}
		return "unknown";
	}
}
