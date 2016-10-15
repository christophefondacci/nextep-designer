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
package com.nextep.designer.ui.markers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class MarkersTable {

	public static Table create(Composite parent) {
		final Table t = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 500;
		data.heightHint = 300;
		t.setLayoutData(data);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		TableColumn c = new TableColumn(t, SWT.NONE);
		c.setText("Description");
		c.setWidth(350);
		// TableColumn c2 = new TableColumn(t,SWT.NONE);
		// c2.setText("Type");
		// c2.setWidth(60);
		TableColumn c3 = new TableColumn(t, SWT.NONE);
		c3.setText("Element");
		c3.setWidth(100);
		TableColumn c4 = new TableColumn(t, SWT.NONE);
		c4.setText("Element type");
		c4.setWidth(70);
		TableColumn c5 = new TableColumn(t, SWT.NONE);
		c5.setText("Line");
		c5.setWidth(35);
		TableColumn c6 = new TableColumn(t, SWT.NONE);
		c6.setText("Context");
		c6.setToolTipText("Informs about the context for which this marker has been found");
		c6.setWidth(300);
		t.setSortColumn(c3);
		return t;
	}
}
