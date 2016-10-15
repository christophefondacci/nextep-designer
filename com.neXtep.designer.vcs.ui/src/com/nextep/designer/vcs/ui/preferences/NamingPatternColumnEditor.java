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
package com.nextep.designer.vcs.ui.preferences;

import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;

public class NamingPatternColumnEditor extends ColumnViewerEditor {

	protected NamingPatternColumnEditor(ColumnViewer viewer,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		super(viewer, editorActivationStrategy, feature);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setEditor(Control w, Item item, int columnNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setLayoutData(LayoutData layoutData) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateFocusCell(ViewerCell focusCell,
			ColumnViewerEditorActivationEvent event) {
		// TODO Auto-generated method stub

	}

}
