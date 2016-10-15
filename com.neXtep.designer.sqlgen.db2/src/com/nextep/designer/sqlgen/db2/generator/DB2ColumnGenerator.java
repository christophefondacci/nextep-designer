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
 * The DB2 column generator which delegates regular full generation and full drop to the JDBC
 * generator and handles column alteration specifically for DB2.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class DB2ColumnGenerator extends SQLGenerator {

	private ISQLGenerator jdbcGenerator;

	public DB2ColumnGenerator() {
		jdbcGenerator = GeneratorFactory.getGenerator(
				IElementType.getInstance(IBasicColumn.TYPE_ID), DBVendor.JDBC);
		jdbcGenerator.setVendor(DBVendor.DB2);
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
		final IBasicColumn oldColumn = (IBasicColumn) result.getTarget();
		final String newColName = newColumn.getName();
		final IGenerationResult genResult = GenerationFactory.createGenerationResult(newColName);

		StringBuilder buf = new StringBuilder(50);
		boolean hasChanged = false;
		String separator = ""; //$NON-NLS-1$
		if (isRenamed(result)) {
			buf.append("RENAME ").append(oldColumn.getName()).append(" TO ").append(newColName); //$NON-NLS-1$ //$NON-NLS-2$
			hasChanged = true;
			separator = NEWLINE + INDENTATION;
		}

		if (!isRenamedOnly(result)) {
			// Comparing data types
			final IDatatype newDatatype = newColumn.getDatatype();
			final IDatatype oldDatatype = oldColumn.getDatatype();
			if (!newDatatype.equals(oldDatatype)) {
				buf.append(separator).append("ALTER COLUMN ").append(escape(newColName)) //$NON-NLS-1$
						.append(" SET DATA TYPE "); //$NON-NLS-1$
				buf.append(DBGMHelper.getDatatypeLabel(DBGMHelper.getDatatype(DBVendor.DB2,
						newDatatype)));
				separator = NEWLINE + INDENTATION;
				hasChanged = true;
			}

			// Comparing default values
			final String newDefaultExpr = newColumn.getDefaultExpr();
			final String oldDefaultExpr = oldColumn.getDefaultExpr();
			if (!notNull(newDefaultExpr).equals(notNull(oldDefaultExpr))) {
				buf.append(separator).append("ALTER COLUMN ").append(escape(newColName)); //$NON-NLS-1$
				if (!"".equals(notNull(newDefaultExpr).trim())) { //$NON-NLS-1$
					buf.append(" SET DEFAULT ").append(newDefaultExpr); //$NON-NLS-1$
				} else {
					buf.append(" DROP DEFAULT"); //$NON-NLS-1$
				}
				separator = NEWLINE + INDENTATION;
				hasChanged = true;
			}

			// Comparing not null flag
			final boolean newNotNull = newColumn.isNotNull();
			final boolean oldNotNull = oldColumn.isNotNull();
			if (newNotNull != oldNotNull) {
				buf.append(separator).append("ALTER COLUMN ").append(escape(newColName)); //$NON-NLS-1$
				if (newNotNull) {
					buf.append(" SET NOT NULL"); //$NON-NLS-1$
				} else {
					buf.append(" DROP NOT NULL"); //$NON-NLS-1$
				}
				hasChanged = true;
			}

			// Safety flag as DB2 does not handle the whole variety of column changes, we may
			// have differences which we could not generate
			if (hasChanged) {
				ISQLScript script = getSqlScript(newColName, newColumn.getDescription(),
						ScriptType.TABLE);
				script.appendSQL(buf.toString());
				genResult.addUpdateScript(getDbReference(newColumn), script);
			}
		}
		return genResult;
	}

	private DatabaseReference getDbReference(IBasicColumn col) {
		final String colRefName = col.getParent().getName() + "." + col.getName(); //$NON-NLS-1$
		return new DatabaseReference(IElementType.getInstance(IBasicColumn.TYPE_ID), colRefName);
	}

}
