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

public class VersionableVersionStatusTable {

	public static Table create(Composite parent, int style, int span) {
		Table t = new Table(parent, style);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 250;
		gd.horizontalSpan = span;
		t.setLayoutData(gd);
		TableColumn nameCol = new TableColumn(t, SWT.NONE);
		nameCol.setText(VCSUIMessages.getString("versionable.status.name")); //$NON-NLS-1$
		nameCol.setWidth(100);
		TableColumn versCol = new TableColumn(t, SWT.CENTER);
		versCol.setText(VCSUIMessages.getString("versionable.status.release")); //$NON-NLS-1$
		versCol.setWidth(70);
		TableColumn branchCol = new TableColumn(t, SWT.CENTER);
		branchCol.setText(VCSUIMessages.getString("versionable.status.branch")); //$NON-NLS-1$
		branchCol.setWidth(60);
		TableColumn dateCol = new TableColumn(t, SWT.CENTER);
		dateCol.setText(VCSUIMessages.getString("versionable.status.createdOn")); //$NON-NLS-1$
		dateCol.setWidth(80);
		TableColumn byCol = new TableColumn(t, SWT.NONE);
		byCol.setText(VCSUIMessages.getString("versionable.status.createdBy")); //$NON-NLS-1$
		byCol.setWidth(80);
		TableColumn activityCol = new TableColumn(t, SWT.NONE);
		activityCol.setText(VCSUIMessages.getString("versionable.status.activity")); //$NON-NLS-1$
		activityCol.setWidth(100);
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		return t;
	}
}
