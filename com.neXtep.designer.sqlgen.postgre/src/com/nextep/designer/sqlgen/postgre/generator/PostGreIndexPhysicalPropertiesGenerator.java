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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.sqlgen.postgre.generator;

import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class PostGreIndexPhysicalPropertiesGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IPhysicalProperties props = (IPhysicalProperties) model;
		final String tablespace = props.getTablespaceName();
		final ISQLScript s = CorePlugin.getTypedObjectFactory().create(ISQLScript.class);
		s.appendSQL("TABLESPACE " + tablespace); //$NON-NLS-1$
		final IGenerationResult result = GenerationFactory.createGenerationResult();
		result.addAdditionScript(
				new DatabaseReference(props.getType(), props.getParent().getName()), s);
		return result;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		return null;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return null;
	}

}
