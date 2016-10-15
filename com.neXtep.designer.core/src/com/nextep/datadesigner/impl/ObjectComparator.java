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
package com.nextep.datadesigner.impl;

import java.util.Comparator;

/**
 * @author Christophe Fondacci
 *
 */
public class ObjectComparator implements Comparator<Object> {

	private static ObjectComparator instance = null;
	private ObjectComparator() {}
	public static Comparator<Object> getInstance() {
		if(instance == null) {
			instance = new ObjectComparator();
		}
		return instance;
	}
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object o1, Object o2) {
		if(o1 != null) {
			return o1.toString().compareTo(o2 == null ? null : o2.toString());
		}
		if(o2 != null) {
			return -(o2.toString().compareTo(o1 == null ? null : o1.toString()));
		}
		return 0;
	}

}
