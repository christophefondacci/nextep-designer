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
package com.nextep.designer.sqlgen.ui.editors.sql;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.projection.Segment;
import org.eclipse.jface.text.rules.IToken;
import com.nextep.designer.dbgm.sql.TableAlias;

/**
 * @author Christophe Fondacci
 */
public class DMLParseResult {

	private List<Segment> tableSegments;
	private List<Segment> columnSegments;
	private List<TableAlias> fromTables;

	// Parsing variables
	/** Start / End of the table declaration */
	public int tableSegStart = -1;
	/** Start of the where declaration */
	public int whereSegStart = -1;
	/** Token which has initiated the table declaration */
	public IToken tableStartToken = null;
	/** Last parsed table alias */
	public TableAlias lastAlias = null;
	/** Ignore into */
	public boolean ignoreInto = true;
	public int stackStart = -1;
	public int parCount = 0;

	public DMLParseResult() {
		fromTables = new ArrayList<TableAlias>();
		tableSegments = new ArrayList<Segment>();
		columnSegments = new ArrayList<Segment>();
	}

	public List<Segment> getTableSegments() {
		return tableSegments;
	}

	public void addTableSegment(Segment tableSegment) {
		this.tableSegments.add(tableSegment);
	}

	public void addColumnSegment(Segment colSegment) {
		this.columnSegments.add(colSegment);
	}

	public List<TableAlias> getFromTables() {
		return fromTables;
	}

	public List<Segment> getColumnSegments() {
		return columnSegments;
	}

	public TableAlias getTableAlias(String alias) {
		for (TableAlias a : fromTables) {
			if (alias.equalsIgnoreCase(a.getTableAlias())) {
				return a;
			}
		}
		return null;
	}

	public void addFromTable(TableAlias a) {
		fromTables.add(a);
	}
}
