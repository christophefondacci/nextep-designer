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

import java.sql.Types;
import java.util.List;

/**
 * Represents a row of SQL results as they were fetched from the underlying jdbc statement.
 * 
 * @author Christophe Fondacci
 */
public interface ISQLRowResult {

	/**
	 * Provides the list of values this row contains
	 * 
	 * @return the list of values for this row
	 */
	List<Object> getValues();

	void setValue(int index, Object value);

	/**
	 * Provides the list of SQL type integer constants for every column of this row
	 * 
	 * @return a list of {@link Types} constants of the columns of this row
	 */
	List<Integer> getSqlTypes();

	/**
	 * Sets the pending state of this row. A pending row is a row which has not yet been persisted
	 * in the database and which would require a SQL insert (rather than an update).
	 * 
	 * @param pending the new pending state of this row
	 */
	void setPending(boolean pending);

	/**
	 * Informs about the pending state of this row. A pending row is a row which has not yet been
	 * persisted in the database and which would require a SQL insert (rather than an update).
	 * 
	 * @return <code>true</code> if this row is pending and requires INSERT, else <code>false</code>
	 */
	boolean isPending();
}
