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
package com.nextep.designer.sqlgen.model.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.model.ISQLCommandWriter;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * @author Bruno Gautier
 */
public final class OracleSQLCommandWriter implements ISQLCommandWriter {

	// Reserved words definition cache
	private static Collection<String> reservedWords = null;

	private final ISQLParser parser;
	private final String NEWLINE;

	public OracleSQLCommandWriter() {
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
		parser = SQLGenPlugin.getService(IGenerationService.class).getSQLParser(DBVendor.ORACLE);
	}

	@Override
	public String promptMessage(String message) {
		return parser.getPromptCommand() + " " + message + NEWLINE; //$NON-NLS-1$
	}

	@Override
	public String comment(String comment) {
		return parser.getCommentStartSequence() + " " + comment + NEWLINE; //$NON-NLS-1$
	}

	@Override
	public String exit() {
		return NEWLINE + parser.getExitCommand() + NEWLINE;
	}

	@Override
	public String closeStatement() {
		return NEWLINE + parser.getStatementDelimiter() + NEWLINE + NEWLINE;
	}

	@Override
	public String escapeDbObjectName(String columnName) {
		// Stategy is now to escape names only if they are in the reserved words. This is to prevent
		// from regressions of escaping everything

		// Initializing a collection of reserved words if not already set
		if (reservedWords == null) {
			final Collection<List<String>> allTokens = parser.getTypedTokens().values();
			reservedWords = new HashSet<String>();
			for (List<String> tokens : allTokens) {
				reservedWords.addAll(tokens);
			}
		}
		if (reservedWords.contains(columnName.toUpperCase())) {
			return parser.getColumnDefinitionEscaper() + columnName
					+ parser.getColumnDefinitionEscaper();
		} else {
			return columnName;
		}

	}

}
