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
package com.nextep.datadesigner.beng.gui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * A helper which builds the delivery list table.
 * (because SWT does not allow table subclassing)
 * @author Christophe
 *
 */
public class DeliveryListTable {

	public static Table create(Composite parent) {
		Table dlvTab = new Table(parent,SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		dlvTab.setHeaderVisible(true);
		final TableColumn nameCol = new TableColumn(dlvTab,SWT.NONE);
		nameCol.setWidth(200);
		nameCol.setText("Name");
		final TableColumn moduleCol = new TableColumn(dlvTab,SWT.NONE);
		moduleCol.setWidth(100);
		moduleCol.setText("Module");
		final TableColumn srcReleaseCol = new TableColumn(dlvTab,SWT.NONE);
		srcReleaseCol.setWidth(70);
		srcReleaseCol.setText("From");
		final TableColumn releaseCol = new TableColumn(dlvTab,SWT.NONE);
		releaseCol.setWidth(70);
		releaseCol.setText("Release");
		final TableColumn  branchCol = new TableColumn(dlvTab,SWT.NONE);
		branchCol.setWidth(70);
		branchCol.setText("Branch");
		return dlvTab;
	}
}
