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
package com.nextep.datadesigner.vcs.services;

/**
 * An evaluator, used for enabling / disabling some controls depending
 * on the current vendor.<br>
 * Typically, menu contributions would first do an "adapt" with a {@link DBVendorEvaluator}
 * class and then apply an "equals" with a database vendor to know if the contribution
 * can be activated.
 * 
 * @author Christophe
 *
 */
public class DBVendorEvaluator {

	private static DBVendorEvaluator instance = null;
	public static final DBVendorEvaluator getInstance() {
		if(instance==null) {
			instance = new DBVendorEvaluator();
		}
		return instance;
	}
	private DBVendorEvaluator() {}
	/**
	 * @return true if and only if the specified string is the DBVendor of the current view
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof String) {
			return ((String)obj).equals(VersionHelper.getCurrentView().getDBVendor().name());
		}
		return false;
	}
}
