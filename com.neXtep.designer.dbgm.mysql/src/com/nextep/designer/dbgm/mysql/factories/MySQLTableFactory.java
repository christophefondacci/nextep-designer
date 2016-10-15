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

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.designer.dbgm.factories.TableFactory;
import com.nextep.designer.dbgm.mysql.impl.MySQLTable;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class MySQLTableFactory extends TableFactory {

	/**
	 * @see com.nextep.designer.dbgm.factories.TableFactory#createVersionable()
	 */
	@Override
	public IVersionable<IBasicTable> createVersionable() {
		return new MySQLTable();
	}

	/**
	 * @see com.nextep.designer.dbgm.factories.TableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		super.rawCopy(source, destination);
		MySQLTable src = (MySQLTable) source.getVersionnedObject().getModel();
		MySQLTable tgt = (MySQLTable) destination.getVersionnedObject().getModel();

		tgt.setEngine(src.getEngine());
		tgt.setCharacterSet(src.getCharacterSet());
		tgt.setCollation(src.getCollation());
	}

}
