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
package com.nextep.designer.dbgm.services;

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataLine;
import com.nextep.designer.dbgm.model.IDataSet;

/**
 * The data service provides method to manipulate large data sets.
 * 
 * @author Christophe Fondacci
 */
public interface IDataService {

	/**
	 * Adds an array of datalines to the specified dataset.
	 * 
	 * @param handle storage handle to locate the underlying dataset storage structure
	 * @param set the {@link IDataSet} to add a data line to
	 * @param lines the {@link IDataLine} array to store
	 */
	void addDataline(IDataSet set, IDataLine... lines);

	/**
	 * Removes a dataline from the specified dataset
	 * 
	 * @param handle storage handle to locate the underlying dataset storage structure
	 * @param set the {@link IDataSet} to remove the line from
	 * @param line the {@link IDataLine} to remove from the dataset
	 */
	// void removeDataline(IStorageHandle handle, IDataSet set, IDataLine line);

	/**
	 * Fetches a dataset delta from repository information. The returned dataset will contain the
	 * differences. When comparing 2 versions of a data set, this method will be far more efficient
	 * than comparing them using the default dataset comparison method (which would work however),
	 * because the repository stores dataset in an incremental way, thus allowing to fastly compute
	 * deltas.
	 * 
	 * @param fromVersion lower version bound of the delta
	 * @param toVersion upper version bound of the delta
	 * @return a {@link IDataDelta} containing the data evolution between those 2 versions
	 */
	// IDataDelta fetchDatasetDeltaFromRepository(IVersionInfo fromVersion, IVersionInfo toVersion);

	/**
	 * Compares the 2 datasets and computes a delta which would be able to transform the source
	 * dataset into the target dataset.
	 * 
	 * @param source source {@link IDataSet} of the comparison
	 * @param target target {@link IDataSet} of the comparison
	 * @return
	 */
	IDataDelta computeDataSetDelta(IDataSet source, IDataSet target);

	/**
	 * Loads the data set contents from the repository to our local storage area. This method will
	 * typically be called before we make a comparison with another set of data or before user
	 * edition.
	 * 
	 * @param dataSet the {@link IDataSet} to load
	 * @param monitor an {@link IProgressMonitor} to report progress and check for cancellation
	 */
	void loadDataLinesFromRepository(IDataSet dataSet, IProgressMonitor monitor);

	/**
	 * Saves local dataset contents back into the repository.
	 * 
	 * @param dataSet the dataset to save back to the repository
	 */
	void saveDataLinesToRepository(IDataSet dataSet, IProgressMonitor monitor);

	/**
	 * Saves the specified delta to the repository
	 * 
	 * @param set the {@link IDataSet} to which this delta corresponds
	 * @param delta the {@link IDataDelta} to store
	 */
	void saveDataDeltaToRepository(IDataSet set, IDataDelta delta, IProgressMonitor monitor);

}
