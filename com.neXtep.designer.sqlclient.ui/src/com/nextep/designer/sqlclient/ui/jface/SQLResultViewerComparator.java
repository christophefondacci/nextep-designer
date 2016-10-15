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
package com.nextep.designer.sqlclient.ui.jface;

import java.util.Comparator;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;

public class SQLResultViewerComparator extends ViewerComparator implements Listener,
		Comparator<ISQLRowResult> {

	public final static String KEY_COMPARATOR = "comparator";
	private TableViewer viewer;
	private int sortColumn = 1;
	private final static Comparator<String> STR_COMPARATOR = new StringComparator();
	private Comparator currentComparator;
	private ITableLabelProvider labelProvider;
	private int sortOrder = SWT.UP;

	public SQLResultViewerComparator(TableViewer viewer) {
		this.viewer = viewer;
		currentComparator = STR_COMPARATOR;
		// labelProvider = (ITableLabelProvider) ((TableViewer) viewer).getLabelProvider();
	}

	@Override
	public void handleEvent(Event e) {
		final Table table = viewer.getTable();
		TableColumn column = (TableColumn) e.widget;
		sortOrder = SWT.UP;
		if (table.getSortColumn() == column) {
			// Inverting order
			sortOrder = table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
		}
		table.setSortColumn(column);
		table.setSortDirection(sortOrder);
		currentComparator = (Comparator<?>) column.getData(KEY_COMPARATOR);
		if (currentComparator == null) {
			currentComparator = STR_COMPARATOR;
		}
		sortColumn = table.indexOf(column);
		SQLResultContentProvider provider = (SQLResultContentProvider) viewer.getContentProvider();
		provider.sortRows(this);
		viewer.refresh();
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		final ISQLRowResult r1 = (ISQLRowResult) e1;
		final ISQLRowResult r2 = (ISQLRowResult) e2;
		return compare(r1, r2);
	}

	private String notNull(String s) {
		return s == null ? "" : s;
	}

	@Override
	public int compare(ISQLRowResult r1, ISQLRowResult r2) {
		final Object r1Value = r1.getValues().get(sortColumn);
		final Object r2Value = r2.getValues().get(sortColumn);

		int comparisonResult = 0;
		if (currentComparator instanceof StringComparator) {
			final String label1 = notNull(SQLResultLabelProvider.getColumnText(r1, sortColumn));
			final String label2 = notNull(SQLResultLabelProvider.getColumnText(r2, sortColumn));
			comparisonResult = currentComparator.compare(label1, label2);
		} else {
			comparisonResult = currentComparator.compare(r1Value, r2Value);
		}
		if (sortOrder == SWT.DOWN) {
			comparisonResult = -comparisonResult;
		}
		return comparisonResult;
	}
}
