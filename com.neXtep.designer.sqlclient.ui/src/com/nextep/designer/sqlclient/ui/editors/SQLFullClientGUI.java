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
package com.nextep.designer.sqlclient.ui.editors;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlclient.ui.SQLClientImages;
import com.nextep.designer.sqlclient.ui.SQLClientMessages;
import com.nextep.designer.sqlclient.ui.handlers.ExecuteQueryHandler;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * @author Christophe Fondacci
 */
public class SQLFullClientGUI extends ControlledDisplayConnector {

	private static final Log LOGGER = LogFactory.getLog(SQLFullClientGUI.class);

	private static final String COL_TYPE = "Datatype"; //$NON-NLS-1$
	private static final int MAX_ROWS_BEFORE_REFRESH = 500;

	/** Container control */
	private Composite editor;
	private Composite controlPane;
	/** SQL Results table */
	// private Table sqlView;
	// Connection information
	private Label connectionLabel;
	private Combo databaseCombo;
	// SQL information
	private IDocumentProvider documentProvider;
	/** Pane to insert the SQL query editor */
	private Composite sqlEditorPane = null;
	/** run SQL button */
	private Button runSQLButton = null;
	/** The underlying SQL connection opened at SWT control creation, closed on disposal */
	private Connection connection;
	/** The SQL table column sorter */
	// private Listener sortListener;
	/** Folder for SQL results */
	private CTabFolder sqlFolder;
	private IConnection conn;

	/**
	 * Generic column sorter class TODO Support numeric datatypes through column data store
	 */
	private static class SortListener implements Listener {

		private Table sqlView;

		public SortListener(Table sqlView) {
			this.sqlView = sqlView;
		}

		public void handleEvent(Event e) {
			TableItem[] items = sqlView.getItems();
			Collator collator = Collator.getInstance(Locale.getDefault());
			TableColumn column = (TableColumn) e.widget;

			int sortOrder = SWT.UP;
			if (sqlView.getSortColumn() == column) {
				// Inverting order
				sortOrder = sqlView.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
			}
			int index = sqlView.indexOf(column);
			for (int i = 1; i < items.length; i++) {
				String value1 = items[i].getText(index);
				for (int j = 0; j < i; j++) {
					String value2 = items[j].getText(index);
					if (sortOrder == SWT.UP ? collator.compare(value1 == null ? "" : value1,
							value2 == null ? "" : value2) < 0
							: collator.compare(value1, value2) > 0) {
						final int colCount = sqlView.getColumnCount();
						String[] colVals = new String[colCount];
						for (int k = 0; k < colCount; k++) {
							colVals[k] = items[i].getText(k);
						}
						items[i].dispose();
						TableItem item = new TableItem(sqlView, SWT.NONE, j);
						item.setText(colVals);
						items = sqlView.getItems();
						break;
					}
				}
			}
			sqlView.setSortColumn(column);
			sqlView.setSortDirection(sortOrder);
		}
	};

	public SQLFullClientGUI(ISQLScript dbObject, IConnection conn) {
		super(dbObject, UIControllerFactory.getController(dbObject.getType()));
		this.conn = conn;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new SashForm(parent, SWT.VERTICAL);

		controlPane = new Composite(editor, SWT.BORDER);
		controlPane.setBackground(FontFactory.WHITE);
		addNoMarginLayout(controlPane, 3);
		// controlPane.setLayout(new GridLayout(3,false));
		controlPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		connectionLabel = new Label(controlPane, SWT.NONE);
		connectionLabel.setText("Connected to : ");
		connectionLabel.setBackground(FontFactory.WHITE);
		createReferenceDBCombo();
		runSQLButton = new Button(controlPane, SWT.PUSH);
		runSQLButton.setImage(SQLClientImages.ICON_RUN);
		runSQLButton.setToolTipText("Run the SQL command");
		runSQLButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (documentProvider != null) {
					try {
						new ExecuteQueryHandler().execute(null);
					} catch (ExecutionException ex) {
						throw new ErrorException(ex);
					}
				}
			}
		});

		sqlEditorPane = new Composite(controlPane, SWT.BORDER);
		FillLayout gl = new FillLayout();
		gl.marginHeight = gl.marginWidth = 0;
		sqlEditorPane.setLayout(gl);
		sqlEditorPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 3));

		sqlFolder = new CTabFolder(editor, SWT.CLOSE | SWT.BORDER);
		sqlFolder.setMRUVisible(true);
		sqlFolder.setSimple(false);
		sqlFolder.setSelectionBackground(new Color[] { FontFactory.WHITE,
				FontFactory.SQL_SHADOW_COLOR0, FontFactory.SQL_SHADOW_COLOR1,
				FontFactory.SQL_SHADOW_COLOR2 }, new int[] { 25, 50, 100 }, true);
		sqlFolder.setSelectionForeground(FontFactory.WHITE);

		sqlFolder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));

		// sqlView = new Table(editor, SWT.FULL_SELECTION | SWT.BORDER);
		// sqlView.setHeaderVisible(true);
		// sqlView.setLinesVisible(true);
		// sqlView.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true,2,1));

		// Establishing SQL Connection
		initSQLConnection();
		// Returning SQL results table
		return sqlFolder;
	}

	public Composite getSQLEditorPane() {
		return sqlEditorPane;
	}

	/**
	 * Creates the database combo
	 */
	private void createReferenceDBCombo() {
		GridData gridData4 = new GridData();
		// gridData4.grabExcessHorizontalSpace = true;
		gridData4.verticalAlignment = GridData.CENTER;
		// gridData4.horizontalAlignment = GridData.FILL;
		databaseCombo = new Combo(controlPane, SWT.READ_ONLY);
		databaseCombo.setLayoutData(gridData4);
		// fromDBCombo.addSelectionListener(this);
		int i = 0;
		for (IConnection c : DBGMHelper.getTargetSet().getConnections()) {
			databaseCombo.add(c.getName());
			databaseCombo.setData(c.getName(), c);
			if (c == conn) {
				databaseCombo.select(i);
			}
			i++;
		}
		databaseCombo.setEnabled(false);

	}

	/**
	 * Initializing the SQL connection used to retrieve data from target database.
	 */
	private void initSQLConnection() {
		if (conn == null) {
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"No development target", "No development target database has been defined. "
							+ "Contents viewer needs a database connection to query SQL data. "
							+ "Please define a development target connection and try again.");
			throw new ErrorException("No development database defined.");
		}

		try {
			connection = CorePlugin.getConnectionService().connect(conn);
		} catch (SQLException e) {
			throw new ErrorException("Could not establish connection : " + e.getMessage(), e);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * Closes the SQL connection
	 */
	private void closeSQLConnection() {
		try {
			connection.close();
			LOGGER.info("Closing SQL connection");
		} catch (SQLException e) {
			throw new ErrorException("Error while closing SQL connection: " + e.getMessage(), e);
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return sqlFolder;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		// Clearing current table columns
		// clearSQLView();

		final ISQLScript script = (ISQLScript) getModel();
		if (script.getSql() == null || "".equals(script.getSql())) {
			return;
		}
		try {
			// sqlText.add("select * from " + o.getName());
			// sqlText.select(sqlText.getItemCount()-1);
			// Creating result table
			final CTabItem sqlItem = new CTabItem(sqlFolder, SWT.NONE);
			Composite resultPane = new Composite(sqlFolder, SWT.NONE);
			final Table sqlView = new Table(resultPane, SWT.FULL_SELECTION | SWT.BORDER);
			final NextepTableEditor editor = NextepTableEditor.handle(sqlView);
			CoolBar statsBar = new CoolBar(resultPane, SWT.NONE);
			statsBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			final CoolItem rowsItem = new CoolItem(statsBar, SWT.NONE);
			rowsItem.setSize(rowsItem.computeSize(100, 20));
			final Label rowsCount = new Label(statsBar, SWT.NONE);
			rowsItem.setControl(rowsCount);
			final CoolItem timeItem = new CoolItem(statsBar, SWT.NONE);
			final Label timeLabel = new Label(statsBar, SWT.NONE);
			timeItem.setControl(timeLabel);
			timeItem.setSize(timeItem.computeSize(200, 20));
			sqlView.setHeaderVisible(true);
			sqlView.setLinesVisible(true);
			sqlView.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
			resultPane.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
			GridLayout grid = new GridLayout();
			grid.marginBottom = grid.marginHeight = grid.marginLeft = grid.marginRight = grid.marginTop = grid.marginWidth = 0;
			resultPane.setLayout(grid);
			sqlItem.setControl(resultPane);
			final Listener sortListener = new SortListener(sqlView);
			final String query = formatQuery(script.getSql());
			final int queryLen = query.length();
			sqlItem.setText(queryLen < 30 ? query : query.substring(0, 30) + "...");
			sqlItem.setToolTipText(query);
			sqlFolder.setSelection(sqlItem);
			final List<ICommand> bufferedCommands = new ArrayList<ICommand>();
			// Initializing lines
			Job refreshJob = new Job("Fetching SQL data...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Statement s = null;
					ResultSet r = null;
					try {
						s = connection.createStatement();
						final Date startDate = new Date();
						final boolean isResultSet = s.execute(query);
						final Date afterExecDate = new Date();

						if (!isResultSet) {
							final int updates = s.getUpdateCount();
							bufferedCommands.add(new ICommand() {

								@Override
								public String getName() {
									return null;
								}

								@Override
								public Object execute(Object... parameters) {
									if (sqlView != null && !sqlView.isDisposed()) {
										TableColumn c = new TableColumn(sqlView, SWT.NONE);
										c.setText(SQLClientMessages.getString("sql.result"));
										c.setWidth(300);
										c.addListener(SWT.Selection, sortListener);
										if (updates > 0) {
											final TableItem i = new TableItem(sqlView, SWT.NONE);
											i.setText(MessageFormat.format(
													SQLClientMessages.getString("sql.updatedRows"),
													updates));
										} else {
											final TableItem i = new TableItem(sqlView, SWT.NONE);
											i.setText(SQLClientMessages.getString("sql.queryOk"));
										}
									}
									return null;
								}
							});
							syncProcessCommands(bufferedCommands);
							return Status.OK_STATUS;
						}
						r = s.getResultSet();

						// Initializing columns
						final ResultSetMetaData md = r.getMetaData();
						// Initializing sizing table
						final int[] colMaxWidth = new int[md.getColumnCount() + 1];
						for (int i = 1; i <= md.getColumnCount(); i++) {
							final int index = i;
							final String colName = md.getColumnName(index);
							// final int colPrecision = md.getPrecision(index);
							final int colType = md.getColumnType(index);
							final int colIndex = i - 1;

							bufferedCommands.add(new ICommand() {

								@Override
								public String getName() {
									return null;
								}

								@Override
								public Object execute(Object... parameters) {
									if (!sqlView.isDisposed()) {
										TableColumn c = new TableColumn(sqlView, SWT.NONE);
										c.addListener(SWT.Selection, sortListener);
										c.setText(colName);
										c.setWidth(colName.length() * 8);
										colMaxWidth[colIndex] = c.getWidth();
										c.setData(COL_TYPE, colType);
										TextColumnEditor.handle(editor, colIndex,
												ChangeEvent.CUSTOM_1, new IEventListener() {

													@Override
													public void handleEvent(ChangeEvent event,
															IObservable source, Object data) {
													}
												});
									}
									return null;
								}
							});
						}
						final ResultSet rset = r;
						int rows = 0;
						final long execTime = afterExecDate.getTime() - startDate.getTime();
						bufferedCommands.add(new ICommand() {

							@Override
							public String getName() {
								return null;
							}

							@Override
							public Object execute(Object... parameters) {
								timeLabel.setText(MessageFormat.format(
										SQLClientMessages.getString("sql.executionTime"), execTime));
								return null;
							}
						});
						syncProcessCommands(bufferedCommands);
						while (r.next()) {
							rows++;
							// Handling cancellation while fetching SQL lines
							if (monitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
							final String[] colValues = new String[md.getColumnCount()];
							final Collection<Integer> nullCols = new ArrayList<Integer>();
							for (int i = 1; i <= md.getColumnCount(); i++) {
								Object val = null;
								try {
									val = rset.getObject(i);
								} catch (SQLException e) {
									LOGGER.error(
											"Error while fetching column value : " + e.getMessage(),
											e);
									val = e.getMessage();
								}
								final String strVal = strVal(val);
								colValues[i - 1] = strVal;
								// Building list of null columns
								if (val == null) {
									nullCols.add(i - 1);
								}
								// Updating max sizes
								final int colWidth = colMaxWidth[i - 1];
								if (strVal.length() * 8 > colWidth) {
									colMaxWidth[i - 1] = strVal.length() * 8;
								}
							}
							// Adding the row as a command
							bufferedCommands.add(buildAddRowCommand(colValues, sqlView, nullCols));
							// Flushing to display every N lines
							if (bufferedCommands.size() > MAX_ROWS_BEFORE_REFRESH) {
								bufferedCommands.add(buildAdjustWidthCommand(sqlView, colMaxWidth));
								syncProcessCommands(bufferedCommands);
							}
						}
						// Flushing any left row
						bufferedCommands.add(buildAdjustWidthCommand(sqlView, colMaxWidth));

						final Date afterFetchDate = new Date();
						final int nbRows = rows;
						bufferedCommands.add(new ICommand() {

							@Override
							public String getName() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public Object execute(Object... parameters) {
								long fetchTime = afterFetchDate.getTime() - afterExecDate.getTime();
								timeLabel.setText(MessageFormat.format(
										SQLClientMessages.getString("sql.executionFetchTime"),
										execTime, fetchTime));
								rowsCount.setText(MessageFormat.format(
										SQLClientMessages.getString("sql.fetchedRows"), nbRows));
								return null;
							}
						});
						syncProcessCommands(bufferedCommands);
					} catch (final SQLException e) {
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

							@Override
							public void run() {
								if (!sqlView.isDisposed()) {
									sqlView.removeAll();
									for (TableColumn c : sqlView.getColumns()) {
										c.dispose();
									}
									TableColumn c = new TableColumn(sqlView, SWT.NONE);
									c.setText("SQL Exception " + e.getErrorCode());
									c.setWidth(300);
									TableItem i = new TableItem(sqlView, SWT.NONE);
									i.setText(e.getMessage());
								}

							}
						});
						// throw new ErrorException(e);
					} finally {
						try {
							if (r != null) {// && !r.isClosed()) {
								r.close();
							}
							if (s != null) { // && !s.isClosed()) {
								s.close();
							}
						} catch (SQLException e) {
							throw new ErrorException(e);
						} finally {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

								@Override
								public void run() {
									// If the user has closed his SQL Query editor, we will
									// fall here (exception) with a disposed button
									if (runSQLButton != null && !runSQLButton.isDisposed()) {
										runSQLButton.setEnabled(true);
									}
								}
							});
						}
					}

					return Status.OK_STATUS;
				}
			};
			runSQLButton.setEnabled(false);
			refreshJob.schedule();

			// } catch(SQLException e) {
			// throw new ErrorException(e);
		} finally {
			// try {
			// if(stmt != null && !stmt.isClosed()) {
			// stmt.close();
			// }
			// if(rset != null && !rset.isClosed()) {
			// rset.close();
			// }
			// } catch(SQLException e) {
			// throw new ErrorException(e);
			// }
		}
	}

	private ICommand buildAddRowCommand(final String[] values, final Table sqlView,
			final Collection<Integer> nullCols) {
		return new ICommand() {

			@Override
			public String getName() {
				return "Adding sql row";
			}

			@Override
			public Object execute(Object... parameters) {
				final TableItem item = new TableItem(sqlView, SWT.NONE);
				item.setText(values);
				for (Integer colIndex : nullCols) {
					// Updating cell's background color for null values
					item.setBackground(colIndex, FontFactory.SHADOW_GRAPH3_COLOR);
				}
				return null;
			}
		};
	}

	private ICommand buildAdjustWidthCommand(final Table sqlView, final int[] maxWidths) {
		return new ICommand() {

			@Override
			public String getName() {
				return "Adjusting column width";
			}

			@Override
			public Object execute(Object... parameters) {
				final TableColumn[] columns = sqlView.getColumns();
				for (int i = 0; i < columns.length; i++) {
					final TableColumn c = columns[i];
					if (c.getWidth() < maxWidths[i]) {
						c.setWidth(maxWidths[i]);
					}
				}
				return null;
			}
		};
	}

	/**
	 * Runs the specified list of commands in the UI thread
	 * 
	 * @param commands list of commands to execute in the {@link UID} thread
	 */
	private void syncProcessCommands(final List<ICommand> commands) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				for (ICommand c : commands) {
					c.execute();
				}
			}
		});
		commands.clear();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	@Override
	public void widgetDisposed(DisposeEvent event) {
		super.widgetDisposed(event);
		closeSQLConnection();
	}

	public void setDocumentProvider(IDocumentProvider doc) {
		this.documentProvider = doc;
	}

	// public void setSQLInput(ISQLEditorInput<?> sqlInput) {
	// this.sqlInput = sqlInput;
	// }
	/**
	 * Formats the user text into a proper SQL string
	 * 
	 * @param userText user input text
	 */
	private String formatQuery(String userText) {
		String query = userText == null ? "" : userText.trim();
		if (query.length() > 1) {
			if (query.charAt(query.length() - 1) == ';') {
				if (!query.toUpperCase().contains("BEGIN") && !query.toUpperCase().contains("END")) {
					query = query.substring(0, query.length() - 1);
				}
			}
			if (query.charAt(query.length() - 1) == '/') {
				query = query.substring(0, query.length() - 1);
			}
		}
		query = query.replace("\r", " ");
		query = query.replace("\n", " ");
		return query;
	}
}
