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
package com.nextep.datadesigner.dbgm.model;

import java.util.Collection;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.IDataSet;

/**
 * This interface describes a basic table object. A basic table is a database object. It is a model
 * representation of a database physical table.
 * 
 * @author Christophe_Fondacci
 */
public interface IBasicTable extends IDatabaseObject<IBasicTable>, ITriggable, IColumnable {

	String TYPE_ID = "TABLE"; //$NON-NLS-1$

	/**
	 * Adds a column to this table. Implementors must ensure that after a call to this method, the
	 * passed column will have its parent properties set to this table.
	 * 
	 * @param c column to add to this table
	 */
	@Override
	void addColumn(IBasicColumn c);

	/**
	 * Removes a column from this table. Implementors must ensure that after a call to this method,
	 * the "parent" property of the column must be reset to <code>null</code>
	 * 
	 * @param c column to remove from this column
	 */
	@Override
	void removeColumn(IBasicColumn c);

	/**
	 * Adds a constraint to this table
	 * 
	 * @param c constraint to add to this table
	 */
	void addConstraint(IKeyConstraint c);

	/**
	 * Removes the specified constraint from this table. Nothing should happen if the given
	 * constraint does not exists.
	 * 
	 * @param c constraint to remove from this table
	 */
	void removeConstraint(IKeyConstraint c);

	/**
	 * @return all the constraints defined for this table
	 */
	Collection<IKeyConstraint> getConstraints();

	/**
	 * Defines the short name of a table. This short name will be used for key / index naming.
	 * 
	 * @param shortName new short name for this table
	 */
	void setShortName(String shortName);

	/**
	 * @return the short name of this table
	 */
	String getShortName();

	/**
	 * Adds a data set associated to this table.
	 * 
	 * @param dataSet data set associated to this table
	 * @see IDataSet
	 */
	void addDataSet(IDataSet dataSet);

	/**
	 * Removes the data set from this table
	 * 
	 * @param dataSet data set to remove
	 */
	void removeDataSet(IDataSet dataSet);

	/**
	 * @return the collection of all data sets defined for this table
	 */
	Collection<IDataSet> getDataSets();

	/**
	 * Adds the specified index to the table
	 * 
	 * @param index index to add to this table
	 */
	void addIndex(IIndex index);

	/**
	 * Removes the specified index from the table
	 * 
	 * @param index idnex to remove from this table
	 */
	void removeIndex(IIndex index);

	/**
	 * @return the collection of all indexes defines for this table
	 */
	Collection<IIndex> getIndexes();

	/**
	 * A temporary workaround for a cluster bug with column references mapping
	 * 
	 * @param col affected column
	 * @param oldRef column old reference
	 * @param newRef column new reference
	 * @deprecated remove quickly, switch to instance/reference based cluster column mappings, need
	 *             to migrate repository
	 */
	@Deprecated
	void internalColumnRefChanged(IBasicColumn col, IReference oldRef, IReference newRef);

	/**
	 * Indicates whether this table is temporary or not
	 * 
	 * @return <code>true</code> if the table is temporary, else <code>false</code>
	 */
	boolean isTemporary();

	/**
	 * Defines the temporary state of this table.
	 * 
	 * @param temporary set to <code>true</code> to make this table temporary
	 */
	void setTemporary(boolean temporary);
}
