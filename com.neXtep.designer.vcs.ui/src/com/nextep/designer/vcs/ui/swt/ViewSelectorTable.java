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
package com.nextep.designer.vcs.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * A helper which initializes the table control for the view selector dialog.
 * 
 * @author Christophe Fondacci
 */
public final class ViewSelectorTable {

	private ViewSelectorTable() {
	}

	public static Table create(Composite parent) {
		final Table t = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.horizontalSpan = 3;
		gridData1.verticalAlignment = GridData.FILL;
		gridData1.minimumHeight = 250;
		gridData1.heightHint = 300;
		t.setLayoutData(gridData1);
		TableColumn viewName = new TableColumn(t, SWT.NONE);
		viewName.setText(VCSUIMessages.getString("table.viewSelector.workspaceCol")); //$NON-NLS-1$
		viewName.setWidth(150);
		t.setSortColumn(viewName);
		t.setSortDirection(SWT.UP);
		TableColumn viewType = new TableColumn(t, SWT.NONE);
		viewType.setText(VCSUIMessages.getString("table.viewSelector.vendorCol")); //$NON-NLS-1$
		viewType.setWidth(70);
		TableColumn viewDescription = new TableColumn(t, SWT.NONE);
		viewDescription.setText(VCSUIMessages.getString("table.viewSelector.descCol")); //$NON-NLS-1$
		viewDescription.setWidth(190);
		TableColumn viewDel = new TableColumn(t, SWT.NONE);
		viewDel.setWidth(50);
		viewDel.setText(VCSUIMessages.getString("table.viewSelector.delCol")); //$NON-NLS-1$
		return t;
	}
}
