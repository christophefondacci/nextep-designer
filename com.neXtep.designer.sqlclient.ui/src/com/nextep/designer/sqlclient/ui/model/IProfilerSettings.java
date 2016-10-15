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
package com.nextep.designer.sqlclient.ui.model;

import com.nextep.datadesigner.model.IObservable;

public interface IProfilerSettings extends IObservable {

	int getThreadCount();

	int getDuration();

	int getThreadStepDuration();

	void setThreadCount(int count);

	void setDuration(int time);

	void setThreadStepDuration(int time);

	boolean isFetching();

	void setFetching(boolean fetching);

	/**
	 * Defines if the SQL queries must be run in a random order.
	 * 
	 * @param randOrder <code>true</code> if the SQL queries must be run in a random order,
	 *        <code>false</code> otherwise.
	 */
	void setRandomOrder(boolean randOrder);

	/**
	 * Do the SQL queries must be run in a random order ?
	 * 
	 * @return <code>true</code> if the SQL queries must be run in a random order,
	 *         <code>false</code> otherwise.
	 */
	boolean isRandomOrder();

}
