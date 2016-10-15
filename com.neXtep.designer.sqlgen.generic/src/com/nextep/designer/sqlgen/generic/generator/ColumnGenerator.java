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
package com.nextep.designer.sqlgen.generic.generator;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * JDBC-generic SQL Generator for columns.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class ColumnGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// Not supporting column alters by default
		return GenerationFactory.createGenerationResult();
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IBasicColumn column = (IBasicColumn) model;
		final String colName = column.getName();

		ISQLScript dropScript = getSqlScript(colName, column.getDescription(), ScriptType.DROP);
		dropScript.appendSQL(prompt("Dropping column '" + colName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("ALTER TABLE " + escape(column.getParent().getName()) //$NON-NLS-1$
				+ " DROP COLUMN " + escape(colName)); //$NON-NLS-1$
		closeLastStatement(dropScript);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(column.getType(), getDbRefName(column)),
				dropScript);

		return genResult;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IBasicColumn c = (IBasicColumn) model;

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult(c.getName());
		r.addAdditionScript(new DatabaseReference(c.getType(), getDbRefName(c)), buildScript(c));
		return r;
	}

	private ISQLScript buildScript(IBasicColumn column) {
		final String colName = column.getName();

		ISQLScript script = getSqlScript(colName, column.getDescription(), ScriptType.CUSTOM);

		script.appendSQL(escape(colName)).appendSQL(" "); //$NON-NLS-1$
		script.appendSQL(DBGMHelper.getDatatypeLabel(DBGMHelper.getDatatype(getVendor(),
				column.getDatatype())));

		String defaultExpr = column.getDefaultExpr();
		if (defaultExpr != null && !"".equals(defaultExpr.trim())) { //$NON-NLS-1$
			script.appendSQL(" DEFAULT ").appendSQL(defaultExpr); //$NON-NLS-1$
		}

		if (column.isNotNull()) {
			script.appendSQL(" NOT NULL"); //$NON-NLS-1$
		}

		return script;
	}

	public static String getDbRefName(IBasicColumn c) {
		return c.getParent().getName() + "." + c.getName(); //$NON-NLS-1$
	}

}
