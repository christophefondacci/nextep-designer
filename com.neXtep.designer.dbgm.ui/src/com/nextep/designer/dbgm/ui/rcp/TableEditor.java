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
package com.nextep.designer.dbgm.ui.rcp;

import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.impl.rcp.RCPTypedEditor;
import com.nextep.designer.dbgm.gef.TableOutline;

/**
 * @author Christophe Fondacci
 *
 */
public class TableEditor extends RCPTypedEditor {

	public static final String EDITOR_ID = "com.neXtep.designer.dbgm.ui.tableEditor";
	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if(adapter == IContentOutlinePage.class) {
			if(getEditorInput() instanceof TypedEditorInput) {
				return new TableOutline( (IBasicTable)((TypedEditorInput)getEditorInput()).getModel());
			}
		}
		return super.getAdapter(adapter);
	}
}
