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
package com.nextep.designer.synch.ui.jface;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.compare.IComparisonEditorProvider;

public class CompareAction extends Action {

	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;
	private final static Log LOGGER = LogFactory.getLog(CompareAction.class);

	public CompareAction(IWorkbenchPage page, ISelectionProvider selProvider) {
		super(SynchUIMessages.getString("synch.navigator.openComparison")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selProvider;
	}

	@Override
	public boolean isEnabled() {
		final ISelection sel = selectionProvider.getSelection();
		if (!sel.isEmpty()) {
			final IStructuredSelection selection = (IStructuredSelection) sel;
			return selection.size() == 1 && selection.getFirstElement() instanceof IComparisonItem;
		}
		return false;
	}

	@Override
	public void run() {
		final IStructuredSelection sel = (IStructuredSelection) selectionProvider.getSelection();
		IComparisonItem compItem = (IComparisonItem) sel.getFirstElement();
		final IComparisonEditorProvider provider = VCSUIPlugin.getComparisonUIManager()
				.getComparisonEditorProvider(compItem.getType());
		try {
			final IEditorInput input = provider.getEditorInput(compItem);
			final String editorId = provider.getEditorId(compItem);
			if (input != null && editorId != null) {
				// Opening our multi editor
				page.openEditor(input, editorId);
			}
		} catch (PartInitException e) {
			LOGGER.error(SynchUIMessages.getString("synch.navigator.openComparisonError") + ": "
					+ e.getMessage(), e);
		}
	}
}
