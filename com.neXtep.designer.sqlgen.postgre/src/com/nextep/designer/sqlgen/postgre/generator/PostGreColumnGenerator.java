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

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PostGreColumnGenerator extends SQLGenerator {

	private final ISQLGenerator jdbcGenerator;

	public PostGreColumnGenerator() {
		jdbcGenerator = GeneratorFactory.getGenerator(
				IElementType.getInstance(IBasicColumn.TYPE_ID), DBVendor.JDBC);
		jdbcGenerator.setVendor(DBVendor.POSTGRE);
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		return jdbcGenerator.generateFullSQL(model);
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		return jdbcGenerator.doDrop(model);
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IBasicColumn newColumn = (IBasicColumn) result.getSource();
		final String newColName = newColumn.getName();
		final String newColDesc = newColumn.getDescription();
		final IBasicColumn oldColumn = (IBasicColumn) result.getTarget();

		IGenerationResult genResult = GenerationFactory.createGenerationResult(newColName);

		if (isRenamed(result)) {
			ISQLScript renameScript = getSqlScript(newColName, newColDesc, ScriptType.TABLE);
			renameScript
					.appendSQL("ALTER TABLE ").appendSQL(escape(newColumn.getParent().getName())) //$NON-NLS-1$
					.appendSQL(" RENAME COLUMN ").appendSQL(escape(oldColumn.getName())).appendSQL(" TO ") //$NON-NLS-1$ //$NON-NLS-2$
					.appendSQL(escape(newColName));
			renameScript.appendSQL(getSQLCommandWriter().closeStatement());

			genResult.addUpdateScript(getColumnDbRef(oldColumn), renameScript);
		}

		if (!isRenamedOnly(result)) {
			final String escapedColName = escape(newColName);
			ISQLScript alterScript = getSqlScript(newColName, newColDesc, ScriptType.TABLE);
			boolean changed = false;

			// Handling a change of column's type
			IDatatype newDataType = newColumn.getDatatype();
			if (!newDataType.equals(oldColumn.getDatatype())) {
				alterScript
						.appendSQL("ALTER COLUMN ").appendSQL(escapedColName) //$NON-NLS-1$
						.appendSQL(" TYPE ") //$NON-NLS-1$
						.appendSQL(
								DBGMHelper.getDatatypeLabel(DBGMHelper.getDatatype(getVendor(),
										newDataType)));
				changed = true;
			}

			// Handling a change of column's default value
			String newDfltExpr = notNull(newColumn.getDefaultExpr());
			if (!newDfltExpr.equals(notNull(oldColumn.getDefaultExpr()))) {
				chainCommands(alterScript, changed);
				alterScript.appendSQL("ALTER COLUMN ").appendSQL(escapedColName); //$NON-NLS-1$
				if ("".equals(newDfltExpr)) { //$NON-NLS-1$
					alterScript.appendSQL(" DROP DEFAULT"); //$NON-NLS-1$
				} else {
					alterScript.appendSQL(" SET DEFAULT ").appendSQL(newDfltExpr); //$NON-NLS-1$
				}
				changed = true;
			}

			// Handling a change of column's nullability
			if (newColumn.isNotNull() ^ oldColumn.isNotNull()) {
				chainCommands(alterScript, changed);
				alterScript.appendSQL("ALTER COLUMN ").appendSQL(escapedColName); //$NON-NLS-1$
				alterScript.appendSQL((newColumn.isNotNull() ? " SET" : " DROP")).appendSQL( //$NON-NLS-1$ //$NON-NLS-2$
						" NOT NULL"); //$NON-NLS-1$
				changed = true;
			}

			/*
			 * If at least one of the handled attributes has changed, we can contribute the
			 * generated script to the list of result scripts.
			 */
			if (changed) {
				genResult.addUpdateScript(getColumnDbRef(newColumn), alterScript);
			}
		}

		return genResult;
	}

	private void chainCommands(ISQLScript script, boolean hasChanged) {
		if (hasChanged) {
			script.appendSQL(NEWLINE).appendSQL(INDENTATION).appendSQL(","); //$NON-NLS-1$
		}
	}

	private DatabaseReference getColumnDbRef(IBasicColumn col) {
		final String colRefName = col.getParent().getName() + "." + col.getName(); //$NON-NLS-1$
		return new DatabaseReference(IElementType.getInstance(IBasicColumn.TYPE_ID), colRefName);
	}

	/**
	 * Convenience method to check if the specified column name correspond to a reserved key word of
	 * the current database vendor and escape it with the escape character of the current database
	 * vendor.
	 * 
	 * @param name the name of the column to check
	 * @return the escaped column name if it match with a reserved key word, the unchanged name
	 *         otherwise
	 */
	private String escapeColName(String name) {
		return getSQLCommandWriter().escapeDbObjectName(name);
	}

}
