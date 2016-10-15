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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.ui.Activator;
import com.nextep.designer.sqlgen.ui.preferences.PreferenceConstants;

/**
 * @author Christophe Fondacci
 *
 */
public class StringAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

	private String fPartitioning;
	private String strDelimiter;
	private ISQLParser parser;

	/**
	 * The input string doesn't contain any line delimiter.
	 *
	 * @param inputString the given input string
	 * @return the displayable string.
	 */
	private String displayString(String inputString, String indentation, String delimiter) {

		int length = inputString.length();
		StringBuffer buffer = new StringBuffer(length);
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()){

			String token = tokenizer.nextToken();
			if (token.equals("\r")) { //$NON-NLS-1$
				buffer.append("\\r"); //$NON-NLS-1$
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					if (token.equals("\n")) { //$NON-NLS-1$
						buffer.append("\\n"); //$NON-NLS-1$
						buffer.append(strDelimiter + " " + parser.getStringConcatenator()+ delimiter); //$NON-NLS-1$
						buffer.append(indentation);
						buffer.append(strDelimiter); //$NON-NLS-1$
						continue;
					} else {
						buffer.append(strDelimiter + " " + parser.getStringConcatenator() + " " + delimiter); //$NON-NLS-1$
						buffer.append(indentation);
						buffer.append(strDelimiter); //$NON-NLS-1$
					}
				} else {
					continue;
				}
			} else if (token.equals("\n")) { //$NON-NLS-1$
				buffer.append("\\n"); //$NON-NLS-1$
				buffer.append(strDelimiter + " " + parser.getStringConcatenator() + " " + delimiter); //$NON-NLS-1$
				buffer.append(indentation);
				buffer.append(strDelimiter); //$NON-NLS-1$
				continue;
			}

			StringBuffer tokenBuffer = new StringBuffer();
			for (int i = 0; i < token.length(); i++){
				char c = token.charAt(i);
				switch (c) {
					case '\r' :
						tokenBuffer.append("\\r"); //$NON-NLS-1$
						break;
					case '\n' :
						tokenBuffer.append("\\n"); //$NON-NLS-1$
						break;
					case '\b' :
						tokenBuffer.append("\\b"); //$NON-NLS-1$
						break;
					case '\t' :
						// keep tabs verbatim
						tokenBuffer.append("\t"); //$NON-NLS-1$
						break;
					case '\f' :
						tokenBuffer.append("\\f"); //$NON-NLS-1$
						break;
					case '\"' :
						tokenBuffer.append("\\\""); //$NON-NLS-1$
						break;
					case '\'' :
						tokenBuffer.append("\\'"); //$NON-NLS-1$
						break;
					case '\\' :
						tokenBuffer.append("\\\\"); //$NON-NLS-1$
						break;
					default :
						tokenBuffer.append(c);
				}
			}
			buffer.append(tokenBuffer);
		}
		return buffer.toString();
	}

	/**
	 * Creates a new Java string auto indent strategy for the given document partitioning.
	 *
	 * @param partitioning the document partitioning
	 */
	public StringAutoIndentStrategy(String partitioning) {
		super();
		fPartitioning= partitioning;
		parser = GeneratorFactory.getSQLParser(DBGMHelper.getCurrentVendor());
		strDelimiter = new String(new char[] { parser.getStringDelimiter() });
	}

	private boolean isLineDelimiter(IDocument document, String text) {
		String[] delimiters= document.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.equals(delimiters, text) > -1;
		return false;
	}

	private String getLineIndentation(IDocument document, int offset) throws BadLocationException {

		// find start of line
		int adjustedOffset= (offset == document.getLength() ? offset  - 1 : offset);
		IRegion line= document.getLineInformationOfOffset(adjustedOffset);
		int start= line.getOffset();

		// find white spaces
		int end= findEndOfWhiteSpace(document, start, offset);

		return document.get(start, end - start);
	}

	private String getModifiedText(String string, String indentation, String delimiter) {
		return displayString(string, indentation, delimiter);
	}

	private void javaStringIndentAfterNewLine(IDocument document, DocumentCommand command) throws BadLocationException {

		ITypedRegion partition= TextUtilities.getPartition(document, fPartitioning, command.offset, true);
		int offset= partition.getOffset();
		int length= partition.getLength();

		if (command.offset == offset + length && document.getChar(offset + length - 1) == parser.getStringDelimiter())
			return;

		String indentation= getLineIndentation(document, command.offset);
		String delimiter= TextUtilities.getDefaultLineDelimiter(document);

		IRegion line= document.getLineInformationOfOffset(offset);
		String string= document.get(line.getOffset(), offset - line.getOffset());
		if (string.trim().length() != 0)
			indentation += String.valueOf("\t\t"); //$NON-NLS-1$

		IPreferenceStore preferenceStore= Activator.getDefault().getPreferenceStore();
		if (isLineDelimiter(document, command.text))
			command.text= strDelimiter + " " +parser.getStringConcatenator() + command.text + indentation + strDelimiter; //$NON-NLS-1$//$NON-NLS-2$
		else if (command.text.length() > 1 && preferenceStore.getBoolean(PreferenceConstants.EDITOR_ESCAPE_STRINGS))
			command.text= getModifiedText(command.text, indentation, delimiter);
	}

	private boolean isSmartMode() {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null)  {
			IEditorPart part= page.getActiveEditor();
			if(part instanceof SQLMultiEditor) {
				// Unwrapping current editor if we have a multi page editor
				part = ((SQLMultiEditor)part).getCurrentEditor();
			}
			if (part instanceof ITextEditorExtension3) {
				ITextEditorExtension3 extension= (ITextEditorExtension3) part;
				return extension.getInsertMode() == ITextEditorExtension3.SMART_INSERT;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IAutoIndentStrategy#customizeDocumentCommand(IDocument, DocumentCommand)
	 */
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		try {
			if (command.text == null)
				return;

			IPreferenceStore preferenceStore= Activator.getDefault().getPreferenceStore();

			if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_WRAP_STRINGS) && isSmartMode()) {
				javaStringIndentAfterNewLine(document, command);
			}

		} catch (BadLocationException e) {
		}
	}

}
