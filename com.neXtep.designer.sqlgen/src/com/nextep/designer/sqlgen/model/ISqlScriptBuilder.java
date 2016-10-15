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
package com.nextep.designer.sqlgen.model;

import java.util.List;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;

/**
 * A script builder is able to generate a SQL script from a {@link IGenerationResult} and might be
 * specific to a datanase vendor.<br>
 * This is a first draft to start cleaning up the messy {@link IGenerationResult} to
 * {@link ISQLScript} generation system and will evolve with the deliveries refactoring.
 * 
 * @author Christophe Fondacci
 */
public interface ISqlScriptBuilder {

	/**
	 * Generates the SQL scripts
	 * 
	 * @param result
	 * @return
	 */
	List<ISQLScript> buildScript(IGenerationResult result);
}
