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

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.sqlgen.services.BuildResult;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * This interface is an abstraction of a "submitter" which is able
 * to submit some script / SQL to a given database.
 * 
 * @author Christophe
 *
 */
public interface IGenerationSubmitter {

	/**
	 * Submits the specified {@link ISQLScript} to the {@link IConnection} 
	 * provided.
	 */
	public abstract BuildResult submit(IProgressMonitor monitor,ISQLScript script, IConnection conn);
	/**
	 * Defines the console which will log the generation output
	 * @param console generation console
	 */
	public abstract void setConsole(IGenerationConsole console);
	/**
	 * @return the generation console which log the output
	 */
	public abstract IGenerationConsole getConsole();

	/**
	 * Defines the generation result which is at the origin of this
	 * generation. This helps the environment to keep track of what
	 * have been generated in a structured way.
	 * 
	 * @param result the {@link IGenerationResult} built by a {@link ISQLGenerator}
	 */
	public abstract void setGenerationResult(IGenerationResult result);
	/**
	 * @return the {@link IGenerationResult} at the origin of this 
	 * 			generation
	 */
	public abstract IGenerationResult getGenerationResult();
	/**
	 * @return the {@link DBVendor} for which this submitter has been designed
	 */
	public abstract DBVendor getVendor(); 
	
}
