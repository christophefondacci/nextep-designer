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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.model;

import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.model.IReference;

/**
 * @author Christophe Fondacci
 */
public interface IPartitionableTable extends IPartitionable {

	/**
	 * @return the references list of partitioning columns
	 */
	List<IReference> getPartitionedColumnsRef();

	/**
	 * Adds a column to the list of partitioning columns. Convenience method, deprecated due to
	 * unsafety.
	 * 
	 * @deprecated use {@link IOracleTablePhysicalProperties#addPartitionedColumnRef(IReference)}
	 * @param col column to add
	 */
	void addPartitionedColumn(IBasicColumn col);

	/**
	 * Removes a column from the list of partitioning columns. Convenience method, deprecated due to
	 * unsafety.
	 * 
	 * @deprecated use {@link IOracleTablePhysicalProperties#removePartitionedColumnRef(IReference)}
	 * @param col column to remove
	 */
	void removePartitionedColumn(IBasicColumn col);

	/**
	 * Adds a column to the list of partitioning columns.
	 * 
	 * @param col column to add
	 */
	void addPartitionedColumnRef(IReference colRef);

	/**
	 * Removes a column from the list of partitioning columns.
	 * 
	 * @param col column to remove
	 */
	void removePartitionedColumnRef(IReference colRef);
}
