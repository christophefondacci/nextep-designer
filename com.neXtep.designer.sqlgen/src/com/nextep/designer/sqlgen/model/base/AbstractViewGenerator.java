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
package com.nextep.designer.sqlgen.model.base;

import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Bruno Gautier
 */
public abstract class AbstractViewGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return generateFullSQL(result.getSource());
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IView view = (IView) model;
		final String viewName = view.getName();

		ISQLScript dropScript = getSqlScript(viewName, view.getDescription(), ScriptType.VIEW);
		dropScript.appendSQL(getSQLCommandWriter().promptMessage(
				"Dropping view '" + viewName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("DROP VIEW ").appendSQL(viewName); //$NON-NLS-1$
		dropScript.appendSQL(getSQLCommandWriter().closeStatement());

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(view.getType(), viewName), dropScript);

		return genResult;
	}

}
