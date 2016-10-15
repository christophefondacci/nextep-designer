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

/**
 * This listener defines methods to be notified by comparison actions coming from the neXtep
 * workbench.<br>
 * This is typically what the comparison view implements to be notified of new comparisons
 * triggerred by the user.
 * 
 * @author Christophe Fondacci
 */
public interface IComparisonListener {

	/**
	 * Notifies that a new comparison has been performed. The comparison result is provided as
	 * argument.
	 * 
	 * @param a short description about what is this comparison
	 * @param comparisonItems results of a comparison.
	 */
	void newComparison(String description, IComparisonItem... comparisonItems);

}
