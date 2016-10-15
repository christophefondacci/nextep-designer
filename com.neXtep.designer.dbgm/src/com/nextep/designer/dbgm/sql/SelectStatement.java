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
package com.nextep.designer.dbgm.sql;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;

/**
 * @author Christophe Fondacci
 */
public class SelectStatement extends AbstractSelectStatement {

	private static final Log log = LogFactory.getLog(SelectStatement.class);

	// public String getSQL() {
	// StringBuffer buf = new StringBuffer("select ");
	// boolean first = true;
	// for(ColumnAlias nc : getSelectedColumns()) {
	// if(!first) {
	// buf.append(", ");
	// } else {
	// first = false;
	// }
	// // Appending table alias prefix, if any
	// if(nc.getTableAlias()!=null && nc.getTableAlias()!="") {
	// buf.append(nc.getTableAlias() + ".");
	// }
	// // Appending column name
	// buf.append(nc.getColumnName());
	// // Appending column alias, if any
	// if(nc.getColumnAlias()!= null) {
	// buf.append(" " + nc.getColumnAlias());
	// }
	// }
	// buf.append(" from ");
	// first = true;
	// for(TableAlias ta : getFromTables()) {
	// if(!first) {
	// buf.append(", ");
	// } else {
	// first = false;
	// }
	// buf.append(ta.getTableName());
	// // Appending table alias, if any
	// if(ta.getTableAlias() != null) {
	// buf.append(" " + ta.getTableAlias());
	// }
	// }
	// buf.append(" " + getPostFromFragment());
	// return buf.toString();
	// }
	public SelectStatement(String sql) {
		super(sql);
	}

	public TableAlias buildTableAlias(List<String> tokens) {
		if (tokens.size() > 2) {
			throw new ErrorException("Unable to parse SQL statement: Invalid FROM clause near '"
					+ tokens.get(tokens.size() - 1) + "'");
		} else if (tokens.isEmpty()) {
			throw new ErrorException("Unable to parse SQL statement: Empty FROM table definition.");
		}
		Iterator<String> it = tokens.iterator();
		TableAlias t = new TableAlias(it.next());
		if (it.hasNext()) {
			t.setAlias(it.next());
		}
		return t;
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
				// If our 2nd token is a dot, then first is table alias and next is col name
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

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

}
