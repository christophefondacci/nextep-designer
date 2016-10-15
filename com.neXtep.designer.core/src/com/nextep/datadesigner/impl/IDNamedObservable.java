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
package com.nextep.datadesigner.impl;

import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;

/**
 * An Identified named observable base implementation
 *
 * @author Christophe Fondacci
 *
 */
public abstract class IDNamedObservable extends NamedObservable implements
		IdentifiedObject {

	private UID id;

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#getUID()
	 */
	@Override
	public UID getUID() {
		return id;
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#setUID(com.nextep.datadesigner.model.UID)
	 */
	@Override
	public void setUID(UID id) {
		this.id = id;
	}
	/**
	 * Hibernate ID getter
	 * @return
	 */
	public long getId() {
		if(id == null) {
			return 0;
		}
		return id.rawId();
	}
	/**
	 * Hibernate ID setter
	 * @param id
	 */
	public void setId(long id) {
		this.id = new UID(id);
	}
}
