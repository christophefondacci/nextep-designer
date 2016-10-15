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
package com.nextep.datadesigner.sqlgen.model;

import java.util.List;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.model.IRepositoryFile;

/**
 * This interface represents the generation of datafiles.
 * It is composed of a control header and of a set of all
 * files to generate.<br>
 * This class is the result of a generation which needs to
 * create data files.
 * 
 * @author Christophe
 *
 */
public interface IDatafileGeneration extends Comparable<IDatafileGeneration>{

	/**
	 * Defines the header string of the data file
	 * which indicates how the datafile can be loaded.
	 * 
	 * @param header control file header
	 */
	public void setControlFileHeader(String header);
	/**
	 * @return the control file header.
	 */
	public String getControlFileHeader();
	/**
	 * Adds a data file 
	 * @param file data file to add
	 */
	public void addDataFile(IRepositoryFile file);
	/**
	 * @return the list of all generated datafiles
	 */
	public List<IRepositoryFile> getDataFiles();
	/**
	 * Defines the datasets which created this generation
	 * @param set dataset
	 */
	public void setDataset(IDataSet set);
	/**
	 * @return the dataset whcihc generated this file
	 */
	public IDataSet getDataset();
}
