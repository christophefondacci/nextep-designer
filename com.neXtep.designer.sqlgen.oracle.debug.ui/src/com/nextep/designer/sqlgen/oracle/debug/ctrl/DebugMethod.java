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
package com.nextep.designer.sqlgen.oracle.debug.ctrl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import oracle.jdbc.OracleTypes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.MethodInvoker;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.ui.services.IBreakpoint;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;

/**
 * @author Christophe Fondacci
 */
public class DebugMethod extends MethodInvoker {

	private static final Log LOGGER = LogFactory.getLog(DebugMethod.class);

	private Connection targetConn;
	private Connection debugConn;
	private String debugSessionID;

	@Override
	public String getMethodName() {
		return "Debug PL/SQL Procedure";
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return new Class<?>[] { IConnection.class };
	}

	@Override
	public Object invokeMethod(Object... arg) {

		IConnection conn = (IConnection) arg[0];

		CallableStatement stmt = null;
		Thread debuggedThread = null;
		try {
			// Initializing our target connection
			targetConn = CorePlugin.getConnectionService().connect(conn);
			//
			stmt = targetConn.prepareCall("ALTER SESSION SET PLSQL_DEBUG=TRUE"); //$NON-NLS-1$
			try {
				stmt.execute();
			} finally {
				CaptureHelper.safeClose(null, stmt);
			}

			stmt = targetConn.prepareCall("{ ? = CALL DBMS_DEBUG.INITIALIZE() }"); //$NON-NLS-1$
			try {
				stmt.registerOutParameter(1, Types.VARCHAR);
				stmt.execute();
				debugSessionID = stmt.getString(1);
			} catch (SQLException e) {
				throw new ErrorException(e);
			} finally {
				CaptureHelper.safeClose(null, stmt);
			}

			// Switching to debug mode
			stmt = targetConn.prepareCall("{ CALL DBMS_DEBUG.DEBUG_ON() }"); //$NON-NLS-1$
			try {
				stmt.execute();
			} finally {
				CaptureHelper.safeClose(null, stmt);
			}

			// Starting our target code
			debuggedThread = new Thread(new TargetRunnable(targetConn));
			debuggedThread.start();

			// Now that we have our ID, we initialize debug connection
			debugConn = CorePlugin.getConnectionService().connect(conn);
			// new Thread(new DebugRunnable(debugConn,debugSessionID)).start();

			stmt = debugConn.prepareCall("{ CALL DBMS_DEBUG.ATTACH_SESSION(?) }"); //$NON-NLS-1$
			try {
				stmt.setString(1, debugSessionID);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				CaptureHelper.safeClose(null, stmt);
			}

			stmt = debugConn.prepareCall("{ ? = CALL DBMS_DEBUG.SYNCHRONIZE(?,0) }"); //$NON-NLS-1$
			try {
				stmt.registerOutParameter(1, Types.INTEGER);
				stmt.registerOutParameter(2, OracleTypes.OTHER, "DBMS_DEBUG.RUNTIME_INFO"); //$NON-NLS-1$
				stmt.execute();
				Object o = stmt.getObject(2);
				if (o != null) {

				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				CaptureHelper.safeClose(null, stmt);
			}
			// // Setting breakpoints
			// stmt =
			// debugConn.prepareCall("{ call adp_debug.set_breakpoint(p_line=>?, p_name=>?, p_body=>true) }");
			// try {
			// for(IBreakpoint bp : SQLEditorUIServices.getInstance().getBreakpoints()) {
			// stmt.setInt(1, bp.getLine());
			// stmt.setString(2,bp.getTarget().getName());
			// stmt.execute();
			// }
			// } catch( Exception e) {
			// e.printStackTrace();
			// } finally {
			// stmt.close();
			// }
			stmt = debugConn.prepareCall("{ ? = CALL DBMS_DEBUG.CONTINUE(?,0,46) }"); //$NON-NLS-1$
			stmt.registerOutParameter(1, Types.INTEGER);
			stmt.registerOutParameter(2, OracleTypes.OTHER, "DBMS_DEBUG.RUNTIME_INFO"); //$NON-NLS-1$

			try {
				stmt.execute();
				Struct struct = (Struct) stmt.getObject(2);
				Object[] attrs = struct.getAttributes();
				int line = (Integer) attrs[0];
				int terminated = (Integer) attrs[1];
				int breakpoint = (Integer) attrs[2];
				LOGGER.debug("Continued to line " + line + ", terminated=" + terminated
						+ ", breakpoint=" + breakpoint);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				CaptureHelper.safeClose(null, stmt);
			}

		} catch (SQLException e) {
			if (debuggedThread != null) {
				debuggedThread.interrupt();
			}
			throw new ErrorException(e);
		} finally {
			try {
				if (debugConn != null) {
					debugConn.close();
				}
			} catch (SQLException e) {
				throw new ErrorException("Unable to properly close connection: " + e.getMessage(),
						e);
			}
		}

		return null;
	}

	private class TargetRunnable implements Runnable {

		Connection conn;

		public TargetRunnable(Connection conn) {
			this.conn = conn;
		}

		@Override
		public void run() {
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				stmt.execute("BEGIN TECH_UTIL.CLEAN_TXT('mytest'); END;"); //$NON-NLS-1$
			} catch (SQLException e) {
				LOGGER.error("Problems in target session", e);
			} finally {
				LOGGER.debug("Terminating target session");
				CaptureHelper.safeClose(null, stmt);
			}
			LOGGER.debug("End target SUCCESS");
		}

	}

	private class DebugRunnable implements Runnable {

		private Connection conn;
		private String debugID;

		public DebugRunnable(Connection conn, String debugID) {
			this.conn = conn;
			this.debugID = debugID;
		}

		@Override
		public void run() {
			CallableStatement stmt = null;
			try {
				stmt = targetConn.prepareCall("{ CALL ADP_DEBUG.START_DEBUGGER(?) }"); //$NON-NLS-1$
				try {
					stmt.setString(1, debugID);
					stmt.execute();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					CaptureHelper.safeClose(null, stmt);
				}
				// Setting breakpoints
				stmt = conn
						.prepareCall("{ CALL ADP_DEBUG.SET_BREAKPOINT(p_line=>?, p_name=>?, p_body=>true) }"); //$NON-NLS-1$
				try {
					for (IBreakpoint bp : SQLEditorUIServices.getInstance().getBreakpoints()) {
						stmt.setInt(1, bp.getLine());
						stmt.setString(2, bp.getTarget().getName());
						stmt.execute();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					CaptureHelper.safeClose(null, stmt);
				}

				stmt = targetConn.prepareCall("{ ? = CALL DBMS_DEBUG.CONTINUE(?,0,46) }"); //$NON-NLS-1$
				stmt.registerOutParameter(1, Types.INTEGER);
				stmt.registerOutParameter(2, Types.STRUCT, "DBMS_DEBUG.RUNTIME_INFO"); //$NON-NLS-1$
				try {
					stmt.execute();
					Struct struct = (Struct) stmt.getObject(2);
					Object[] attrs = struct.getAttributes();
					int line = (Integer) attrs[0];
					int terminated = (Integer) attrs[1];
					int breakpoint = (Integer) attrs[2];
					LOGGER.debug("Continued to line " + line + ", terminated=" + terminated
							+ ", breakpoint=" + breakpoint);
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					CaptureHelper.safeClose(null, stmt);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

}
