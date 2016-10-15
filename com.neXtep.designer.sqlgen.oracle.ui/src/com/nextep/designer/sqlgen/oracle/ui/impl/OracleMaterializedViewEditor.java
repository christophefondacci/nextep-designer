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
package com.nextep.designer.sqlgen.oracle.ui.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.model.IResourceLocator;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.sqlgen.ui.editors.SQLEditor;
import com.nextep.designer.ui.factories.ImageFactory;

public class OracleMaterializedViewEditor extends TypedFormRCPEditor {

	private static final Log log = LogFactory.getLog(OracleMaterializedViewEditor.class);
	public static final String EDITOR_ID = "com.neXtep.designer.sqlgen.oracle.ui.matViewEditor"; //$NON-NLS-1$
	private SQLEditor editor;

	@Override
	protected void addPages() {
		super.addPages();
		try {
			editor = new SQLEditor();
			final int page = addPage(editor, new MaterializedViewEditorInput(
					(IMaterializedView) getModel()));
			final IResourceLocator locator = IElementType.getInstance(ISQLScript.TYPE_ID)
					.getTinyIcon();
			final Image img = ImageFactory.getImage(locator);
			setPageImage(page, img);
			setPageText(page, "SQL"); //$NON-NLS-1$
		} catch (PartInitException e) {
			log.error("Unable to create materialized view SQL edition tab: " + e.getMessage(), e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (editor != null) {
			editor.doSave(monitor);
		}
	}

	@Override
	public void doSaveAs() {
		if (editor != null) {
			editor.doSaveAs();
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
