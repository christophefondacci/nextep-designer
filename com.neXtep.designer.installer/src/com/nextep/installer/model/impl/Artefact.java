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
package com.nextep.installer.model.impl;

import com.neXtep.shared.model.ArtefactType;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDelivery;

public class Artefact implements IArtefact {

	private String filename;
	private String relativePath;
	private ArtefactType type;
	private IDelivery delivery;
	private DBVendor vendor;
	public String getFilename() {
		return filename;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public ArtefactType getType() {
		return type;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setRelativePath(String path) {
		this.relativePath = path;
	}

	public void setType(ArtefactType type) {
		this.type = type;
	}

	public IDelivery getDelivery() {
		return delivery;
	}

	public void setDelivery(IDelivery delivery) {
		this.delivery=delivery;
	}

	public DBVendor getDBVendor() {
		return vendor;
	}

	public void setDBVendor(DBVendor vendor) {
		this.vendor = vendor;
	}

}
