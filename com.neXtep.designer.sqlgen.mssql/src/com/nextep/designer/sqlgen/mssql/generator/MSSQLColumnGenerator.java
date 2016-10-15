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
package com.nextep.designer.sqlgen.mssql.generator;

import java.math.BigDecimal;
import java.util.List;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.UnsupportedDatatypeException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.generic.generator.ColumnGenerator;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * A little extension to the default column generation handling some DIFF cases properly. It handles
 * column rename and column alteration using the SQL-Server T-SQL syntax.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MSSQLColumnGenerator extends SQLGenerator {

	private static final List<String> BASE_TYPES = DBGMHelper.getDatatypeProvider(DBVendor.MSSQL)
			.listSupportedDatatypes();
	private static final String IDENTITY_PROPERTY = "IDENTITY"; //$NON-NLS-1$

	private final ISQLGenerator jdbcGenerator;

	public MSSQLColumnGenerator() {
		jdbcGenerator = GeneratorFactory.getGenerator(
				IElementType.getInstance(IBasicColumn.TYPE_ID), DBVendor.JDBC);
		jdbcGenerator.setVendor(DBVendor.MSSQL);
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IBasicColumn column = (IBasicColumn) model;
		final String colName = column.getName();
		final IDatatype colDatatype = new Datatype(DBGMHelper.getDatatype(DBVendor.MSSQL,
				column.getDatatype()));
		final String colDatatypeName = colDatatype.getName();
		final int colDatatypeLength = colDatatype.getLength();

		ISQLScript script = getSqlScript(colName, column.getDescription(), ScriptType.CUSTOM);

		script.appendSQL(escape(colName)).appendSQL(" "); //$NON-NLS-1$

		if (!colDatatypeName.contains(IDENTITY_PROPERTY)) {
			// Escaping the data type label when the IDENTITY property is not set
			colDatatype.setName(escape(colDatatypeName));

			// Handling user-defined data types. These types must not define a length.
			if (!BASE_TYPES.contains(colDatatypeName)) {
				colDatatype.setLength(-1);
				colDatatype.setPrecision(-1);
			}
		}

		/*
		 * Handling data types maximum values. If a column has a data type that can have a maximum
		 * value, we set the length to "max" if no value has been specified or if the data type
		 * maximum value has been set (for captured columns for example).
		 */
		String datatypeLabel = ""; //$NON-NLS-1$
		try {
			BigDecimal datatypeMaxSize = DBGMHelper.getDatatypeProvider(DBVendor.MSSQL)
					.getDatatypeMaxSize(colDatatypeName);
			if (colDatatypeLength == -1
					|| datatypeMaxSize.equals(new BigDecimal(colDatatypeLength))) {
				datatypeLabel = colDatatype.getName() + "(max)"; //$NON-NLS-1$
			}
		} catch (UnsupportedDatatypeException udte) {
			// The column's data type has no maximum value, we leave the data type label empty so
			// the standard data type label is generated
		}
		if ("".equals(datatypeLabel)) { //$NON-NLS-1$
			datatypeLabel = DBGMHelper.getDatatypeLabel(colDatatype);
		}
		script.appendSQL(datatypeLabel);

		String defaultExpr = column.getDefaultExpr();
		if (defaultExpr != null && !"".equals(defaultExpr.trim())) { //$NON-NLS-1$
			script.appendSQL(" DEFAULT ").appendSQL(defaultExpr); //$NON-NLS-1$
		}

		// We explicitly append NULL to column definition to avoid implicit definition for some
		// data types like SYSNAME.
		script.appendSQL((column.isNotNull() ? " NOT" : "")).appendSQL(" NULL"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		IGenerationResult genResult = GenerationFactory.createGenerationResult(colName);
		genResult.addAdditionScript(
				new DatabaseReference(column.getType(), ColumnGenerator.getDbRefName(column)),
				script);

		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		return jdbcGenerator.doDrop(model);
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IBasicColumn newColumn = (IBasicColumn) result.getSource();
		final IBasicColumn oldColumn = (IBasicColumn) result.getTarget();
		final IGenerationResult genResult = GenerationFactory.createGenerationResult();

		// Renaming column using T-SQL functions
		if (isRenamed(result)) {
			ISQLScript renameScript = getSqlScript(newColumn.getName(), newColumn.getDescription(),
					ScriptType.TABLE);
			renameScript.appendSQL("EXEC sp_rename '") //$NON-NLS-1$
					.appendSQL(CaptureHelper.getUniqueColumnName(oldColumn)).appendSQL("', '") //$NON-NLS-1$
					.appendSQL(CaptureHelper.getUniqueColumnName(newColumn))
					.appendSQL("', 'COLUMN'"); //$NON-NLS-1$
			closeLastStatement(renameScript);
			genResult.addDropScript(
					new DatabaseReference(oldColumn.getType(), CaptureHelper
							.getUniqueColumnName(oldColumn)), renameScript);
		}

		// The column data type alteration section
		if (!isRenamedOnly(result)) {
			final IDatatype newDatatype = newColumn.getDatatype();
			final IDatatype oldDatatype = oldColumn.getDatatype();
			final String newDefaultExpr = newColumn.getDefaultExpr();
			final String oldDefaultExpr = oldColumn.getDefaultExpr();
			final boolean newNotNull = newColumn.isNotNull();
			final boolean oldNotNull = oldColumn.isNotNull();

			// We need to check if something changed in the column definition before generating the
			// alter script. If only the column comment changed, we do not generate anything.
			if (!newDatatype.equals(oldDatatype)
					|| !notNull(newDefaultExpr).equals(notNull(oldDefaultExpr)) || newNotNull
					^ oldNotNull) {
				final String newDatatypeName = newDatatype.getName();
				final String oldDatatypeName = oldDatatype.getName();

				// Columns can't be altered when the IDENTITY property is set, except for changing
				// the seed value.
				if (oldDatatypeName.contains(IDENTITY_PROPERTY)
						|| newDatatypeName.contains(IDENTITY_PROPERTY)) {
					final int newDatatypeLength = newDatatype.getLength();
					final int oldDatatypeLength = oldDatatype.getLength();
					final int newDatatypePrecision = newDatatype.getPrecision();
					final int oldDatatypePrecision = oldDatatype.getPrecision();

					// If only the seed value has changed, we try to re-seed the IDENTITY property.
					if (oldDatatypeName.contains(IDENTITY_PROPERTY)
							&& newDatatypeName.contains(IDENTITY_PROPERTY)
							&& newDatatypeLength != oldDatatypeLength
							&& newDatatypePrecision == oldDatatypePrecision) {
						final ISQLScript reseedScript = getSqlScript(newColumn.getName(),
								newColumn.getDescription(), ScriptType.TABLE);
						reseedScript
								.appendSQL(prompt("Changing the current identity value in the '" //$NON-NLS-1$
										+ newColumn.getName() + "' column...")); //$NON-NLS-1$
						reseedScript.appendSQL("DBCC CHECKIDENT ('") //$NON-NLS-1$
								.appendSQL(CaptureHelper.getUniqueColumnName(newColumn))
								.appendSQL("', RESEED, ").appendSQL(newDatatypeLength) //$NON-NLS-1$
								.appendSQL(")"); //$NON-NLS-1$
						closeLastStatement(reseedScript);

						genResult.addDropScript(new DatabaseReference(newColumn.getType(),
								CaptureHelper.getUniqueColumnName(newColumn)), reseedScript);
					} else {
						// Otherwise we drop/re-create the column
						final ISQLScript dropScript = getSqlScript(oldColumn.getName(),
								oldColumn.getDescription(), ScriptType.TABLE);
						IGenerationResult dropColGenResult = doDrop(oldColumn);
						// Iterating for safety, should always be 1 and only 1 script in there
						for (ISQLScript colScript : dropColGenResult.getDrops()) {
							dropScript.appendScript(colScript);
						}
						genResult.addDropScript(new DatabaseReference(newColumn.getType(),
								CaptureHelper.getUniqueColumnName(newColumn)), dropScript);

						genResult.integrate(generateFullSQL(newColumn));
					}
				} else {
					// Generating the alter column SQL
					final ISQLScript alterScript = getSqlScript(newColumn.getName(),
							newColumn.getDescription(), ScriptType.TABLE);
					alterScript.appendSQL("ALTER COLUMN "); //$NON-NLS-1$
					// Appending the full column declaration
					IGenerationResult fullColGeneration = generateFullSQL(newColumn);
					// Iterating for safety, should always be 1 and only 1 script in there
					for (ISQLScript colScript : fullColGeneration.getAdditions()) {
						alterScript.appendScript(colScript);
					}

					genResult.addUpdateScript(new DatabaseReference(newColumn.getType(),
							CaptureHelper.getUniqueColumnName(newColumn)), alterScript);
				}
			}
		}

		return genResult;
	}

}
