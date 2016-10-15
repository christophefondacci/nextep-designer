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
package com.nextep.designer.dbgm.ui.jface;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.nextep.designer.dbgm.ui.DBGMUIMessages;

/**
 * 
 * @author Christophe Fondacci
 */
public class DomainTable {

	public static Table create(Composite parent) {
		Table t = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		t.setLinesVisible(true);
		t.setHeaderVisible(true);
		TableColumn selCol = new TableColumn(t, SWT.NONE);
		selCol.setWidth(20);
		selCol.setText("#"); //$NON-NLS-1$
		TableColumn nameCol = new TableColumn(t, SWT.NONE);
		nameCol.setWidth(100);
		nameCol.setText(DBGMUIMessages.getString("editor.domain.nameCol")); //$NON-NLS-1$
		TableColumn descCol = new TableColumn(t, SWT.NONE);
		descCol.setWidth(200);
		descCol.setText(DBGMUIMessages.getString("editor.domain.descCol")); //$NON-NLS-1$
		TableColumn lengthCol = new TableColumn(t, SWT.NONE);
		lengthCol.setWidth(80);
		lengthCol.setText(DBGMUIMessages.getString("editor.domain.lengthCol")); //$NON-NLS-1$
		TableColumn precisionCol = new TableColumn(t, SWT.NONE);
		precisionCol.setWidth(80);
		precisionCol.setText(DBGMUIMessages.getString("editor.domain.precisionCol")); //$NON-NLS-1$
		return t;
	}

	public static Table createDomainType(Composite parent) {
		Table t = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
		t.setLinesVisible(true);
		t.setHeaderVisible(true);
		TableColumn vendorCol = new TableColumn(t, SWT.NONE);
		vendorCol.setWidth(100);
		vendorCol.setText(DBGMUIMessages.getString("editor.domain.vendor")); //$NON-NLS-1$
		TableColumn typeCol = new TableColumn(t, SWT.NONE);
		typeCol.setWidth(200);
		typeCol.setText(DBGMUIMessages.getString("editor.domain.nameCol")); //$NON-NLS-1$
		TableColumn lengthCol = new TableColumn(t, SWT.NONE);
		lengthCol.setWidth(60);
		lengthCol.setText(DBGMUIMessages.getString("editor.domain.lengthCol")); //$NON-NLS-1$
		TableColumn precisionCol = new TableColumn(t, SWT.NONE);
		precisionCol.setWidth(60);
		precisionCol.setText(DBGMUIMessages.getString("editor.domain.precisionCol")); //$NON-NLS-1$
		return t;
	}
}
