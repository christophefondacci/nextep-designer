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
package com.nextep.designer.data.ui.editors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.impl.rcp.RCPTypedEditor;
import com.nextep.designer.data.ui.DataUiMessages;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.ui.factories.ImageFactory;

public class DataSetMultiEditor extends MultiPageEditorPart {

	private final static Log LOGGER = LogFactory.getLog(DataSetMultiEditor.class);
	public final static String EDITOR_ID = "com.neXtep.designer.data.ui.dataSetMultiEditor"; //$NON-NLS-1$

	@Override
	protected void createPages() {
		DataSetSQLEditorInput input = (DataSetSQLEditorInput) getEditorInput();
		try {
			int index = addPage(new DataSetSQLEditor(), input);
			setPageText(index, DataUiMessages.getString("editor.dataset.dataTab")); //$NON-NLS-1$
			final IDataSet set = input.getDataSet();
			TypedEditorInput typedInput = new TypedEditorInput(set);
			index = addPage(new RCPTypedEditor(), typedInput);
			setPageText(index, DataUiMessages.getString("editor.dataset.definitionTab")); //$NON-NLS-1$
			setPartName(set.getName());
			setTitleImage(ImageFactory.getImage(set.getType().getIcon()));
		} catch (PartInitException e) {
			LOGGER.error(
					DataUiMessages.getString("editor.dataset.partInitException") + e.getMessage(), e); //$NON-NLS-1$
		}

	}

	@Override
	public void doSave(IProgressMonitor monitor) {

	}

	@Override
	public void doSaveAs() {

	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
