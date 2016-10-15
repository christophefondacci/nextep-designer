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

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
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
public class OracleColumnGenerator extends SQLGenerator implements ISQLGenerator {

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IBasicColumn column = (IBasicColumn) model;
		final String colName = column.getName();

		IGenerationResult genResult = GenerationFactory.createGenerationResult(colName);
		ISQLScript createScript = getSqlScript(colName, column.getDescription(), ScriptType.TABLE);

		// We generate the full column definition with the nullable attribute
		appendColDef(createScript, column, true);

		genResult.addAdditionScript(new DatabaseReference(column.getType(), getDbRefName(column)),
				createScript);

		return genResult;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IBasicColumn newColumn = (IBasicColumn) result.getSource();
		final IBasicColumn oldColumn = (IBasicColumn) result.getTarget();
		final String newColName = newColumn.getName();
		final String newColDesc = newColumn.getDescription();
		final IElementType newColType = newColumn.getType();

		IGenerationResult genResult = GenerationFactory.createGenerationResult(newColName);

		if (isRenamed(result)) {
			ISQLScript renameScript = getSqlScript(newColName, newColDesc, ScriptType.TABLE);
			final String parentTabName = getName((IDatabaseObject<?>) newColumn.getParent());
			renameScript.appendSQL("ALTER TABLE ").appendSQL(parentTabName) //$NON-NLS-1$
					.appendSQL(" RENAME COLUMN ").appendSQL(escape(oldColumn.getName())).appendSQL(" TO ") //$NON-NLS-1$ //$NON-NLS-2$
					.appendSQL(escape(newColName));
			closeLastStatement(renameScript);

			genResult.addUpdateScript(new DatabaseReference(newColType, getDbRefName(oldColumn)),
					renameScript);
		}
		if (newColumn.isVirtual() != oldColumn.isVirtual()) {
			genResult.integrate(doDrop(oldColumn));
			ISQLScript createScript = getSqlScript(newColName, newColumn.getDescription(),
					ScriptType.TABLE);
			// We generate the full column definition with the nullable
			// attribute
			appendColDef(createScript, newColumn, true);
			genResult.addAdditionScript(new DatabaseReference(newColType, getDbRefName(newColumn)),
					createScript);
			return genResult;
		}

		if (!isRenamedOnly(result)) {
			final IDatatype newDatatype = newColumn.getDatatype();
			final IDatatype oldDatatype = oldColumn.getDatatype();
			final String newDefaultExpr = newColumn.getDefaultExpr();
			final String oldDefaultExpr = oldColumn.getDefaultExpr();
			final boolean newNotNull = newColumn.isNotNull();
			final boolean oldNotNull = oldColumn.isNotNull();

			// We need to check if something changed in the column definition
			// before generating the
			// alter script. If only the column comment changed, we do not
			// generate anything.
			if (!newDatatype.equals(oldDatatype)
					|| !notNull(newDefaultExpr).equals(notNull(oldDefaultExpr)) || newNotNull
					^ oldNotNull) {
				ISQLScript alterScript = getSqlScript(newColName, newColDesc, ScriptType.TABLE);

				// We generate a partial column definition without the nullable
				// attribute
				appendColDef(alterScript, newColumn, false);

				// Specific: NULL / NOT NULL should only be generated if this
				// attribute changed
				// within a MODIFY clause
				if (oldNotNull && !newNotNull) {
					alterScript.appendSQL(" NULL"); //$NON-NLS-1$
				} else if (!oldNotNull && newNotNull) {
					alterScript.appendSQL(" NOT NULL"); //$NON-NLS-1$
				}

				genResult.addUpdateScript(
						new DatabaseReference(newColType, getDbRefName(newColumn)), alterScript);
			}
		}

		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IBasicColumn column = (IBasicColumn) model;
		final String colName = column.getName();
		final String tabName = getName((IDatabaseObject<?>) column.getParent());
		final String rawTabName = column.getParent().getName();

		IGenerationResult genResult = GenerationFactory.createGenerationResult(colName);
		ISQLScript dropScript = getSqlScript(colName, column.getDescription(), ScriptType.TABLE);

		dropScript.appendSQL(prompt("Dropping column '" + colName + "' on table '" + rawTabName //$NON-NLS-1$ //$NON-NLS-2$
				+ "'...")); //$NON-NLS-1$
		dropScript.appendSQL("ALTER TABLE " + tabName + " DROP COLUMN " + escape(colName)); //$NON-NLS-1$ //$NON-NLS-2$
		closeLastStatement(dropScript);

		genResult
				.addDropScript(
						new DatabaseReference(column.getType(), OracleColumnGenerator
								.getDbRefName(column)), dropScript);
		return genResult;
	}

	public static String getDbRefName(IBasicColumn column) {
		return column.getParent().getName() + "." + column.getName(); //$NON-NLS-1$
	}

	private ISQLScript appendColDef(ISQLScript script, IBasicColumn column, boolean generateNotNull) {
		final String colName = escape(column.getName());
		final IDatatype colType = DBGMHelper.getDatatype(DBVendor.ORACLE, column.getDatatype());
		final String colDefaultExpr = column.getDefaultExpr();

		script.appendSQL(colName)
				.appendSQL(" ").appendSQL(DBGMHelper.getDatatypeLabel(colType, DBVendor.ORACLE)); //$NON-NLS-1$

		if (colDefaultExpr != null && !"".equals(colDefaultExpr.trim())) { //$NON-NLS-1$
			if (!column.isVirtual()) {
				script.appendSQL(" DEFAULT ").appendSQL(colDefaultExpr); //$NON-NLS-1$
			} else {
				script.appendSQL(" GENERATED ALWAYS AS (").appendSQL(colDefaultExpr).appendSQL(") VIRTUAL "); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		if (column.isNotNull() && generateNotNull && !column.isVirtual()) {
			script.appendSQL(" NOT NULL"); //$NON-NLS-1$
		}

		return script;
	}

}
