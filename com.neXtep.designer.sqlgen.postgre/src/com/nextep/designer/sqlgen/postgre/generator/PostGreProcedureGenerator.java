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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

public class PostGreProcedureGenerator extends SQLGenerator {

	private final static Log log = LogFactory.getLog(PostGreProcedureGenerator.class);

	/**
	 * @see com.nextep.datadesigner.sqlgen.impl.SQLGenerator#generateDiff(com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return generateFullSQL(result.getSource());
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#doDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult doDrop(Object model) {
		IProcedure p = (IProcedure) model;

		ISQLScript s = new SQLScript(p.getName(), p.getDescription(), "", ScriptType.PROC);
		s.appendSQL(prompt(" Dropping procedure '" + p.getName() + "'..."));
		if ((p.getReturnType() != null && p.getReturnType().getName() != null && !p.getReturnType()
				.getName().trim().isEmpty())
				|| p.getSQLSource().contains("FUNCTION")) {
			s.appendSQL("DROP FUNCTION " + p.getName() + getParser().getStatementDelimiter()
					+ NEWLINE);
		} else {
			s.appendSQL("DROP PROCEDURE " + p.getName() + getParser().getStatementDelimiter()
					+ NEWLINE);
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
		ISQLScript s = new SQLScript(p.getName(), p.getDescription(), "", ScriptType.PROC);
		s.appendSQL(prompt("Creating procedure '" + p.getName() + "'..."));
		String sql = p.getSQLSource().trim();
		// Removing comments
		// Appending SQL source code
		if (sql.toUpperCase().trim().startsWith("CREATE")) {
			s.appendSQL(p.getSQLSource() + NEWLINE);
		} else {
			s.appendSQL("CREATE OR REPLACE " + sql + NEWLINE);
		}
		if (sql.charAt(sql.length() - 1) != ';') {
			s.appendSQL(getParser().getStatementDelimiter() + NEWLINE);
		}
		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(p.getType(), p.getName()), s);

		// Adding dependencies
		GenerationHelper.addSqlGenerationPreconditions(r, p.getName(), p,
				IElementType.getInstance(IProcedure.TYPE_ID));

		return r;
	}

}
