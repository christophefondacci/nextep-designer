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
package com.nextep.designer.sqlgen.services;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.model.ErrorInfo;
import com.nextep.designer.sqlgen.model.ICapturer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This service provides method that can capture contents from a database {@link IConnection}
 * definition
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface ICaptureService {

	/**
	 * Fetches the structural contents of the database from the specified connection definition
	 * 
	 * @param conn the {@link IConnection} to fetch contents from
	 * @param monitor a {@link IProgressMonitor} to report progress to
	 * @return a collection of {@link IVersionable} instances wrapping the database model of the
	 *         captured structure
	 */
	Collection<IVersionable<?>> getContentsFromDatabase(IConnection conn, IProgressMonitor monitor);

	Collection<IVersionable<?>> getContentsForCompletion(IConnection conn, IProgressMonitor monitor);

	/**
	 * This method fetches errors from the specified connection and provide them to the caller
	 * through an error info bean.
	 * 
	 * @param conn the {@link IConnection} to fetch errors from
	 * @param monitor a {@link IProgressMonitor} to report progress
	 * @return a collection of {@link ErrorInfo}
	 */
	Collection<ErrorInfo> getErrorsFromDatabase(IConnection conn, IProgressMonitor monitor);

	/**
	 * Retrieves the capturer of a given vendor
	 * 
	 * @param vendor the {@link DBVendor} to retrieve the {@link ICapturer} for
	 * @return the corresponding {@link ICapturer}
	 */
	ICapturer getCapturer(DBVendor vendor);

}
