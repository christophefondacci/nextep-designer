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
package com.nextep.datadesigner.sqlgen.strategies;

import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * @author Christophe Fondacci
 */
public class UserTypeDropStrategy extends DoDropStrategy {

	public UserTypeDropStrategy() {
		setName("Drop user-defined types");
		setDescription("Generator will add DROP statements to generation scripts "
				+ "when types no more exist in repository.");
	}

	@Override
	public String getId() {
		return getClass().getName();
	}

	@Override
	public IGenerationResult generateDrop(ISQLGenerator generator, Object modelToDrop) {
		return generator.doDrop(modelToDrop);
	}

}
