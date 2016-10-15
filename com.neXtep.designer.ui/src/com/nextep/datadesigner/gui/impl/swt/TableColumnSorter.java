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
package com.nextep.datadesigner.gui.impl.swt;

import java.text.Collator;
import java.util.Locale;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import com.nextep.datadesigner.model.INamedObject;

/**
 * A generic sorter which handles sort on any column of a Table managed by a table viewer. It
 * handles listeners on columns, proper column sort display, reordering.<br>
 * This sorter is text-based that means that it will sort the information based on the label of
 * elements in the columns. Label is fetched from the viewer's label provider.
 * 
 * @author Christophe Fondacci
 */
public class TableColumnSorter extends ViewerComparator implements Listener {

	private Table table;
	private Viewer viewer;

	public TableColumnSorter(Table table, Viewer viewer) {
		this.table = table;
		// Adding column listener
		for (TableColumn c : table.getColumns()) {
			c.addListener(SWT.Selection, this);
		}
		this.viewer = viewer;
	}

	@Override
	public void handleEvent(Event e) {
		TableColumn column = (TableColumn) e.widget;
		int sortOrder = SWT.UP;
		if (table.getSortColumn() == column) {
			// Inverting order
			sortOrder = table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP;
		}
		table.setSortColumn(column);
		table.setSortDirection(sortOrder);
		viewer.refresh();
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (table.getSortColumn() == null)
			return super.compare(viewer, e1, e2);
		Collator collator = Collator.getInstance(Locale.getDefault());
		final int col = table.indexOf(table.getSortColumn());
		final String col1Label = getLabel((StructuredViewer) viewer, e1, col);
		final String col2Label = getLabel((StructuredViewer) viewer, e2, col);

		final String label1 = notNull(col1Label);
		final String label2 = notNull(col2Label);

		return (table.getSortDirection() == SWT.UP ? collator.compare(label1, label2) : collator
				.compare(label2, label1));
	}

	private String getLabel(StructuredViewer viewer, Object model, int index) {
		IBaseLabelProvider provider = viewer.getLabelProvider();
		if (provider instanceof ITableLabelProvider) {
			return ((ITableLabelProvider) provider).getColumnText(model, index);
		} else if (provider instanceof ILabelProvider) {
			return ((ILabelProvider) provider).getText(model);
		} else if (model instanceof INamedObject) {
			return ((INamedObject) model).getName();
		} else {
			return model.toString();
		}
	}

	private String notNull(String s) {
		return s == null ? "" : s;
	}
}
