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
package com.nextep.designer.sqlclient.ui.handlers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.gui.impl.GUIWrapper;
import com.nextep.datadesigner.sqlgen.model.IGenerationConsole;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.dbgm.ui.services.DBGMUIHelper;
import com.nextep.designer.sqlclient.ui.editors.ProfilerSettingsGUI;
import com.nextep.designer.sqlclient.ui.impl.ProfilerSettings;
import com.nextep.designer.sqlclient.ui.model.IProfilerSettings;
import com.nextep.designer.sqlgen.ui.views.GenerationConsole;

public class ProfileQueryHandler extends AbstractHandler {

	private static final Log log = LogFactory.getLog(ProfileQueryHandler.class);
	private static List<String> csvStats = new ArrayList<String>();
	private static final DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	private static class SqlThread implements Runnable {

		private IConnection connection;
		private String sql[];
		public long totalTime;
		public long maxTime = -1;
		public long minTime = -1;
		public boolean fetch;
		public boolean randOrder;
		public long executions;
		private volatile boolean shouldStop = false;
		private static final Log log = LogFactory.getLog(SqlThread.class);

		public SqlThread(IConnection connection, boolean fetch, boolean randOrder, String[] sql) {
			this.connection = connection;
			this.sql = sql;
			this.fetch = fetch;
			this.randOrder = randOrder;
		}

		@Override
		public void run() {
			final IDatabaseConnector connector = CorePlugin.getConnectionService()
					.getDatabaseConnector(connection);
			final Random randGenerator = new Random();
			Connection c = null;
			Statement stmt = null;

			try {
				c = connector.connect(connection);
				stmt = c.createStatement();
				int nextQueryPos = (randOrder ? randGenerator.nextInt(sql.length) : 0);

				while (!shouldStop) {
					final long start = new Date().getTime();
					final boolean isRset = stmt.execute(sql[nextQueryPos]);
					nextQueryPos = (randOrder ? randGenerator.nextInt(sql.length) : nextQueryPos++);

					if (isRset && fetch) {
						ResultSet rset = null;
						try {
							rset = stmt.getResultSet();
							ResultSetMetaData md = rset.getMetaData();
							while (rset.next()) {
								// This loop goes through every result, every column
								for (int i = 1; i <= md.getColumnCount(); i++) {
									final Object o = rset.getObject(i);
								}
							}
						} catch (SQLException sqle) {
							log.error("Error while fetching Resulset", sqle);
						} finally {
							try {
								if (rset != null) {
									rset.close();
								}
							} catch (SQLException sqle) {
								log.error("Error while closing Resulset", sqle);
							}
						}
					}

					final long end = new Date().getTime();
					long time = (end - start);
					totalTime += time;
					if (minTime < 0) {
						minTime = time;
						maxTime = time;
					} else {
						maxTime = Math.max(maxTime, time);
						minTime = Math.min(minTime, time);
					}
					executions++;
				}
			} catch (SQLException e) {
				log.error("Error during SQL profiling", e);
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (c != null) {
						c.close();
					}
				} catch (SQLException e) {
					log.error("Error while terminating SQL profiling", e);
				}
			}

		}

		public void stop() {
			this.shouldStop = true;
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchSite site = HandlerUtil.getActiveSite(event);
		ISelectionProvider provider = site.getSelectionProvider();
		final IGenerationConsole console = new GenerationConsole("SQL Profiling - "
				+ new Date().toString(), true);
		ISelection sel = provider.getSelection();
		if (sel == null || sel.isEmpty())
			return null;
		if (sel instanceof ITextSelection) {
			ITextSelection textSel = (ITextSelection) sel;

			final String sql = textSel.getText().trim();
			if (sql.length() == 0) {
				return null;
			}
			final String[] sqls = sql.split(";");

			// Building profiler settings
			final IProfilerSettings settings = new ProfilerSettings();
			GUIWrapper wrapper = new GUIWrapper(new ProfilerSettingsGUI(settings),
					"Define profiling settings", 400, 220);
			wrapper.invoke();
			console.start();
			final IConnection conn = DBGMUIHelper.getConnection(null);

			// Starting profiling
			Job profilingJob = new Job("Profiling SQL...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					console.log("Profiler started: " + settings.getThreadCount() + " thread(s)"
							+ ", ramp-up period "
							+ (settings.getThreadCount() * settings.getThreadStepDuration())
							+ " seconds" + ", Fetching "
							+ (settings.isFetching() ? "enabled" : "disabled")
							+ ", Random execution "
							+ (settings.isRandomOrder() ? "enabled" : "disabled"));
					csvStats.clear();
					csvStats.add("time;threads count;Minimum time;Maximum time;Average Time;Total Time;Total Executions");
					final long startTime = new Date().getTime();
					long currentTime = new Date().getTime();
					long lastThreadTime = 0;
					List<SqlThread> threads = new ArrayList<SqlThread>();
					while (((currentTime - startTime) / 1000) < settings.getDuration()
							&& !monitor.isCanceled()) {
						printStatistics(threads, console);
						if (threads.size() < settings.getThreadCount()) {
							if ((currentTime - lastThreadTime) > (settings.getThreadStepDuration() * 1000)) {
								SqlThread thread = new SqlThread(conn, settings.isFetching(),
										settings.isRandomOrder(), sqls);
								threads.add(thread);
								Thread t = new Thread(thread);
								t.start();
								lastThreadTime = new Date().getTime();
							}
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							log.error("Interrupted profiling", e);
							break;
						}
						currentTime = new Date().getTime();
					}
					printStatistics(threads, console);
					// Stopping everything
					for (SqlThread t : threads) {
						t.stop();
					}
					console.log("Profiler stopped");
					console.log("Profiler CSV statistics : ");
					for (String s : csvStats) {
						console.log(s);
					}

					return Status.OK_STATUS;
				}
			};

			profilingJob.schedule();
		}
		return null;
	}

	private void printStatistics(List<SqlThread> threads, IGenerationConsole console) {
		long minTime = -1, maxTime = -1, totalTime = 0;
		int executions = 0;
		for (SqlThread t : threads) {
			if (minTime < 0) {
				minTime = t.minTime;
				maxTime = t.maxTime;
			} else {
				minTime = Math.min(t.minTime, minTime);
				maxTime = Math.max(t.maxTime, maxTime);
			}
			totalTime += t.totalTime;
			executions += t.executions;
		}
		final Date thisDate = new Date();
		console.log(format.format(thisDate) + " - Profiled " + threads.size() + " threads : min="
				+ minTime + "ms, max=" + maxTime + "ms, avg="
				+ ((double) totalTime / (double) executions) + "ms, exec=" + executions);
		csvStats.add(format.format(thisDate) + ";" + threads.size() + ";" + minTime + ";" + maxTime
				+ ";" + ((double) totalTime / (double) executions) + ";" + totalTime + ";"
				+ executions);
	}

}
