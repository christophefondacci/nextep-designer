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
package com.nextep.designer.vcs.model;

import java.util.Date;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

public interface IRepositoryFile extends INamedObject, IObservable, IReferenceable, IdentifiedObject, ITypedObject {

	public static final String TYPE_ID="FILE";

	/**
	 * Defines the file size of this repository file
	 * @return file size
	 */
	public long getFileSizeKB();
	/**
	 * Defines the file size of the stored repository file
	 * @param size size in kilo bytes
	 */
	public void setFileSizeKB(long size);
	/**
	 * @return the date when this file has been imported
	 */
	public Date getImportDate();
	/**
	 * Defines the date when this file has been imported
	 * @param date file import date
	 */
	public void setImportDate(Date date);
}
