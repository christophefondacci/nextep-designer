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

import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.model.ISQLCommandWriter;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * @author Bruno Gautier
 */
public final class MSSQLCommandWriter implements ISQLCommandWriter {

	private final ISQLParser parser = SQLGenPlugin.getService(IGenerationService.class)
			.getSQLParser(DBVendor.MSSQL);
	private String NEWLINE;

	public MSSQLCommandWriter() {
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
	}

	@Override
	public String promptMessage(String message) {
		/*
		 * FIXME [BGA] Comments are disabled for now because the JDBC submitter cannot handle the
		 * "GO" keyword placed after each comment.
		 */
		// String msg = parser.getPromptCommand() + " N'" + message.replace("'", "''") + "'";
		// msg = msg + parser.getStatementDelimiter();
		// return msg;
		return ""; //$NON-NLS-1$
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

	/*
	 * The {@link ISQLParser#getColumnDefinitionEscaper()} method is not used here because it can
	 * not handle different start and end escape characters.
	 */
	@Override
	public String escapeDbObjectName(String columnName) {
		return "[" + columnName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
