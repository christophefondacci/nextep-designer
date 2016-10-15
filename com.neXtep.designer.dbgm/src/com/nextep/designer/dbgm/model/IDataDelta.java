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
package com.nextep.designer.dbgm.model;

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * A delta is defined by a <i>from</i> version and a <i>to</i> version which defines a version range
 * of a data set : <code>[fromVersion , toVersion]</code>. A data delta thus provides incremental
 * information about data evolutions within this version range:<br>
 * - Added data<br>
 * - Updated data<br>
 * - Deleted data<br>
 * <br>
 * All set of data is provided through a {@link IDataSet}.
 * 
 * @author Christophe Fondacci
 */
public interface IDataDelta extends IReferenceable {

	/**
	 * Retrieves the starting version of the delta.
	 * 
	 * @return the lower bound of the version range
	 */
	IVersionInfo getFromVersion();

	/**
	 * Defines the starting version of the delta
	 * 
	 * @param fromVersion the lower bound of the version range
	 */
	void setFromVersion(IVersionInfo fromVersion);

	/**
	 * Retrieves the ending version of the delta.
	 * 
	 * @return the upper bound of the version range
	 */
	IVersionInfo getToVersion();

	/**
	 * Defines the ending version of the delta
	 * 
	 * @param toVersion the upper bound of the version range
	 */
	void setToVersion(IVersionInfo toVersion);

	/**
	 * Returns the data set informing about data which had been added in this delta.
	 * 
	 * @return the {@link IDataSet} of added data
	 */
	IDataSet getAddedDataSet();

	/**
	 * Defines the "addition" data set for this data delta
	 * 
	 * @param addedDataSet the {@link IDataSet} containing added data of this delta
	 */
	void setAddedDataSet(IDataSet addedDataSet);

	/**
	 * Returns the data set informing about data which had been updated in this delta
	 * 
	 * @return the {@link IDataSet} of updated data
	 */
	IDataSet getUpdatedDataSet();

	/**
	 * Defines the "update" data set for this data delta
	 * 
	 * @param addedDataSet the {@link IDataSet} containing updated data of this delta
	 */
	void setUpdatedDataSet(IDataSet updatedDataSet);

	/**
	 * Returns the data set informing about data which had been deleted in this delta
	 * 
	 * @return the {@link IDataSet} of deleted data
	 */
	IDataSet getDeletedDataSet();

	/**
	 * Defines the "deletion" data set for this data delta
	 * 
	 * @param deletedDataSet the {@link IDataSet} containing deleted data of this delta
	 */
	void setDeletedDataSet(IDataSet deletedDataSet);

	/**
	 * Informs about the difference type contained in thsi data delta
	 * 
	 * @return a {@link DifferenceType} computed from data comparison
	 */
	DifferenceType getDifferenceType();

	/**
	 * Computes the difference type of this data set by adding the specified difference. By default,
	 * a delta has a no state. Calling this method will automatically adjust the difference type of
	 * this delta. For example : <br>
	 * - if the delta is in EQUALS state and a call with MISSING_TARGET it will be switched to
	 * DIFFER<br>
	 * - if the delta is in MISSING_TARGET state and calling with MISSING_SOURCE will switch to
	 * DIFFER<br>
	 * 
	 * @param type the new difference type to compute against the current difference type of the
	 *        delta
	 */
	void computeDifferenceType(DifferenceType type);

	/**
	 * Indicates whether this data delta has at least one added line. This information cannot be
	 * obtained easily elsewhere since this would require to query the local storage to get data
	 * lines contained in the addition dataset.
	 * 
	 * @return <code>true</code> if there is additions, else <code>false</code>
	 */
	boolean hasAdditions();

	/**
	 * Indicates whether this data delta has at least one updated line. This information cannot be
	 * obtained easily elsewhere since this would require to query the local storage to get data
	 * lines contained in the update dataset.
	 * 
	 * @return <code>true</code> if there is updates, else <code>false</code>
	 */
	boolean hasUpdates();

	/**
	 * Indicates whether this data delta has at least one deleted line. This information cannot be
	 * obtained easily elsewhere since this would require to query the local storage to get data
	 * lines contained in the deleted dataset.
	 * 
	 * @return <code>true</code> if there is deletions, else <code>false</code>
	 */
	boolean hasDeletions();
}
