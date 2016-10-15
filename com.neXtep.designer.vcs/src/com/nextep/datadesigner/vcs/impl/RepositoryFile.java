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

import java.util.Date;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.vcs.model.IRepositoryFile;

public class RepositoryFile extends IDNamedObservable implements
		IRepositoryFile {

	private long size;
	private Date importDate = new Date();
	public RepositoryFile() {
		setReference(new Reference(getType(),null,this));
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public long getFileSizeKB() {
		return size;
	}

	@Override
	public void setFileSizeKB(long size) {
		this.size=size;
	}

	@Override
	public Date getImportDate() {
		return importDate;
	}

	@Override
	public void setImportDate(Date date) {
		this.importDate = date;
	}

}
