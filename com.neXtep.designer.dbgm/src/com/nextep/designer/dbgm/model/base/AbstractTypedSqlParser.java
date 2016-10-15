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
package com.nextep.designer.dbgm.model.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.designer.dbgm.model.ITypedSqlParser;

/**
 * This abstract class provides default implementation for name parsing and
 * rename of a {@link IParseable} element based on a simple tag which should be
 * provided by the implementor. This tag corresponds to the word immediately
 * before the name of the element.<br>
 * <br>
 * For example, a procedure declaration can define the name position by
 * specifying "PROCEDURE" as the name delimiter tag since every declaration will
 * be construct like :<br>
 * <br>
 * <code>CREATE PROCEDURE <i>proc_name</i></code><br>
 * <br>
 * Any parser which is not that simple should reimplement fully the
 * {@link ITypedSqlParser} (i.e. Oracle package parsers) interface rather than
 * extending this class.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractTypedSqlParser implements ITypedSqlParser {

	@Override
	public String parseName(String sql) {
		// Extracting procedure or function name from the SQL source
		Pattern pattern = Pattern.compile(getNameRegExp());
		Matcher m = pattern.matcher(sql.toLowerCase());
		String name = null;
		if (m.find()) {
			name = sql.substring(m.start(2), m.end(2));
		}
		return name;
	}

	protected String getNameDelimiterEndTag() {
		return "\\s+((\\w)+)";
	}

	protected String getNameRegExp() {
		return "\\s*(" + getNameDelimiterTag().toLowerCase() + ")" + getNameDelimiterEndTag().toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String rename(String sqlToRename, String newName) {
		// Extracting procedure or function name from the SQL source
		Pattern pattern = Pattern.compile(getNameRegExp()); //$NON-NLS-1$ //$NON-NLS-2$
		Matcher m = pattern.matcher(sqlToRename.toLowerCase());
		// Looking for first occurrence
		String newSql = sqlToRename;
		if (m.find()) {
			final String oldName = sqlToRename.substring(m.start(2), m.end(2));
			// Quick fix for handling line feed regexp capture (postgres)
			String suffix = "";
			if(oldName.endsWith("\\r\\n")) {
				suffix = "\\r\\n";
			} else if (oldName.endsWith("\\n")) {
				suffix = "\\n";
			}
			// Building the "renamed" SQL source declaration
			newSql = sqlToRename.substring(0, m.start(2)) + newName + suffix
					+ sqlToRename.substring(m.end(2));
		}
		return newSql;
	}

	/**
	 * This method informs about the tag preceding the name information within
	 * the SQL source. Note that regular expressions are permitted which allows
	 * implementor to return "PROCEDURE|FUNCTION" in order to specify that the
	 * first tag could either be "PROCEDURE" <b>or</b> "FUNCTION".
	 * 
	 * @return the tag always preceding the name information of the SQL source,
	 *         as a regular expression
	 */
	protected abstract String getNameDelimiterTag();

	@Override
	public void rename(IParseable parseable, String newName) {
		final String renamed = rename(parseable.getSql(), newName);
		parseable.setSql(renamed);
	}

}
