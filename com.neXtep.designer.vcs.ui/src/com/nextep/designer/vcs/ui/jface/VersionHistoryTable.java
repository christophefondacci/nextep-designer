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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.nextep.designer.vcs.ui.VCSUIMessages;

public class VersionHistoryTable {

	public static Table create(Composite parent) {
		Table historyTable = new Table(parent, SWT.FULL_SELECTION | SWT.BORDER);
		historyTable.setHeaderVisible(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 200;
		historyTable.setLayoutData(gd);
		historyTable.setLinesVisible(true);
		TableColumn releaseColumn = new TableColumn(historyTable, SWT.NONE);
		releaseColumn.setText(VCSUIMessages.getString("version.history.release")); //$NON-NLS-1$
		releaseColumn.setWidth(80);
		TableColumn branchColumn = new TableColumn(historyTable, SWT.NONE);
		branchColumn.setText(VCSUIMessages.getString("version.history.branch")); //$NON-NLS-1$
		branchColumn.setWidth(80);
		TableColumn statusColumn = new TableColumn(historyTable, SWT.NONE);
		statusColumn.setText(VCSUIMessages.getString("version.history.status")); //$NON-NLS-1$
		statusColumn.setWidth(100);
		TableColumn releaseDateColumn = new TableColumn(historyTable, SWT.NONE);
		releaseDateColumn.setText(VCSUIMessages.getString("version.history.created")); //$NON-NLS-1$
		releaseDateColumn.setWidth(130);
		TableColumn authorColumn = new TableColumn(historyTable, SWT.NONE);
		authorColumn.setText(VCSUIMessages.getString("version.history.by")); //$NON-NLS-1$
		authorColumn.setWidth(130);
		TableColumn activityColumn = new TableColumn(historyTable, SWT.NONE);
		activityColumn.setText(VCSUIMessages.getString("version.history.activity")); //$NON-NLS-1$
		activityColumn.setWidth(300);
		return historyTable;
	}
}
