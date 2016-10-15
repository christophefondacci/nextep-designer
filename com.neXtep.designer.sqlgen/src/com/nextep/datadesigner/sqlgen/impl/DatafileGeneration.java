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
package com.nextep.datadesigner.sqlgen.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.sqlgen.model.IDatafileGeneration;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.model.IRepositoryFile;

public class DatafileGeneration implements IDatafileGeneration {

	/** Generated data files */ 
	private List<IRepositoryFile> files;
	/** Control string for SQL data load */
	private String controlString;
	/** Data set which generated these files */
	private IDataSet dataset;
	
	public DatafileGeneration(IDataSet dataset) {
		files = new ArrayList<IRepositoryFile>();
		setDataset(dataset);
	}
	@Override
	public void addDataFile(IRepositoryFile file) {
		this.files.add(file);
	}

	@Override
	public String getControlFileHeader() {
		return controlString;
	}

	@Override
	public List<IRepositoryFile> getDataFiles() {
		return files;
	}

	@Override
	public void setControlFileHeader(String header) {
		this.controlString = header;
	}
	@Override
	public int compareTo(IDatafileGeneration o) {
		final IBasicTable srcTab = getDataset().getTable();
		final IBasicTable tgtTab = o.getDataset().getTable();
		final Collection<IReferencer> refs = CorePlugin.getService(IReferenceManager.class).getReverseDependencies(srcTab);
		if(refs.contains(tgtTab)) {
			return 1;
		} else {
			return -1;
		}
	}
	@Override
	public IDataSet getDataset() {
		return dataset;
	}
	@Override
	public void setDataset(IDataSet set) {
		this.dataset = set;
	}

}
