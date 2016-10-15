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
package com.nextep.datadesigner.vcs.impl;

import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * This class provides summarized information about a container.
 * It allows to quickly load information about a container without
 * loading all dependent objects.
 * 
 * @author Christophe
 *
 */
public class ContainerInfo extends IDNamedObservable {

	private IVersionInfo release;
	private DBVendor vendor;
	public void setRelease(IVersionInfo release) {
		this.release = release;
	}
	public IVersionInfo getRelease() {
		return release;
	}
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ContainerInfo)) {
			return false;
		}
		return getRelease().equals(((ContainerInfo)obj).getRelease());
	}
	@Override
	public int hashCode() {
		return 1;
	}
	@Override
	public IReference getReference() {
		if(release!=null) {
			return release.getReference();
		}
		return null;
	}
	public void setDBVendor(DBVendor vendor) {
		this.vendor=vendor;
	}
	public DBVendor getDBVendor() {
		return this.vendor;
	}
}
