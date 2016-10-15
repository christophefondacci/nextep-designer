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
package com.nextep.designer.sqlgen.mysql.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.model.IElementType;
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
public class MySQLViewGenerator extends MySQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(MySQLViewGenerator.class);

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return generateFullSQL(result.getSource());
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IView view = (IView) model;

		// Generating drop statement
		ISQLScript s = getSqlScript(view.getName(), view.getDescription(), ScriptType.VIEW);
		s.appendSQL(prompt("Dropping view '" + view.getName() + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		s.appendSQL("DROP VIEW " + escape(view.getName())); //$NON-NLS-1$
		closeLastStatement(s);

		// Creating result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(view.getType(), view.getName()), s);

		// Returning drop result
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IView view = (IView) model;

		ISQLScript s = getSqlScript(view.getName(), view.getDescription(), ScriptType.VIEW);
		s.setSql(prompt("Creating view '" + view.getName() + "'..."));
		s.appendSQL("CREATE OR REPLACE VIEW " + escape(view.getName()));

		// Appending column aliases
		// boolean first = true;
		// for(String alias : view.getColumnAliases()) {
		// if(!first) {
		// s.appendSQL(", ");
		// } else {
		// s.appendSQL("     ");
		// first=false;
		// }
		// s.appendSQL(alias);
		// }

		// Generating view select statement
		s.appendSQL(" AS " + NEWLINE); //$NON-NLS-1$
		s.appendSQL(cleanSQL(view.getSql()));
		closeLastStatement(s);

		// Creating generation result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(view.getType(), view.getName()), s);

		// Building dependencies
		GenerationHelper.addSqlGenerationPreconditions(r, view.getName(), view,
				IElementType.getInstance(IView.TYPE_ID));

		return r;
	}

	private String cleanSQL(String sql) {
		String cleaned = sql.trim();
		if (cleaned.length() > 0) {
			char lastCar = cleaned.charAt(cleaned.length() - 1);
			if (lastCar == ';') {
				cleaned = cleaned.substring(0, cleaned.length() - 1);
			}
			return cleaned.trim();
		} else {
			return cleaned;
		}
	}

}
