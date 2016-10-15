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
/**
 *
 */
package com.nextep.datadesigner.gui.impl;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * A selection provider which publish the selected
 * table item's data to the current workarea selection.
 *
 * @author Christophe Fondacci
 *
 */
public class TableSelectionProvider extends AbstractSelectionProvider implements ISelectionProvider, SelectionListener {

	private Table table;

	private TableSelectionProvider(Table table) {
		this.table=table;
		// Registering as a listener
		table.addSelectionListener(this);
	}
	public static ISelectionProvider handle(Table t) {
		return new TableSelectionProvider(t);
	}
	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		TableItem[] sel = table.getSelection();
		List<Object> selectedModels = new ArrayList<Object>();
		for(TableItem i : sel) {
			selectedModels.add(i.getData());
		}
		StructuredSelection s = new StructuredSelection(selectedModels);
		return s;
	}

}
