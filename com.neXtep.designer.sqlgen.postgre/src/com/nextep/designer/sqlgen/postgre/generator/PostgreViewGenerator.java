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
package com.nextep.designer.sqlgen.postgre.generator;

import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PostgreViewGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return generateFullSQL(result.getSource());
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		IView view = (IView) model;
		// Generating drop statement
		ISQLScript s = new SQLScript(ScriptType.VIEW);
		s.appendSQL(prompt("Dropping view '" + view.getName() + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		s.appendSQL("DROP VIEW " + view.getName()); //$NON-NLS-1$
		s.appendSQL(getSQLCommandWriter().closeStatement());

		// Creating result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(view.getType(), view.getName()), s);

		// Returning drop result
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IView view = (IView) model;
		final String viewName = getName(view);

		ISQLScript s = new SQLScript(ScriptType.VIEW);
		s.setSql(prompt("Creating view '" + viewName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		s.appendSQL("CREATE OR REPLACE VIEW " + viewName); //$NON-NLS-1$

		// Generating view select statement
		s.appendSQL(" AS ").appendSQL(NEWLINE); //$NON-NLS-1$
		s.appendSQL(GenerationHelper.removeLastStatementDelimiter(view.getSql(), getParser()
				.getStatementDelimiter()));
		s.appendSQL(getSQLCommandWriter().closeStatement());

		// Creating generation result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(view.getType(), view.getName()), s);

		// Building dependencies
		GenerationHelper.addSqlGenerationPreconditions(r, view.getName(), view,
				IElementType.getInstance(IView.TYPE_ID));

		return r;
	}
}
