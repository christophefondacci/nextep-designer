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
package com.nextep.designer.synch.services;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This service provides features to capture data from a database.
 * 
 * @author Christophe Fondacci
 */
public interface IDataCaptureService {

	/**
	 * This method captures the data of the specified tables collection using the provided
	 * connection. Data will be returned as a collection of {@link IDataSet}.
	 * 
	 * @param c the {@link IConnection} to fetch table data from
	 * @param tablesToCapture collection of tables to capture
	 * @return a collection of captured dataset
	 */
	Collection<IVersionable<IDataSet>> captureTablesData(IConnection c,
			Collection<IBasicTable> tablesToCapture, IProgressMonitor monitor);
}
