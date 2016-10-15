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
package com.nextep.designer.sqlgen.parsers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IToken;

import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.model.ITypedSqlParser;
import com.nextep.designer.dbgm.sql.ColumnAlias;
import com.nextep.designer.dbgm.sql.ISelectStatement;
import com.nextep.designer.dbgm.sql.TableAlias;
import com.nextep.designer.sqlgen.model.ISQLParser;

public class SelectParser implements ITypedSqlParser {

	private static final Log log = LogFactory.getLog(SelectParser.class);

	private static enum Location {
		NONE, SELECT, AS, FROM, WHERE, POST_WHERE
	}

	@Override
	public IParseData parse(IParseable p, String sql) {
		final ISelectStatement sel = (ISelectStatement) p;
		final DBVendor vendor = DBGMHelper.getCurrentVendor();
		final ISQLParser parser = GeneratorFactory.getSQLParser(vendor);
		final Map<String, List<String>> reservedWordsMap = parser.getTypedTokens();
		final List<String> reservedWords = new ArrayList<String>();
		for (List<String> reservedWordsList : reservedWordsMap.values()) {
			reservedWords.addAll(reservedWordsList);
		}
		// Handling null sql
		if (sql == null)
			return null;
		// Initializing SQL select scanner
		SelectScanner scanner = new SelectScanner();
		Document d = new Document(sql.toUpperCase());
		scanner.setRange(d, 0, d.getLength());
		IToken token = scanner.nextToken();
		// Preparing varaibles
		Location loc = Location.NONE;
		// Location subLoc = Location.NONE;
		List<String> tokens = new ArrayList<String>();
		int fromEnd = 0;
		while (!token.isEOF()) {
			if (token == SelectScanner.TOKEN_SELECT) {
				loc = Location.SELECT;
			} else if (token == SelectScanner.TOKEN_AS && loc == Location.SELECT) {

			} else if (token == SelectScanner.TOKEN_WORD) {
				String word = null;
				try {
					word = d.get(scanner.getTokenOffset(), scanner.getTokenLength());
					if (!reservedWords.contains(word)) {
						tokens.add(word);
					}
				} catch (BadLocationException e) {
					throw new ErrorException(e);
				}
			} else if (token == SelectScanner.TOKEN_DOT) {
				tokens.add(".");
			} else if (token == SelectScanner.TOKEN_COMMA) {
				switch (loc) {
				case SELECT:
					sel.addSelectedColumn(buildColumnAlias(tokens));
					tokens.clear();
					break;
				case FROM:
					sel.addFromTable(buildTableAlias(tokens));
					tokens.clear();
					break;
				}
			} else if (token == SelectScanner.TOKEN_FROM) {
				loc = Location.FROM;
				sel.addSelectedColumn(buildColumnAlias(tokens));
				tokens.clear();
			} else if (token == SelectScanner.TOKEN_WHERE) {
				loc = Location.WHERE;
				sel.addFromTable(buildTableAlias(tokens));
				tokens.clear();
				fromEnd = scanner.getTokenOffset();
			} else if (token == SelectScanner.TOKEN_GROUP || token == SelectScanner.TOKEN_ORDER) {
				if (loc == Location.FROM) {
					sel.addFromTable(buildTableAlias(tokens));
					tokens.clear();
				}
				loc = Location.POST_WHERE;
				if (fromEnd == 0)
					fromEnd = scanner.getTokenOffset();
			}
			token = scanner.nextToken();
		}
		if (loc == Location.FROM) {
			sel.addFromTable(buildTableAlias(tokens));
		}
		if (fromEnd > 0) {
			try {
				sel.setPostFromFragment(d.get(fromEnd, d.getLength() - fromEnd));
			} catch (BadLocationException e) {
				log.error("Problems while parsing SQL statement.");
			}
		}
		return null;
	}

	public ColumnAlias buildColumnAlias(List<String> tokens) {
		ColumnAlias c = new ColumnAlias();
		// If too many tokens, we have an expression column
		if (tokens.size() > 4) {
			for (int i = 0; i < tokens.size() - 1; i++) {
				c.setColumnName(c.getColumnName() + tokens.get(i));
			}
			c.setColumnAlias(tokens.get(tokens.size() - 1));
			return c;
		}
		// Else we should be in a standard case
		Iterator<String> it = tokens.iterator();
		if (it.hasNext()) {
			String first = it.next();
			// If only 1 item, this is the column name
			if (!it.hasNext()) {
				c.setColumnName(first);
				return c;
			} else {
				String sec = it.next();
				// If our 2nd token is a dot, then first is table alias and next
				// is col name
				if (".".equals(sec)) {
					c.setTableAlias(first);
					if (it.hasNext()) {
						c.setColumnName(it.next());
					}
				} else {
					// No dot, this is a column name
					c.setColumnName(first);
				}
				// We might have a final alias
				if (it.hasNext()) {
					c.setColumnAlias(it.next());
				}
			}
		}
		return c;
	}

	public TableAlias buildTableAlias(List<String> tokens) {
		// if (tokens.size() > 2) {
		// throw new
		// ErrorException("Unable to parse SQL statement: Invalid FROM clause near '"
		// + tokens.get(tokens.size() - 1) + "'");
		// } else
		if (tokens.isEmpty()) {
			throw new ErrorException("Unable to parse SQL statement: Empty FROM table definition.");
		} else {
			int dotIndex = -1;
			// Looking for a dot (meaning a schema prefix) because we would need
			// to ignore that part
			int i = 0;
			for (String token : tokens) {
				if (".".equals(token.trim())) {
					dotIndex = i;
				}
				i++;
			}
			// The table is the next token
			int tableIndex = dotIndex + 1;
			// Have
			if (tokens.size() > tableIndex) {
				// Getting our table name at the index we expect
				final String tableName = tokens.get(tableIndex);
				// Looking for any alias defined next
				String tableAlias = null;
				if (tokens.size() > tableIndex + 1) {
					tableAlias = tokens.get(tableIndex + 1);
				}
				// Now building our alias bean
				TableAlias ta = new TableAlias(tableName);
				ta.setAlias(tableAlias);
				return ta;
			} else {
				StringBuilder allTokens = new StringBuilder();
				for (String token : tokens) {
					allTokens.append(token + " ");
				}
				throw new ErrorException(
						"Unable to parse SQL statement: Invalid FROM clause near '"
								+ tokens.get(tokens.size() - 1) + "' in '" + allTokens.toString()
								+ "' (expected table name)");
			}
		}
	}

	@Override
	public String parseName(String sql) {
		return null;
	}

	@Override
	public String rename(String sqlToRename, String newName) {
		return null;
	}

	@Override
	public void rename(IParseable parseable, String newName) {

	}

}
