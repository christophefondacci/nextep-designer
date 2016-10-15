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
/**
 *
 */
package com.nextep.datadesigner.sqlgen.impl;

import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.vcs.impl.ContainerMerger;
import com.nextep.datadesigner.vcs.impl.VersionContainer;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.impl.GenerationResult;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionContainer;

/**
 * @author Christophe Fondacci
 */
public class ContainerGenerator extends SQLGenerator implements ISQLGenerator {

	private static ContainerGenerator instance = null;

	public ContainerGenerator() {
	}

	public static ContainerGenerator getInstance() {
		if (instance == null) {
			instance = new ContainerGenerator();
		}
		return instance;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateFullSQL(java.lang.Object)
	 */
	@Override
	public IGenerationResult generateFullSQL(Object model) {
		VersionContainer c = (VersionContainer) model;
		final IGenerationService generationService = CorePlugin
				.getService(IGenerationService.class);
		// Initializing wrapper SQL script
		return generationService.batchGenerate(null, getVendor(), c.getName(), c.getDescription(),
				c.getContents());
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult doDrop(Object model) {
		// Cannot drop a container
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.impl.SQLGenerator#generateDiff(com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IVersionContainer source = (IVersionContainer) result.getSource();
		final IGenerationResult thisResult = new GenerationResult(source.getName());
		IGenerationResult contentsGeneration = generateChildren(ContainerMerger.CATEGORY_CONTENTS,
				result, null, true);
		thisResult.integrate(contentsGeneration);
		return thisResult;
	}

}
