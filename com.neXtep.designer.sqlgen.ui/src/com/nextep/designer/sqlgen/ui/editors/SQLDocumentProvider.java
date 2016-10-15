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
package com.nextep.designer.sqlgen.ui.editors;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.vcs.services.VersionHelper;

/**
 * @author Christophe Fondacci
 */
public class SQLDocumentProvider extends AbstractDocumentProvider {

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
	 */
	@Override
	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		//
		return new ProjectionAnnotationModel();
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(java.lang.Object)
	 */
	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = new Document();
		if (element instanceof ISQLEditorInput<?>) {
			ISQLEditorInput<?> input = (ISQLEditorInput<?>) element;
			document.set(input.getSql());
		} else if (element instanceof FileStoreEditorInput) {
			setDocumentContent(document, (IEditorInput) element);
		}
		IDocumentPartitioner partitioner = new FastPartitioner(new SQLPartitionScanner(DBGMHelper
				.getCurrentVendor()),
				new String[] { SQLPartitionScanner.SINGLECOMMENT, SQLPartitionScanner.COMMENT,
						SQLPartitionScanner.STRING, SQLPartitionScanner.PROMPT });
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);

		return document;
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#doSaveDocument(org.eclipse.core.runtime.IProgressMonitor,
	 *      java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	@Override
	protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document,
			boolean overwrite) throws CoreException {
		if (element instanceof ISQLEditorInput<?>) {
			ISQLEditorInput<?> input = (ISQLEditorInput<?>) element;
			input.setSql(document.get());
			input.save(this);
		}
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#getOperationRunner(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IRunnableContext getOperationRunner(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#isReadOnly(java.lang.Object)
	 */
	@Override
	public boolean isReadOnly(Object element) {
		return !isModifiable(element);
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#isModifiable(java.lang.Object)
	 */
	@Override
	public boolean isModifiable(Object element) {
		if (element instanceof ISQLEditorInput<?>) {
			Object s = ((ISQLEditorInput<?>) element).getModel();
			return VersionHelper.ensureModifiable(s, false);
		} else {
			return true;
		}
		// return super.isModifiable(element);
	}

	/*************************************************
	 * TEST SECTION
	 ************************************************/
	/**
	 * Tries to read the file pointed at by <code>input</code> if it is an
	 * <code>IPathEditorInput</code>. If the file does not exist, <code>true</code> is returned.
	 * 
	 * @param document the document to fill with the contents of <code>input</code>
	 * @param input the editor input
	 * @return <code>true</code> if setting the content was successful or no file exists,
	 *         <code>false</code> otherwise
	 * @throws CoreException if reading the file fails
	 */
	private boolean setDocumentContent(IDocument document, IEditorInput input) throws CoreException {
		// XXX handle encoding
		Reader reader;
		try {
			if (input instanceof IPathEditorInput)
				reader = new FileReader(((IPathEditorInput) input).getPath().toFile());
			else if (input instanceof FileStoreEditorInput) {
				reader = new FileReader(((FileStoreEditorInput) input).getURI().getPath());
			} else
				return false;
		} catch (FileNotFoundException e) {
			// return empty document and save later
			return true;
		}

		try {
			setDocumentContent(document, reader);
			return true;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					"org.eclipse.ui.examples.rcp.texteditor", IStatus.OK, "error reading file", e)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Reads in document content from a reader and fills <code>document</code>
	 * 
	 * @param document the document to fill
	 * @param reader the source
	 * @throws IOException if reading fails
	 */
	private void setDocumentContent(IDocument document, Reader reader) throws IOException {
		Reader in = new BufferedReader(reader);
		try {

			StringBuffer buffer = new StringBuffer(512);
			char[] readBuffer = new char[512];
			int n = in.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n = in.read(readBuffer);
			}

			document.set(buffer.toString());

		} finally {
			in.close();
		}
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#isStateValidated(java.lang.Object)
	 */
	@Override
	public boolean isStateValidated(Object element) {
		return true;
	}

	@Override
	public boolean canSaveDocument(Object element) {
		if (isReadOnly(element)) {
			return false;
		}
		return super.canSaveDocument(element);
	}
}
