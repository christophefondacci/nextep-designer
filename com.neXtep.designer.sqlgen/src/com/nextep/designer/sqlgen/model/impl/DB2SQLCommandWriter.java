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
public final class DB2SQLCommandWriter implements ISQLCommandWriter {

	private final ISQLParser parser = SQLGenPlugin.getService(IGenerationService.class)
			.getSQLParser(DBVendor.DB2);
	private String NEWLINE;

	public DB2SQLCommandWriter() {
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
	}

	@Override
	public String promptMessage(String message) {
		return parser.getPromptCommand() + " " + message + parser.getStatementDelimiter() //$NON-NLS-1$
				+ NEWLINE;
	}

	@Override
	public String comment(String comment) {
		return parser.getCommentStartSequence() + " " + comment + NEWLINE; //$NON-NLS-1$
	}

	@Override
	public String exit() {
		return NEWLINE + parser.getExitCommand() + parser.getStatementDelimiter() + NEWLINE;
	}

	@Override
	public String closeStatement() {
		return parser.getStatementDelimiter() + NEWLINE + NEWLINE;
	}

	@Override
	public String escapeDbObjectName(String columnName) {
		return parser.getColumnDefinitionEscaper() + columnName
				+ parser.getColumnDefinitionEscaper();
	}

}
