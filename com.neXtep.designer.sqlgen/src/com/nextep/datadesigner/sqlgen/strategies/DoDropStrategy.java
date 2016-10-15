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
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * @author Christophe Fondacci
 */
public class DoDropStrategy extends NoDropStrategy {

	protected String NEWLINE;

	public DoDropStrategy() {
		setName("Drop");
		setDescription("Generator will create default drop statements.");
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
	}

	@Override
	public String getId() {
		return DoDropStrategy.class.getName();
	}

	@Override
	public IGenerationResult generateDrop(ISQLGenerator generator, Object modelToDrop,
			DBVendor vendor) {
		setVendor(vendor);
		return generateDrop(generator, modelToDrop);
	}

	public IGenerationResult generateDrop(ISQLGenerator generator, Object modelToDrop) {
		return generator.doDrop(modelToDrop);
	}

	@Override
	public boolean isDropping() {
		return true;
	}

}
