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
package com.nextep.designer.beng.model.impl;

import java.io.File;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.model.IExternalFile;

/**
 * External file implementation.
 * 
 * @author Christophe Fondacci
 *
 */
public class ExternalFile extends IDNamedObservable implements IExternalFile {

	/** File directory */
	private String directory;
	/** Parent delivery*/
	private IDeliveryModule delivery;
	/** Position in external files list */
	private int position;
	
	public ExternalFile(File f) {
		setName(f.getName());
		setDirectory(f.getAbsolutePath());
	}
	public ExternalFile() {}
	@Override
	public IDeliveryModule getDelivery() {
		return delivery;
	}

	@Override
	public String getDirectory() {
		return directory;
	}

	@Override
	public void setDelivery(IDeliveryModule delivery) {
		this.delivery = delivery;
	}

	@Override
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/**
	 * @see com.nextep.designer.beng.model.IExternalFile#getPosition()
	 */
	@Override
	public int getPosition() {
		return position;
	}

	/**
	 * @see com.nextep.designer.beng.model.IExternalFile#setPosition(int)
	 */
	@Override
	public void setPosition(int position) {
		this.position = position;
	}

}
