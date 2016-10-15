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
 * Default SQL output writer providing common default behaviour. A vendor needs to be specified so
 * that the writer will be connected to the vendor-specific parser.
 * 
 * @author Bruno Gautier
 * @author Christophe Fondacci
 */
public final class DefaultSQLCommandWriter implements ISQLCommandWriter {

	private ISQLParser parser;
	private String NEWLINE;

	public DefaultSQLCommandWriter() {
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
	}

	public DefaultSQLCommandWriter(DBVendor vendor) {
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
		this.parser = SQLGenPlugin.getService(IGenerationService.class).getSQLParser(vendor);
	}

	@Override
	public String promptMessage(String message) {
		final String promptCmd = parser.getPromptCommand();
		// We handle the situation when a parser may not define a prompt command
		// In this case we will prompt nothing (empty string)
		if (promptCmd != null && !"".equals(promptCmd.trim())) { //$NON-NLS-1$
			return promptCmd + " " + message + NEWLINE; //$NON-NLS-1$
		} else {
			return ""; //$NON-NLS-1$
		}
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
		return parser.getStatementDelimiter() + NEWLINE;
	}

	@Override
	public String escapeDbObjectName(String columnName) {
		return parser.getColumnDefinitionEscaper() + columnName
				+ parser.getColumnDefinitionEscaper();
	}

}
