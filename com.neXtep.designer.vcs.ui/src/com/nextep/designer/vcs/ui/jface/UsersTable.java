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


public final class UsersTable {

	/**
	 * Builds the SWT table which displays user information. The returned table is always compatible
	 * with the JFace viewers for the element.
	 * 
	 * @param parent SWT parent container
	 * @return the SWT table
	 */
	public static Table createTable(Composite parent) {
		final Table t = new Table(parent,SWT.BORDER);
		GridData gd = new GridData(SWT.FILL,SWT.FILL,true,true);
		gd.widthHint=350;
		gd.heightHint=200;
		gd.verticalSpan=4;
		t.setLayoutData(gd);
		t.setLinesVisible(true);
		t.setHeaderVisible(true);
		TableColumn loginCol = new TableColumn(t, SWT.NONE);
		loginCol.setWidth(150);
		loginCol.setText("Login");
		TableColumn nameCol = new TableColumn(t, SWT.NONE);
		nameCol.setWidth(200);
		nameCol.setText("Full name");
		TableColumn adminCol = new TableColumn(t, SWT.NONE);
		adminCol.setAlignment(SWT.CENTER);
		adminCol.setWidth(30);
		adminCol.setText("Admin");
		return t;
	}
	
	public static Table createUserRights(Composite parent) {
		final Table t = new Table(parent,SWT.BORDER);
		GridData gd = new GridData(SWT.FILL,SWT.FILL,true,true);
		t.setLayoutData(gd);
		t.setLinesVisible(true);
		t.setHeaderVisible(true);
		TableColumn rightCol = new TableColumn(t, SWT.NONE);
		rightCol.setWidth(120);
		rightCol.setText("Name");
		TableColumn grantedCol = new TableColumn(t, SWT.NONE);
		grantedCol.setWidth(50);
		grantedCol.setAlignment(SWT.CENTER);
		grantedCol.setText("Assigned");
		return t;
	}
}
