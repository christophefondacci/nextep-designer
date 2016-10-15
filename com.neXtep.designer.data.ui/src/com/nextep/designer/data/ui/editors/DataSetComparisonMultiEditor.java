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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.designer.data.ui.DataUiMessages;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.ui.VCSImages;

public class DataSetComparisonMultiEditor extends MultiPageEditorPart {

	private final static Log LOGGER = LogFactory.getLog(DataSetComparisonMultiEditor.class);
	public final static String EDITOR_ID = "com.neXtep.designer.sqlclient.ui.dataset.multiComparisonEditor"; //$NON-NLS-1$

	@Override
	protected void createPages() {
		final DataDeltaEditorInput input = (DataDeltaEditorInput) getEditorInput();
		final IDataDelta delta = input.getModel();
		final IDataSet insertSet = delta.getAddedDataSet();
		setPartName(input.getName());
		if (delta.hasAdditions()) {
			try {
				final IEditorInput insertInput = new TypedEditorInput(insertSet);
				final IEditorPart insertEditor = new DataSetComparisonEditor();
				int index = addPage(insertEditor, insertInput);
				setPageText(index, DataUiMessages.getString("editor.dataset.comparison.additionsTab")); //$NON-NLS-1$
				setPageImage(index, VCSImages.ICON_DIFF_ADDED);
			} catch (PartInitException e) {
				LOGGER.error(DataUiMessages.getString("editor.dataset.comparison.partInitException") + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		if (delta.hasUpdates()) {
			final IDataSet updateSet = delta.getUpdatedDataSet();
			try {
				final IEditorInput updateInput = new TypedEditorInput(updateSet);
				final IEditorPart updateEditor = new DataSetComparisonEditor();
				int index = addPage(updateEditor, updateInput);
				setPageText(index, DataUiMessages.getString("editor.dataset.comparison.updatesTab")); //$NON-NLS-1$
				setPageImage(index, VCSImages.ICON_DIFF_CHANGED);
			} catch (PartInitException e) {
				LOGGER.error(DataUiMessages.getString("editor.dataset.comparison.partInitException") + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		if (delta.hasDeletions()) {
			final IDataSet deleteSet = delta.getDeletedDataSet();
			try {
				final IEditorInput deleteInput = new TypedEditorInput(deleteSet);
				final IEditorPart deleteEditor = new DataSetComparisonEditor();
				int index = addPage(deleteEditor, deleteInput);
				setPageText(index, DataUiMessages.getString("editor.dataset.comparison.deletionsTab")); //$NON-NLS-1$
				setPageImage(index, VCSImages.ICON_DIFF_REMOVED);
			} catch (PartInitException e) {
				LOGGER.error(DataUiMessages.getString("editor.dataset.comparison.partInitException") + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		// If no data changed
		if (!delta.hasAdditions() && !delta.hasUpdates() && !delta.hasDeletions()) {
			final IDataSet updateSet = delta.getUpdatedDataSet();
			try {
				final IEditorInput updateInput = new TypedEditorInput(updateSet);
				final IEditorPart updateEditor = new DataSetComparisonEditor();
				int index = addPage(updateEditor, updateInput);
				setPageText(index, DataUiMessages.getString("editor.dataset.comparison.noChange")); //$NON-NLS-1$
				setPageImage(index, VCSImages.ICON_EQUALS);
			} catch (PartInitException e) {
				LOGGER.error(DataUiMessages.getString("editor.dataset.comparison.partInitException") + e.getMessage(), e); //$NON-NLS-1$
			}
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
