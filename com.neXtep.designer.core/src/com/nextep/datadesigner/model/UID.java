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
package com.nextep.datadesigner.model;

import java.io.Serializable;

/**
 * This class represents a unique identifier
 *
 * @author Christophe Fondacci
 *
 */
public class UID implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private long id;

	/**
	 * Default constructor
	 * @param id identifier long value
	 */
	public UID(long id) {
		this.id = id;
	}
	/**
	 *
	 * @return the raw representation of this id
	 */
	public long rawId() {
		return id;
	}
	/**
	 * @return the string representation of this id
	 */
	public String toString() {
		return String.valueOf(id);
	}
	public boolean equals(Object o ) {
		if(o instanceof UID && o!=null) {
			return id == ((UID)o).rawId();
		}
		return false;
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
