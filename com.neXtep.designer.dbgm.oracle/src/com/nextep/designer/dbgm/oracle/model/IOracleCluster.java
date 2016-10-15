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
package com.nextep.designer.dbgm.oracle.model;

import java.util.Set;
import com.nextep.datadesigner.model.IReference;

/**
 * This class represents a cluster of several Oracle tables. A Oracle cluster is a physical table
 * which stores several logical tables.
 * 
 * @author Christophe
 */
public interface IOracleCluster extends IOracleTable {

	String CLUSTER_TYPE_ID = "CLUSTER"; //$NON-NLS-1$

	/**
	 * @return the list of tables in this table cluster
	 */
	Set<IOracleClusteredTable> getClusteredTables();

	/**
	 * Adds the specified table to this cluster
	 * 
	 * @param t table to add to the cluster
	 */
	IOracleClusteredTable addClusteredTable(IReference t);

	/**
	 * Removes the specified table from this cluster
	 * 
	 * @param t table to remove from the cluster
	 */
	void removeClusteredTable(IReference t);

	/**
	 * A helper method which retrieves the clustered table instance which wraps the specified table
	 * reference.
	 * 
	 * @param r reference of the table in the cluster
	 * @return the clustered table or <code>null</code> if no such table exists in this cluster.
	 */
	IOracleClusteredTable getClusteredTable(IReference r);

}
