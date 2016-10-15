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
package com.nextep.designer.sqlgen.ui;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import com.nextep.designer.sqlgen.ui.actions.SubmitSQLAction;
import com.nextep.designer.sqlgen.ui.editors.SQLMultiEditor;

public class SQLEditorActionBarContributor extends TextEditorActionContributor {

	private RetargetTextEditorAction fContentAssist;

	public SQLEditorActionBarContributor() {
		super();
		fContentAssist = new RetargetTextEditorAction(SQLMessages.getResourceBundle(),
				"ContentAssist", "ContentAssist"); //$NON-NLS-1$//$NON-NLS-2$
		fContentAssist
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		// getActionBars().setGlobalActionHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS,
		// fContentAssist);
	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void contributeToMenu(IMenuManager menuManager) {
		super.contributeToMenu(menuManager);
		IMenuManager editMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			editMenu.add(new Separator());
			editMenu.add(fContentAssist);
		}
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		super.contributeToToolBar(toolBarManager);
		toolBarManager.add(new SubmitSQLAction());
		toolBarManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		IAction editorAction = null;
		if (targetEditor instanceof ITextEditor) {
			editorAction = getAction((ITextEditor) targetEditor, "ContentAssist"); //$NON-NLS-1$
		} else if (targetEditor instanceof SQLMultiEditor) {
			editorAction = getAction(((SQLMultiEditor) targetEditor).getCurrentEditor(),
					"ContentAssist"); //$NON-NLS-1$
		}
		fContentAssist.setAction(editorAction);
		getActionBars().setGlobalActionHandler("ContentAssist", editorAction); //$NON-NLS-1$
	}

}
