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
package com.nextep.designer.dbgm.mysql.factories;

import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.factories.IndexFactory;
import com.nextep.designer.dbgm.mysql.impl.MySQLIndex;
import com.nextep.designer.dbgm.mysql.model.IMySQLIndex;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * An extension of the default index factory which instantiates MySQL-specific indexes and handles
 * the proper copy of the specific attributes.
 * 
 * @author Christophe Fondacci
 */
public class MySQLIndexFactory extends IndexFactory {

	@Override
	public IVersionable<?> createVersionable() {
		return new MySQLIndex();
	}

	@Override
	public void rawCopy(IVersionable<?> source, IVersionable<?> destination) {
		super.rawCopy(source, destination);
		final IMySQLIndex src = (IMySQLIndex) source.getVersionnedObject().getModel();
		final IMySQLIndex tgt = (IMySQLIndex) destination.getVersionnedObject().getModel();

		for (IReference srcColRef : src.getIndexedColumnsRef()) {
			final Integer colPrefixLength = src.getColumnPrefixLength(srcColRef);
			if (colPrefixLength != null) {
				tgt.setColumnPrefixLength(srcColRef, colPrefixLength);
			}
		}
	}
}
