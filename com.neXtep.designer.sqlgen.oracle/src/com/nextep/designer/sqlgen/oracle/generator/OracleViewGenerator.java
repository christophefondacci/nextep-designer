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
package com.nextep.designer.sqlgen.oracle.generator;

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
import com.nextep.designer.sqlgen.model.base.AbstractViewGenerator;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleViewGenerator extends AbstractViewGenerator {

	private static final Log LOGGER = LogFactory.getLog(OracleViewGenerator.class);

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IView view = (IView) model;
		final String viewName = getName(view);
		final String rawName = view.getName();
		final String viewDefinition = generateLineBreaksForSqlPlus(view.getSql());

		ISQLScript createScript = getSqlScript(viewName, view.getDescription(), ScriptType.VIEW);
		createScript.appendSQL(prompt("Creating view '" + rawName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		createScript.appendSQL("CREATE OR REPLACE VIEW ").appendSQL(viewName).appendSQL(" AS") //$NON-NLS-1$ //$NON-NLS-2$
				.appendSQL(NEWLINE);
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
		// s.appendSQL(NEWLINE + ") AS " + NEWLINE);
		createScript.appendSQL(GenerationHelper.removeLastStatementDelimiter(
				GenerationHelper.removeLastStatementDelimiter(viewDefinition, ";"), "/")); //$NON-NLS-1$ //$NON-NLS-2$
		createScript.appendSQL(getSQLCommandWriter().closeStatement());

		// Creating generation result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addAdditionScript(new DatabaseReference(view.getType(), rawName), createScript);

		// Building dependencies
		GenerationHelper.addSqlGenerationPreconditions(genResult, view.getName(), view,
				IElementType.getInstance(IView.TYPE_ID));

		return genResult;
	}

	/**
	 * Generates line breaks in the view SQL to comply with SQL*Plus line max
	 * length limitations. This method simply splits the SQL each time a
	 * standard SQL keyword or comma or parenthesis is found.
	 * 
	 * @param sql
	 *            the SQL to generate
	 * @return the SQL*Plus compliant SQL
	 */
	private String generateLineBreaksForSqlPlus(String sql) {
		if (sql != null && sql.length() > 2000) {
			/*
			 * This regular expression will search for any SELECT / FROM / WHERE
			 * / AND keyword that is not a substring of another word, and which
			 * is preceded by any word, with no line breaks between them. For
			 * each match, it will then put a line break between the word and
			 * the keyword.
			 */
			return sql.replaceAll("(\\w+[^\\r\\n\\w]+?)\\b(SELECT|FROM|WHERE|AND)\\b", "$1" //$NON-NLS-1$ //$NON-NLS-2$
					+ NEWLINE + "$2"); //$NON-NLS-1$
		}
		return sql;
	}

}
