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
package com.nextep.datadesigner.sqlgen.impl;

import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * The default generator which does nothing.
 * 
 * @author Christophe Fondacci
 */
public class NullGenerator implements ISQLGenerator {

	private DBVendor vendor;

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#doDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult doDrop(Object model) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult generateDrop(Object model) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateFullSQL(java.lang.Object)
	 */
	@Override
	public IGenerationResult generateFullSQL(Object model) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateIncrementalSQL(com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public IGenerationResult generateIncrementalSQL(IComparisonItem result) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DBVendor getVendor() {
		return vendor;
	}

	@Override
	public void setVendor(DBVendor vendor) {
		this.vendor = vendor;
	}
}
