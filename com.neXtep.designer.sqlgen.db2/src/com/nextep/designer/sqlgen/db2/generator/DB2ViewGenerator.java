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
package com.nextep.designer.sqlgen.db2.generator;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.sql.ISelectStatement;
import com.nextep.designer.dbgm.sql.SelectStatement;
import com.nextep.designer.dbgm.sql.TableAlias;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.base.AbstractViewGenerator;

/**
 * @author Bruno Gautier
 */
public final class DB2ViewGenerator extends AbstractViewGenerator {

	private static final Log LOGGER = LogFactory.getLog(DB2ViewGenerator.class);

	// TODO [BGA] Replace this method by a call to a delegated JDBC view generator
	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IView view = (IView) model;
		final String viewName = view.getName();
		final IElementType viewType = view.getType();
		final String viewDefinition = view.getSql();

		ISQLScript createScript = getSqlScript(viewName, view.getDescription(), ScriptType.VIEW);
		createScript.appendSQL(prompt("Creating view '" + viewName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		createScript.appendSQL("CREATE OR REPLACE VIEW ").appendSQL(viewName); //$NON-NLS-1$

		/*
		 * If view column aliases have been captured, it means they have been explicitly defined at
		 * creation time, so we need to generate the column aliases list clause.
		 */
		List<String> aliases = view.getColumnAliases();
		if (aliases != null && aliases.size() > 0) {
			createScript.appendSQL(" ("); //$NON-NLS-1$
			/*
			 * We cannot use the IBasicColumn#getRank() to check for the column rank since there is
			 * no guarantee that the rank will start at 0.
			 */
			int colRank = 0;
			for (String alias : aliases) {
				if (colRank++ > 0)
					createScript.appendSQL(", "); //$NON-NLS-1$
				createScript.appendSQL(alias);
			}
			createScript.appendSQL(")"); //$NON-NLS-1$
		}

		createScript
				.appendSQL(" AS") //$NON-NLS-1$
				.appendSQL(NEWLINE)
				.appendSQL("    ") //$NON-NLS-1$
				.appendSQL(
						GenerationHelper.removeLastStatementDelimiter(viewDefinition, getParser()
								.getStatementDelimiter()));
		closeLastStatement(createScript);

		// Creating generation result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addAdditionScript(new DatabaseReference(viewType, viewName), createScript);

		// Building dependencies
		try {
			ISelectStatement stmt = new SelectStatement(viewDefinition);
			List<TableAlias> refs = stmt.getFromTables();

			for (TableAlias t : refs) {
				genResult.addPrecondition(new DatabaseReference(viewType, t.getTableName()));
			}
		} catch (ErrorException e) {
			LOGGER.warn("Couldn't generate view dependencies", e);
		}

		return genResult;
	}

}
