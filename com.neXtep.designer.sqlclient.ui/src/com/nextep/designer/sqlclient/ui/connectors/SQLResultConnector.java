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
package com.nextep.designer.sqlclient.ui.connectors;

import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.progress.UIJob;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.sqlclient.ui.SQLClientMessages;
import com.nextep.designer.sqlclient.ui.jface.DateCellValidator;
import com.nextep.designer.sqlclient.ui.jface.DateComparator;
import com.nextep.designer.sqlclient.ui.jface.NumberComparator;
import com.nextep.designer.sqlclient.ui.jface.SQLResultContentProvider;
import com.nextep.designer.sqlclient.ui.jface.SQLResultLabelProvider;
import com.nextep.designer.sqlclient.ui.jface.SQLResultViewerComparator;
import com.nextep.designer.sqlclient.ui.jface.SQLRowResultCellModifier;
import com.nextep.designer.sqlclient.ui.jface.StringComparator;
import com.nextep.designer.sqlclient.ui.model.INextepMetadata;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLQueryListener;
import com.nextep.designer.sqlclient.ui.model.ISQLTypeSerializer;
import com.nextep.designer.sqlclient.ui.model.IStatusAccessor;
import com.nextep.designer.sqlclient.ui.model.impl.DateSerializer;
import com.nextep.designer.sqlclient.ui.model.impl.NumberSerializer;
import com.nextep.designer.sqlclient.ui.model.impl.StringSerializer;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.base.AbstractUIComponent;

public class SQLResultConnector extends AbstractUIComponent implements ISQLQueryListener {

	private final static Log LOGGER = LogFactory.getLog(SQLResultConnector.class);

	public final static String KEY_CELL_VALIDATOR = "validator"; //$NON-NLS-1$
	public final static String KEY_DESERIALIZER = "deserializer";
	private final static ICellEditorValidator DATE_VALIDATOR = new DateCellValidator();
	private final static ISQLTypeSerializer DATE_DESERIALIZER = new DateSerializer();
	private final static ISQLTypeSerializer NUMBER_DESERIALIZER = new NumberSerializer();
	private final static ISQLTypeSerializer STRING_DESERIALIZER = new StringSerializer();

	private Table sqlView;
	private TableViewer sqlViewer;
	private SQLRowResultCellModifier modifier;
	private int[] colMaxWidth;
	private CLabel timeLabel;
	private CLabel rowsCount;
	private SQLResultViewerComparator comparator;
	private ISQLQuery currentQuery;
	private boolean isEditable = false;

	@Override
	public Control create(Composite parent) {
		Composite resultPane = new Composite(parent, SWT.NONE);
		sqlView = new Table(resultPane, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL | SWT.MULTI);
		// final NextepTableEditor editor = NextepTableEditor.handle(sqlView);
		CoolBar statsBar = new CoolBar(resultPane, SWT.NONE);
		statsBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// Status item for result count
		final CoolItem rowsItem = new CoolItem(statsBar, SWT.NONE);
		rowsItem.setSize(rowsItem.computeSize(150, 20));
		rowsCount = new CLabel(statsBar, SWT.NONE);
		rowsItem.setControl(rowsCount);
		// Status item for query time and fetch time
		final CoolItem timeItem = new CoolItem(statsBar, SWT.NONE);
		timeLabel = new CLabel(statsBar, SWT.NONE);
		timeItem.setControl(timeLabel);
		timeItem.setSize(timeItem.computeSize(400, 20));

		sqlView.setHeaderVisible(true);
		sqlView.setLinesVisible(true);
		sqlView.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		resultPane.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		GridLayout grid = new GridLayout();
		grid.marginBottom = grid.marginHeight = grid.marginLeft = grid.marginRight = grid.marginTop = grid.marginWidth = 0;
		resultPane.setLayout(grid);

		// Configuring viewer
		sqlViewer = new TableViewer(sqlView);
		sqlViewer.setContentProvider(new SQLResultContentProvider());
		ColumnViewerToolTipSupport.enableFor(sqlViewer, ToolTip.NO_RECREATE);
		SQLResultLabelProvider lblProvider = new SQLResultLabelProvider(this, 0);
		final TableViewerColumn viewerCol = new TableViewerColumn(sqlViewer, SWT.NONE);
		viewerCol.setLabelProvider(lblProvider);
		// final ITableLabelProvider labelProvider = new SQLResultLabelProvider();
		// sqlViewer.setLabelProvider(labelProvider);
		modifier = new SQLRowResultCellModifier(false, sqlViewer, new IStatusAccessor() {

			@Override
			public void setStatus(String status, boolean error) {
				setStatusLabel(status, error);
			}
		});
		// Initializing comparator
		comparator = new SQLResultViewerComparator(sqlViewer);
		sqlViewer.setComparator(comparator);
		return resultPane;
	}

	@Override
	public void queryStarted(ISQLQuery query) {
		disposeQuery();
		currentQuery = query;
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				setStatusLabel("Executing...", false);
				rowsCount.setText("");
				removeTableColumns();
				modifier.setSQLQuery(currentQuery);
			}
		});
	}

	private void disposeQuery() {
		if (currentQuery != null) {
			currentQuery.dispose();
		}
	}

	private void removeTableColumns() {
		for (TableColumn c : sqlView.getColumns()) {
			c.dispose();
		}
		sqlViewer.setItemCount(0);
		sqlViewer.setInput(null);
	}

	@Override
	public void queryFinished(final ISQLQuery query, final long execTime, final long totalTime,
			final int resultCount, final boolean isResultSet) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				if (isResultSet) {
					setStatusLabel(MessageFormat.format(
							SQLClientMessages.getString("sql.executionFetchTime"), execTime,
							totalTime - execTime), false);
					rowsCount.setText(MessageFormat.format(
							SQLClientMessages.getString("sql.fetchedRows"), resultCount));
				} else {
					setStatusLabel(MessageFormat.format(
							SQLClientMessages.getString("sql.updateTime"), totalTime), false);

					if (resultCount > 0) {
						rowsCount.setText(MessageFormat.format(
								SQLClientMessages.getString("sql.updatedRows"), resultCount));
					} else {
						rowsCount.setText(SQLClientMessages.getString("sql.queryOk"));
					}
				}
				if (query.hasMoreRows()) {
					sqlViewer.setItemCount(resultCount + 1);
				} else {
					query.dispose();
					query.removeQueryListener(SQLResultConnector.this);
				}
			}
		});
		UIJob j = new UIJob("Auto-sizing columns") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				adjusteColumnSizes();
				return Status.OK_STATUS;
			}
		};
		j.schedule(500);
	}

	public void adjusteColumnSizes() {
		for (TableColumn c : sqlView.getColumns()) {
			c.pack();
		}
	}

	@Override
	public void queryResultMetadataAvailable(final ISQLQuery query, final long executionTime,
			final INextepMetadata md) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				initializeTable(query, executionTime, md);
			}
		});
	}

	private void initializeTable(ISQLQuery query, long executionTime, INextepMetadata md) {
		colMaxWidth = new int[md.getColumnCount() + 1];
		// Checking columns to display
		int displayedColumns = query.getDisplayedColumnsCount();
		if (displayedColumns <= 0) {
			displayedColumns = md.getColumnCount();
		}
		for (int i = 1; i <= displayedColumns; i++) {
			final String colName = md.getColumnName(i);
			// final int colPrecision = md.getPrecision(index);
			final int colType = md.getColumnType(i);

			final int colIndex = i - 1;
			if (!sqlView.isDisposed()) {
				TableColumn c = new TableColumn(sqlView, SWT.NONE);
				c.setText(colName);
				c.setWidth(colName.length() * 8);
				c.addListener(SWT.Selection, comparator);
				colMaxWidth[colIndex] = c.getWidth();
				SQLResultLabelProvider lblProvider = new SQLResultLabelProvider(this, colIndex);
				final TableViewerColumn viewerCol = new TableViewerColumn(sqlViewer, c);
				viewerCol.setLabelProvider(lblProvider);
				// Registering column comparator
				switch (colType) {
				case Types.BIGINT:
				case Types.BIT:
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.INTEGER:
				case Types.NUMERIC:
				case Types.REAL:
				case Types.SMALLINT:
				case Types.TINYINT:
					c.setData(SQLResultViewerComparator.KEY_COMPARATOR, new NumberComparator());
					// Setting our deserializer which can produce number from string
					c.setData(KEY_DESERIALIZER, NUMBER_DESERIALIZER);
					break;
				case Types.DATE:
				case Types.TIME:
				case Types.TIMESTAMP:
					// Defines validation so that user will be notified about problems
					c.setData(KEY_CELL_VALIDATOR, DATE_VALIDATOR);
					// Setting our deserializer which can produce date from string
					c.setData(KEY_DESERIALIZER, DATE_DESERIALIZER);
					c.setData(SQLResultViewerComparator.KEY_COMPARATOR, new DateComparator());
					break;
				default:
					c.setData(KEY_DESERIALIZER, STRING_DESERIALIZER);
					c.setData(SQLResultViewerComparator.KEY_COMPARATOR, new StringComparator());
				}
			}
		}
		registerCellEditors();
		setStatusLabel(MessageFormat.format(SQLClientMessages.getString("sql.executionTime"),
				executionTime), false);
		sqlViewer.setInput(query.getResult());
	}

	@Override
	public void queryFailed(ISQLQuery query, final Exception e) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				setStatusLabel("Query failed: " + e.getMessage(), true);
			}
		});
	}

	/**
	 * Refreshes the status label from this SQL result component.
	 * 
	 * @param status the message to display
	 * @param error whether this is an error message or information
	 */
	private void setStatusLabel(String status, boolean error) {
		if (error) {
			timeLabel.setImage(ImageFactory.ICON_ERROR_TINY);
			timeLabel.setForeground(FontFactory.ERROR_COLOR);
		} else {
			timeLabel.setImage(null);
			timeLabel.setForeground(FontFactory.BLACK);
		}
		timeLabel.setText(status == null ? "" : status);
	}

	public ISQLQuery getSQLQuery() {
		return currentQuery;
	}

	public TableViewer getTableViewer() {
		return sqlViewer;
	}

	public void setEditableState(boolean isEditable) {
		modifier.setModifiable(isEditable);
		this.isEditable = isEditable;
	}

	public boolean getEditableState() {
		return isEditable;
	}

	private void registerCellEditors() {
		List<CellEditor> editors = new ArrayList<CellEditor>();
		List<String> colProperties = new ArrayList<String>();
		int i = 0;
		for (TableColumn c : sqlView.getColumns()) {
			final TextCellEditor editor = new TextCellEditor(sqlView);
			final ICellEditorValidator validator = (ICellEditorValidator) c
					.getData(KEY_CELL_VALIDATOR);
			if (validator != null) {
				editor.setValidator(validator);
				// This editor listener handles the refresh of the status bar on validation of the
				// user edition
				editor.addListener(new ICellEditorListener() {

					@Override
					public void editorValueChanged(boolean oldValidState, boolean newValidState) {
						if (!newValidState) {
							setStatusLabel(editor.getErrorMessage(), true);
						} else {
							setStatusLabel(null, false);
						}
					}

					@Override
					public void cancelEditor() {
					}

					@Override
					public void applyEditorValue() {

					}
				});
			}
			editors.add(editor);
			colProperties.add(String.valueOf(i++));
		}
		sqlViewer.setCellEditors(editors.toArray(new CellEditor[editors.size()]));
		sqlViewer.setCellModifier(modifier);
		sqlViewer.setColumnProperties(colProperties.toArray(new String[colProperties.size()]));
	}
}
