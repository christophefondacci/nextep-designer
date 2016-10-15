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
package com.nextep.datadesigner.sqlgen.gui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;
import com.nextep.datadesigner.vcs.gui.rcp.TextEditorInput;

public class FileTextDocumentProvider extends AbstractDocumentProvider {

	@Override
	protected IAnnotationModel createAnnotationModel(Object element)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument doc = new Document();
		if(element instanceof TextEditorInput) {
			doc.set(((TextEditorInput)element).getText());
		}
		return doc;
	}

	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element,
			IDocument document, boolean overwrite) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadOnly(Object element) {
		return true;
	}
	@Override
	public boolean isModifiable(Object element) {
		return false;
	}
}
