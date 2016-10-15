/*******************************************************************************
 * Copyright (c) 2012 neXtep Software and contributors.
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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.sqlclient.ui.helpers;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.projection.Segment;
import org.eclipse.jface.text.rules.IToken;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.designer.dbgm.sql.TableAlias;
import com.nextep.designer.sqlclient.ui.model.INextepMetadata;
import com.nextep.designer.sqlclient.ui.model.impl.NextepResultSetMetaData;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.ui.editors.sql.DMLParseResult;
import com.nextep.designer.sqlgen.ui.editors.sql.DMLScanner;

/**
 * @author Christophe Fondacci
 */
public final class SQLHelper {

	private static final Log LOGGER = LogFactory.getLog(SQLHelper.class);

	private SQLHelper() {
	}

	public static INextepMetadata createOfflineMetadata(ResultSetMetaData md, String sqlQuery)
			throws SQLException {
		final NextepResultSetMetaData nmd = new NextepResultSetMetaData();
		DMLParseResult parseResult = null;
		nmd.setColumnCount(md.getColumnCount());
		for (int i = 1; i <= md.getColumnCount(); i++) {
			nmd.setColumnName(i, md.getColumnName(i));
			nmd.setColumnType(i, md.getColumnType(i));
			// Fetching tablename from driver
			String tableName = md.getTableName(i);
			// If not available we try to parse ourselves
			if (tableName == null || "".equals(tableName)) {
				// Parsing
				if (parseResult == null) {
					try {
						parseResult = parseSQL(sqlQuery, 1);
					} catch (RuntimeException e) {
						LOGGER.error("Error while parsing SQL : " + e.getMessage(), e);
					}
				}
				// Only providing name on single table select
				if (parseResult != null && parseResult.getFromTables().size() == 1) {
					tableName = parseResult.getFromTables().iterator().next().getTableName();
				}
			}
			nmd.setTableName(i, tableName);
		}
		return nmd;
	}

	private static DMLParseResult parseSQL(String sql, int start) {
		final ISQLParser parser = GeneratorFactory.getSQLParser(DBGMHelper.getCurrentVendor());
		// Retrieving the corresponding statement start
		IDocument doc = new Document();
		doc.set(sql + " "); //$NON-NLS-1$

		FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(doc);
		try {
			IRegion lastSemicolonRegion = finder.find(start - 1, ";", false, false, false, false); //$NON-NLS-1$
			if (lastSemicolonRegion == null) {
				lastSemicolonRegion = new Region(0, 1);
			}
			IRegion selectRegion = finder.find(lastSemicolonRegion.getOffset(),
					"SELECT|INSERT|UPDATE|DELETE", true, false, false, true); //$NON-NLS-1$

			IRegion endSemicolonRegion = finder.find(start == doc.getLength() ? start - 1 : start,
					";", true, false, false, false); //$NON-NLS-1$
			if (endSemicolonRegion == null) {
				endSemicolonRegion = new Region(doc.getLength() - 1, 0);
			}
			if (selectRegion == null || lastSemicolonRegion == null || endSemicolonRegion == null) {
				return null;
			}
			// The select must be found after the first semicolon, else it is not the
			// same SQL statement
			if (selectRegion.getOffset() >= lastSemicolonRegion.getOffset()
					&& endSemicolonRegion.getOffset() >= selectRegion.getOffset()) {
				DMLScanner scanner = new DMLScanner(parser);
				scanner.setRange(doc, selectRegion.getOffset(), endSemicolonRegion.getOffset()
						- selectRegion.getOffset());
				IToken token = scanner.nextToken();
				DMLParseResult result = new DMLParseResult();
				Stack<DMLParseResult> stack = new Stack<DMLParseResult>();
				Map<Segment, DMLParseResult> results = new HashMap<Segment, DMLParseResult>();
				while (!token.isEOF()) {
					// Counting parenthethis
					if (token == DMLScanner.LEFTPAR_TOKEN) {
						result.parCount++;
					} else if (token == DMLScanner.RIGHTPAR_TOKEN) {
						result.parCount--;
					}

					if (token == DMLScanner.SELECT_TOKEN) { // && (result.tableSegStart>0 ||
						// result.whereSegStart>0)) {
						stack.push(result);
						result = new DMLParseResult();
						result.stackStart = scanner.getTokenOffset();
					} else if (token == DMLScanner.RIGHTPAR_TOKEN && result.parCount < 0) { // &&
						// stack.size()>0)
						// {
						results.put(new Segment(result.stackStart, scanner.getTokenOffset()
								- result.stackStart), result);
						result = stack.pop();
					} else if (token == DMLScanner.INSERT_TOKEN) {
						result.ignoreInto = false;
					} else if (token == DMLScanner.FROM_TOKEN || token == DMLScanner.UPDATE_TOKEN
							|| (token == DMLScanner.INTO_TOKEN && !result.ignoreInto)) {
						result.ignoreInto = true;
						// We have a table segment start
						result.tableSegStart = scanner.getTokenOffset();
						result.tableStartToken = token;
					} else if (token == DMLScanner.WORD_TOKEN && result.tableSegStart > 0) {
						// We are in a table segment so we instantiate appropriate table references
						// and aliases
						// in the parse result
						if (result.lastAlias == null) {
							// This is a new table definition, we add it
							result.lastAlias = new TableAlias(doc.get(scanner.getTokenOffset(),
									scanner.getTokenLength()).toUpperCase());
							// result.lastAlias
							// .setTable(tablesMap.get(result.lastAlias.getTableName()));
							result.addFromTable(result.lastAlias);
						} else if (result.lastAlias.getTableAlias() == null) {
							// This is an alias of a defined table
							final String alias = doc.get(scanner.getTokenOffset(),
									scanner.getTokenLength());
							final List<String> reservedWords = parser.getTypedTokens().get(
									ISQLParser.DML);
							if (!reservedWords.contains(alias.toUpperCase())) {
								result.lastAlias.setAlias(alias);
							} else {
								result.lastAlias = null;
							}
						}
					} else if (token == DMLScanner.COMMA_TOKEN) {
						// On a comma, we reset any table reference
						result.lastAlias = null;
					} else if (token == DMLScanner.DML_TOKEN) {
						result.lastAlias = null;
						if (result.tableSegStart != -1) {
							int tableSegEnd = scanner.getTokenOffset();
							result.addTableSegment(new Segment(result.tableSegStart, tableSegEnd
									- result.tableSegStart));
							result.tableSegStart = -1;
						}
					} else if (result.tableSegStart != -1
							&& ((result.tableStartToken == DMLScanner.FROM_TOKEN && token == DMLScanner.WHERE_TOKEN)
									|| (result.tableStartToken == DMLScanner.UPDATE_TOKEN && token == DMLScanner.SET_TOKEN) || (result.tableStartToken == DMLScanner.INTO_TOKEN && token == DMLScanner.LEFTPAR_TOKEN))) {
						// We have matched a table segment end, so we close the segment
						// and we add it to the parse result's table segments
						int tableSegEnd = scanner.getTokenOffset();
						result.addTableSegment(new Segment(result.tableSegStart, tableSegEnd
								- result.tableSegStart));
						result.tableSegStart = -1;
						if (token == DMLScanner.WHERE_TOKEN) {
							result.whereSegStart = scanner.getTokenOffset()
									+ scanner.getTokenLength();
						}
					}
					token = scanner.nextToken();
				}
				// If the table segment is still opened, we close it at the end of the SQL statement
				if (result.tableSegStart > -1) {
					int tableSegEnd = endSemicolonRegion.getOffset();
					result.addTableSegment(new Segment(result.tableSegStart, tableSegEnd
							- result.tableSegStart + 1));
				}
				// Locating the appropriate result
				for (Segment s : results.keySet()) {
					if (s.getOffset() <= start && s.getOffset() + s.getLength() > start) {
						return results.get(s);
					}
				}
				return result;
			}
		} catch (BadLocationException e) {
			LOGGER.debug("Problems while retrieving SQL statement");
		}
		return null;
	}

}
