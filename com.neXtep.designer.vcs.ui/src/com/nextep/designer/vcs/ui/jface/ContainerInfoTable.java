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
package com.nextep.designer.vcs.ui.jface;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ContainerInfoTable {
	/**
	 * Create a new container info table compatible with jface components
	 * 
	 * @param parent
	 * @return
	 */
	public static Table create(Composite parent) {
		final Table containerTable = new Table(parent,SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		containerTable.setHeaderVisible(true);
		TableColumn nameCol = new TableColumn(containerTable,SWT.NONE);
		nameCol.setWidth(200);
		nameCol.setText("Name");
		TableColumn releaseCol = new TableColumn(containerTable,SWT.NONE);
		releaseCol.setWidth(70);
		releaseCol.setText("Release");
		TableColumn vendorCol = new TableColumn(containerTable,SWT.NONE);
		vendorCol.setWidth(70);
		vendorCol.setText("Vendor");
		TableColumn branchCol = new TableColumn(containerTable,SWT.NONE);
		branchCol.setWidth(70);
		branchCol.setText("Branch");
		TableColumn dateCol = new TableColumn(containerTable,SWT.NONE);
		dateCol.setWidth(120);
		dateCol.setText("Date");
		return containerTable;
	}
}
