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

/**
 * This interface defines the structure of a listener for a {@link ISQLResult} instance.
 * 
 * @author Christophe Fondacci
 */
public interface ISQLResultListener {

	/**
	 * This method is called when rows are added to the result. Depending on the implementation,
	 * listeners may receive more than one rows at a time when the executor bulks the fetch.
	 * 
	 * @param result the {@link ISQLResult} which fired this notification
	 * @param rows the array of rows which had been added to the {@link ISQLResult}
	 */
	void rowsAdded(ISQLResult result, ISQLRowResult... rows);

	/**
	 * This method is called when rows are removed from the result. Depending on the implementation,
	 * listeners may receive more than one rows at a time when the executor bulks the fetch.
	 * 
	 * @param result the {@link ISQLResult} which fired this notification
	 * @param rows the array of rows which had been removed from the {@link ISQLResult}
	 */
	void rowsRemoved(ISQLResult result, ISQLRowResult... rows);
}
