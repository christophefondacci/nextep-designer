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
package com.nextep.designer.sqlgen.mysql.merger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.nextep.datadesigner.sqlgen.impl.merge.ProcedureMerger;
import com.nextep.designer.vcs.model.ComparisonScope;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MySQLProcedureMerger extends ProcedureMerger {

	/*
	 * FIXME [BGA] This method does not all handle all kinds of comments that are supported by MySQL
	 * server. Single line comments can also start with a "#" character.
	 */
	@Override
	protected String cleanSource(String source) {
		final String originalSource = super.cleanSource(source);

		// Removing all single carriage return characters
		String s = originalSource.replace("\r", ""); //$NON-NLS-1$ //$NON-NLS-2$

		// Removing all single line comments
		s = s.replaceAll("( |\t)*--(.)*(\n|\r)+", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		// Handling create procedure
		int firstLineFeed = s.indexOf('\n');
		int procIndex = s.toUpperCase().indexOf("PROCEDURE"); //$NON-NLS-1$
		if (procIndex >= 0 && procIndex < firstLineFeed) {
			s = "create procedure " + s.substring(procIndex + 9).trim(); //$NON-NLS-1$
		} else {
			int funcIndex = s.toUpperCase().indexOf("FUNCTION"); //$NON-NLS-1$
			s = "create function " + s.substring(funcIndex + 8).trim(); //$NON-NLS-1$
		}

		// Removing multiline comments (regexp generate stack overflow)
		int index = s.indexOf("/*"); //$NON-NLS-1$
		while (index != -1) {
			int end = s.indexOf("*/", index + 2); //$NON-NLS-1$
			s = s.substring(0, index) + ((end == -1) ? "" : s.substring(end + 2)); //$NON-NLS-1$

			index = s.indexOf("/*"); //$NON-NLS-1$
		}
		s = s.replaceAll("\n\\s+\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll("(\\s)+\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll("\n\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.trim();
		if (s.charAt(s.length() - 1) == ';') {
			s = s.substring(0, s.length() - 1);
		}

		/*
		 * Here we format the f***ing "READS SQL DATA" / "DETERMINISTIC" flags which MySQL
		 * translates from source to flags without offering any chance to retrieve the initial
		 * source. The formatting only serves comparison with DB.
		 */
		if (getMergeStrategy().getComparisonScope() != ComparisonScope.REPOSITORY) {
			// Flags are always defined BEFORE the first begin tag
			int startIndex = s.toUpperCase().indexOf("BEGIN"); //$NON-NLS-1$

			// Handle the case when the stored program is not made up of a compound statement
			if (startIndex == -1) {
				startIndex = s.length();
			}

			String temp = s.substring(0, startIndex);
			Pattern p = Pattern.compile("READS(\\s)+SQL(\\s)+DATA"); //$NON-NLS-1$
			Matcher m = p.matcher(temp.toUpperCase());
			boolean readsSqlData = false;
			if (m.find()) {
				readsSqlData = true;
				// Removing the declaration, we will add a proper one after
				temp = temp.substring(0, m.start())
						+ (m.end() >= temp.length() ? "" : temp.substring(m.end(), temp.length())); //$NON-NLS-1$
			}
			// Same for deterministic flag
			p = Pattern.compile("DETERMINISTIC"); //$NON-NLS-1$
			m = p.matcher(temp.toUpperCase());
			boolean deterministic = false;
			if (m.find()) {
				deterministic = true;
				// Removing the declaration, we will add a proper one after
				temp = temp.substring(0, m.start())
						+ (m.end() >= temp.length() ? "" : temp.substring(m.end(), temp.length())); //$NON-NLS-1$
			}
			if (readsSqlData) {
				temp = temp.trim() + "\nREADS SQL DATA\n"; //$NON-NLS-1$
			}
			if (deterministic) {
				temp = temp.trim() + "\nDETERMINISTIC\n"; //$NON-NLS-1$
			}
			s = temp + s.substring(startIndex).trim();
		}
		return s;
	}

}
