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

import com.nextep.datadesigner.dbgm.model.IProcedure;
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
 */
public class MySQLProcedureGenerator extends SQLGenerator {

	// private static final Log log =
	// LogFactory.getLog(MySQLProcedureGenerator.class);
	/**
	 * @see com.nextep.datadesigner.sqlgen.impl.SQLGenerator#generateDiff(com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// Dropping the target
		IGenerationResult r = doDrop(result.getTarget());
		// Regenerating from source
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#doDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult doDrop(Object model) {
		IProcedure p = (IProcedure) model;

		ISQLScript s = new SQLScript(p.getName(), p.getDescription(), "", ScriptType.PROC);
		s.appendSQL("-- Dropping procedure '" + p.getName() + "'..." + NEWLINE);
		if ((p.getReturnType() != null && p.getReturnType().getName() != null && !p.getReturnType()
				.getName().trim().isEmpty())
				|| p.getSQLSource().toUpperCase().contains("FUNCTION")) {
			s.appendSQL("DROP FUNCTION " + escape(p.getName()) + ";" + NEWLINE);
		} else {
			s.appendSQL("DROP PROCEDURE " + escape(p.getName()) + ";" + NEWLINE);
		}

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(p.getType(), p.getName()), s);

		return r;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateFullSQL(java.lang.Object)
	 */
	@Override
	public IGenerationResult generateFullSQL(Object model) {
		IProcedure p = (IProcedure) model;
		if (p.getSQLSource() == null)
			return null;
		ISQLScript s = new SQLScript(p.getName(), p.getDescription(), "", ScriptType.TRIGGER);
		s.appendSQL("-- Creating procedure '" + p.getName() + "'..." + NEWLINE);
		// Changing delimiter
		s.appendSQL("DELIMITER |;" + NEWLINE);
		String sql = p.getSQLSource();

		// Appending SQL source code
		if (sql.toUpperCase().trim().startsWith("CREATE")) {
			s.appendSQL(p.getSQLSource().trim() + NEWLINE);
		} else {
			s.appendSQL("create " + sql.trim() + NEWLINE);
		}
		s.appendSQL("|;" + NEWLINE);
		s.appendSQL("DELIMITER ;" + NEWLINE + NEWLINE);

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(p.getType(), p.getName()), s);

		// Building dependencies
		GenerationHelper.addSqlGenerationPreconditions(r, p.getName(), p,
				IElementType.getInstance(IProcedure.TYPE_ID));

		return r;
	}

	/**
	 * Removes any multi line comment from the given sql string. Multiline
	 * comments are any comment which starts with "/*" and ends with reversed
	 * sequence.
	 * 
	 * @param sql
	 *            the sql string to remove comments from
	 * @return the "cleaned" sql string
	 */
	private String removeMultiLineComment(String sql) {
		while (sql.indexOf("/*") >= 0) {
			int offset = sql.indexOf("/*");
			int end = sql.indexOf("*/", offset + 1);
			sql = sql.substring(0, offset) + sql.substring(end + 2);
		}
		return sql;
	}
}
