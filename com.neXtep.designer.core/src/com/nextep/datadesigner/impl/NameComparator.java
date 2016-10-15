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
import com.nextep.datadesigner.model.INamedObject;

/**
 * Name comparator for classes implementing the <code>INamedObject</code> which want to be sorted by
 * name. This avoids implementing the Comparable interface for each <code>INamedObject</code>
 * implementors.
 * 
 * @author Christophe Fondacci
 */
public class NameComparator implements Comparator<INamedObject> {

	private static NameComparator instance = null;

	private NameComparator() {
	}

	/**
	 * Instance getter
	 * 
	 * @return this singleton instance
	 */
	public static NameComparator getInstance() {
		if (instance == null) {
			instance = new NameComparator();
		}
		return instance;
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(INamedObject o1, INamedObject o2) {
		if (o1 == o2) {
			// Nullity equality (DES-925)
			return 0;
		} else if (o1 != null && o2 != null && o1.getName() == o2.getName()) {
			// Null names equality (DES-925)
			return 0;
		} else if (o1 == null || o1.getName() == null) {
			return -1;
		} else if (o2 == null || o2.getName() == null) {
			return 1;
		} else {
			return o1.getName().compareTo(o2.getName());
		}
	}
}
