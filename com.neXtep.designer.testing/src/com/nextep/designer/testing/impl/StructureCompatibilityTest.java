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
package com.nextep.designer.testing.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.VersionContainer;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.testing.model.TestEvent;
import com.nextep.designer.testing.model.TestStatus;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

public class StructureCompatibilityTest extends DatabaseCompatibilityTest {

	@Override
	protected TestStatus runDatabase(IProgressMonitor monitor, Connection conn) {
		if(getCompatibilityRelease()==null) return TestStatus.FAILED;
		// Retrieving compatiblity container
		IVersionContainer container = (IVersionContainer)CorePlugin.getIdentifiableDao().load(VersionContainer.class, getCompatibilityRelease().getUID(), HibernateUtil.getInstance().getSandBoxSession(), true);
		if(container==null) {
			return TestStatus.FAILED;
		}
		// Retrieving tables
		TestStatus globalStatus= TestStatus.PASSED;
		final List<IVersionable<?>> tables = VersionHelper.getAllVersionables(container, IElementType.getInstance(IBasicTable.TYPE_ID));
		for(final IVersionable<?> v : tables) {
			if(monitor.isCanceled()) return TestStatus.FAILED;
			final IBasicTable t = (IBasicTable)v.getVersionnedObject().getModel();
			final String select = buildTableSelect(t);
			if(execute(conn,v,select)==TestStatus.FAILED) {
				globalStatus=TestStatus.FAILED;
			}
		}
		return globalStatus;
	}

	/**
	 * Builds a SQL-select statement which will select all
	 * columns of the given table
	 * @param t table to generate SQL-select for
	 * @return the SQL-select statement string
	 */
	private String buildTableSelect(IBasicTable t) {
		final StringBuilder b = new StringBuilder(50);
		b.append("select ");
		boolean first = true;
		for(IBasicColumn c : t.getColumns()) {
			if(first) {
				first=false;
			} else {
				b.append(',');
			}
			b.append(c.getName());
		}
		b.append(" from " + t.getName());
		if(VersionHelper.getCurrentView().getDBVendor() == DBVendor.ORACLE) {
			b.append(" where rownum=1");
		} else if(VersionHelper.getCurrentView().getDBVendor()==DBVendor.MYSQL) {
			b.append(" limit 1");
		}
		return b.toString();
	}
	
	private TestStatus execute(Connection conn, IVersionable<?> v, String sql) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery(sql);
			handle(v, TestEvent.COMPATIBILITY, TestStatus.PASSED, null);
			return TestStatus.PASSED;
		} catch(SQLException e) {
			handle(v, TestEvent.COMPATIBILITY, TestStatus.FAILED, e);
			return TestStatus.FAILED;
		} finally {
			if(stmt!=null) {
				try {
					stmt.close();
				} catch(SQLException e) {
					throw new ErrorException("SQL Connection problems while executing compatibility test.");
				}
			}
		}
		
	}

	@Override
	public String getName() {
		return "Structure test";
	}
}
