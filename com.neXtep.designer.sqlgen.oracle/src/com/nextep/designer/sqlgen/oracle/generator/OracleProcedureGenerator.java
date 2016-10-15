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

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.oracle.OracleMessages;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleProcedureGenerator extends SQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(OracleProcedureGenerator.class);

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return generateFullSQL(result.getSource());
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		IProcedure proc = (IProcedure) model;
		final boolean isJava = (proc.getLanguageType() == LanguageType.JAVA);
		/*
		 * FIXME [BGA] The return type of the procedure is used to determine if
		 * it is a function or a procedure. Currently, this attribute is only
		 * filled by the Oracle capturer, as it is not persisted in the Nextep
		 * repository. So it will work in a DATABASE scope to drop functions
		 * that exist only in database, but the DROP statement generated in a
		 * REPOSITORY scope will still be wrong for functions.
		 */
		final IDatatype returnType = proc.getReturnType();
		ISQLScript s = new SQLScript(proc.getName(), proc.getDescription(), "", //$NON-NLS-1$
				isJava ? ScriptType.JAVA : ScriptType.PROC);
		s.appendSQL(prompt("Dropping " + (isJava ? "Java " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ (returnType != null ? "function" : "procedure") + " '" + proc.getName() + "'...")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		s.appendSQL("DROP " //$NON-NLS-1$
				+ (isJava ? "JAVA SOURCE " : (returnType != null ? "FUNCTION " : "PROCEDURE ")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ proc.getName());
		closeLastStatement(s);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(proc.getType(), proc.getName()), s);
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		IProcedure proc = (IProcedure) model;
		if (proc.getSQLSource() == null || "".equals(proc.getSQLSource().trim())) { //$NON-NLS-1$
			LOGGER.warn(MessageFormat.format(OracleMessages.getString("warnEmptyProcedure"), //$NON-NLS-1$
					proc.getName()));
			return null;
		}
		final boolean isJava = (proc.getLanguageType() == LanguageType.JAVA);
		ISQLScript s = new SQLScript(proc.getName(), proc.getDescription(), "", //$NON-NLS-1$
				isJava ? ScriptType.JAVA : ScriptType.PROC);
		s.appendSQL(prompt("Creating " + (isJava ? "Java " : "") + "procedure '" + proc.getName() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ "'...")); //$NON-NLS-1$
		s.appendSQL("CREATE OR REPLACE "); //$NON-NLS-1$
		if (isJava) {
			s.appendSQL("JAVA SOURCE NAMED \"" + proc.getName() + "\" AS " + NEWLINE //$NON-NLS-1$ //$NON-NLS-2$
					+ proc.getSQLSource().trim());
		} else {
			String decl = proc.getSQLSource().trim();
			if (decl.toLowerCase().startsWith("create")) { //$NON-NLS-1$
				decl = decl.substring(7);
			}
			s.appendSQL(decl);
		}
		closeLastStatement(s);
		s.appendSQL("SHOW ERRORS").appendSQL(NEWLINE); //$NON-NLS-1$

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(proc.getType(), proc.getName()), s);

		// Adding dependencies
		GenerationHelper.addSqlGenerationPreconditions(r, proc.getName(), proc,
				IElementType.getInstance(IProcedure.TYPE_ID),
				IElementType.getInstance(IPackage.TYPE_ID));
		return r;
	}

}
