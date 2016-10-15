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
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * @author Christophe Fondacci
 *
 */
public class UIDComparator implements Comparator<IdentifiedObject> {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(IdentifiedObject o1, IdentifiedObject o2) {
		long id1,id2;
		if(o1 != null) 	id1 = o1.getUID().rawId();
		else 			id1 = -1;
		if(o2 != null)	id2 = o2.getUID().rawId();
		else			id2 = -1;

		if(id1 == id2) {
			return 0;
		} else if(id1>id2) {
			return 1;
		}
		return -1;
	}

}
