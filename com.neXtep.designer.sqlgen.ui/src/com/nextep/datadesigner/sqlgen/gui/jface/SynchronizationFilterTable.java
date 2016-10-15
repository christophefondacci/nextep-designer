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
package com.nextep.datadesigner.sqlgen.gui.jface;



import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class SynchronizationFilterTable {

	public static Table create(Composite parent) {
		Table t = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		TableColumn nameCol = new TableColumn(t,SWT.NONE);
		nameCol.setText("Filtered expression");
		nameCol.setWidth(250);
		TableColumn typeCol = new TableColumn(t,SWT.NONE);
		typeCol.setText("Filtered type");
		typeCol.setWidth(150);
		t.setHeaderVisible(true);
		return t;
	}
}
