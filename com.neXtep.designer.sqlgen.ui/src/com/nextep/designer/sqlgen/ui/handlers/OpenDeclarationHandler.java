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
package com.nextep.designer.sqlgen.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import com.nextep.designer.sqlgen.ui.editors.SQLHyperlinkDetector;

public class OpenDeclarationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchSite s = HandlerUtil.getActiveSite(event);
		ISelectionProvider provider = s.getSelectionProvider();
		ISelection sel = provider.getSelection();
		if(sel instanceof ITextSelection) {
			ITextSelection textSel = (ITextSelection)sel;
			// Retrieving document
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			if(editor instanceof MultiPageEditorPart) {
				editor = (IEditorPart)((MultiPageEditorPart)editor).getSelectedPage();
			}
			if(editor instanceof ITextEditor) {
				IDocument doc = ((ITextEditor)editor).getDocumentProvider().getDocument(editor.getEditorInput());
				// Retrieving hyperlink, just as we would do for hyperlinking
				IHyperlink[] links = SQLHyperlinkDetector.detectHyperlinks(doc, new Region(textSel.getOffset(),textSel.getLength()));
				// Activating first link
				if(links!=null && links.length>0) {
					links[0].open();
				}
			}
		}
		return null;
	}

}
