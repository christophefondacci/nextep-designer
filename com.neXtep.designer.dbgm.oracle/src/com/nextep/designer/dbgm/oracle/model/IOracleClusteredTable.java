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

import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This class represents a table within a cluster. A clustered table mostly defines the concrete
 * table and column mappings from this table to the cluster columns.
 * 
 * @author Christophe
 */
public interface IOracleClusteredTable extends IdentifiedObject, IObservable, IReferenceable,
		ITypedObject {

	String TYPE_ID = "CLUSTER_TAB"; //$NON-NLS-1$

	/**
	 * @return the reference to the concrete table
	 */
	IReference getTableReference();

	/***
	 * Defines the reference to the table
	 * 
	 * @param ref table reference
	 */
	void setTableReference(IReference ref);

	/**
	 * @return the mapping of the cluster columns to the table columns
	 */
	Map<IReference, IReference> getColumnMappings();

	/**
	 * Maps a cluster column to a table column. Columns should have compatible or same datatypes.
	 * 
	 * @param clusterCol reference of the cluster column
	 * @param tableCol reference of the table column
	 */
	void setColumnReferenceMapping(IReference clusterCol, IReference tableCol);

	/**
	 * Retrieves the table column reference currently mapped to the specified cluster column
	 * reference.
	 * 
	 * @param clusterCol reference of the cluster column
	 * @return the reference of the mapped table column
	 * @see IOracleClusteredTable#getColumnMapping(IReference)
	 */
	IReference getColumnReferenceMapping(IReference clusterCol);

	/**
	 * This is a convenience method identical to
	 * {@link IOracleClusteredTable#getColumnReferenceMapping(IReference)} except that it returns
	 * the column itself rather than its reference.
	 * 
	 * @param clusterCol reference of the cluster column
	 * @return the mapped table's column
	 */
	IBasicColumn getColumnMapping(IReference clusterCol);

	/**
	 * Defines the cluster to which this clustered table belongs
	 * 
	 * @param cluster parent cluster
	 */
	void setCluster(IOracleCluster cluster);

	/**
	 * @return the parent cluster of this clustered table
	 */
	IOracleCluster getCluster();
}
