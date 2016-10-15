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
package com.nextep.designer.sqlgen.oracle.strategies;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.IToken;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.oracle.parser.PackageScanner;

/**
 * Auto indent line strategy sensitive to brackets.
 */
public class SQLAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

	private static final Log log = LogFactory.getLog(SQLAutoIndentStrategy.class);
	private ISQLParser parser;
	private String strDelimiter;
	public SQLAutoIndentStrategy(ISQLParser parser) {
		this.parser = parser;
		strDelimiter = new String( new char[] { parser.getStringDelimiter() } );
	}

	/* (non-Javadoc)
	 * Method declared on IAutoIndentStrategy
	 */
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text))
			smartIndentAfterNewLine(d, c);
		else if ("}".equals(c.text)) { //$NON-NLS-1$
			smartInsertAfterBracket(d, c);
		} else if( strDelimiter.equals(c.text) ) {
			c.text+=strDelimiter;
			c.shiftsCaret = false;
			c.caretOffset=c.offset+1;
		}
	}

	/**
	 * Returns whether or not the given text ends with one of the documents legal line delimiters.
	 *
	 * @param d the document
	 * @param txt the text
	 * @return <code>true</code> if <code>txt</code> ends with one of the document's line delimiters, <code>false</code> otherwise
	 */
	private boolean endsWithDelimiter(IDocument d, String txt) {
		String[] delimiters= d.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.endsWith(delimiters, txt) > -1;
		return false;
	}

	/**
	 * Returns the line number of the next bracket after end.
	 *
	 * @param document - the document being parsed
	 * @param line - the line to start searching back from
	 * @param end - the end position to search back from
	 * @param closingBracketIncrease - the number of brackets to skip
	 * @return the line number of the next matching bracket after end
	 * @throws BadLocationException in case the line numbers are invalid in the document
	 */
	 protected int findMatchingOpenBegin(IDocument document, int line, int end, int closingBracketIncrease) throws BadLocationException {

		int start= document.getLineOffset(line);
		int beginCount= getBeginCount(document, start, end, false) - closingBracketIncrease;

		// sum up the brackets counts of each line (closing brackets count negative,
		// opening positive) until we find a line the brings the count to zero
		while (beginCount < 0) {
			line--;
			if (line < 0) {
				return -1;
			}
			start= document.getLineOffset(line);
			end= start + document.getLineLength(line) - 1;
			beginCount += getBeginCount(document, start, end, false);
		}
		return line;
	}
		/**
		 * Retrieves the last word immediately before the specified
		 * offset in the document. Any space will be ignored.
		 * 
		 * @param document document to look into
		 * @param offset offset to start the search from.
		 * @return the last word.
		 */
		private String getLastWord(IDocument doc , int offset) {
			if (doc == null || offset > doc.getLength())
				return "";
			
			int length= 0;
			try {
				boolean wordStarted = false;
				while (--offset >= 0 && (!wordStarted || wordStarted && Character.isJavaIdentifierPart(doc.getChar(offset)))) {
					// We continue on spaces
					if(' ' == doc.getChar(offset)) {
						continue;
					} else if(!wordStarted) {
						wordStarted = true;
					}
					length++;
				}
				
				return doc.get(offset+1,length);
			} catch( BadLocationException e) {
				log.debug("Error while retrieving completion prefix: BadLocation.");
				return "";
			}
		}
	/**
	 * Returns the bracket value of a section of text. Closing brackets have a value of -1 and
	 * open brackets have a value of 1.
	 *
	 * @param document - the document being parsed
	 * @param start - the start position for the search
	 * @param end - the end position for the search
	 * @param ignoreCloseBrackets - whether or not to ignore closing brackets in the count
	 * @return the bracket value of a section of text
	 * @throws BadLocationException in case the positions are invalid in the document
	 */
	 private int getBeginCount(IDocument document, int start, int end, boolean ignoreCloseBrackets) throws BadLocationException {

		PackageScanner scanner = new PackageScanner();
		scanner.setRange(document, start, end-start);
		IToken token = scanner.nextToken();
		int beginCount = 0;
		while(!token.isEOF()) {
			if(token == PackageScanner.BEGIN_TOKEN) {
				beginCount++;
			} else if(token == PackageScanner.END_TOKEN) {
				beginCount--;
			}
			token = scanner.nextToken();
		}
		return beginCount;
	}

	/**
	 * Returns the end position of a comment starting at the given <code>position</code>.
	 *
	 * @param document - the document being parsed
	 * @param position - the start position for the search
	 * @param end - the end position for the search
	 * @return the end position of a comment starting at the given <code>position</code>
	 * @throws BadLocationException in case <code>position</code> and <code>end</code> are invalid in the document
	 */
	 private int getCommentEnd(IDocument document, int position, int end) throws BadLocationException {
		int currentPosition = position;
		while (currentPosition < end) {
			char curr= document.getChar(currentPosition);
			currentPosition++;
			if (curr == '*') {
				if (currentPosition < end && document.getChar(currentPosition) == '/') {
					return currentPosition + 1;
				}
			}
		}
		return end;
	}

	/**
	 * Returns the content of the given line without the leading whitespace.
	 *
	 * @param document - the document being parsed
	 * @param line - the line being searched
	 * @return the content of the given line without the leading whitespace
	 * @throws BadLocationException in case <code>line</code> is invalid in the document
	 */
	 protected String getIndentOfLine(IDocument document, int line) throws BadLocationException {
		if (line > -1) {
			int start= document.getLineOffset(line);
			int end= start + document.getLineLength(line) - 1;
			int whiteend= findEndOfWhiteSpace(document, start, end);
			return document.get(start, whiteend - start);
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the position of the <code>character</code> in the <code>document</code> after <code>position</code>.
	 *
	 * @param document - the document being parsed
	 * @param position - the position to start searching from
	 * @param end - the end of the document
	 * @param character - the character you are trying to match
	 * @return the next location of <code>character</code>
	 * @throws BadLocationException in case <code>position</code> is invalid in the document
	 */
	 private int getStringEnd(IDocument document, int position, int end, char character) throws BadLocationException {
		int currentPosition = position;
		while (currentPosition < end) {
			char currentCharacter= document.getChar(currentPosition);
			currentPosition++;
			if (currentCharacter == '\\') {
				// ignore escaped characters
				currentPosition++;
			} else if (currentCharacter == character) {
				return currentPosition;
			}
		}
		return end;
	}

	/**
	 * Set the indent of a new line based on the command provided in the supplied document.
	 * @param document - the document being parsed
	 * @param command - the command being performed
	 */
	 protected void smartIndentAfterNewLine(IDocument document, DocumentCommand command) {

		int docLength= document.getLength();
		if (command.offset == -1 || docLength == 0)
			return;


		try {
			int p= (command.offset == docLength ? command.offset - 1 : command.offset);
			int line= document.getLineOfOffset(p);
			
			StringBuffer buf= new StringBuffer();//command.text);
			final String lastWord = getLastWord(document, command.offset).toUpperCase();
			if("IS".equals(lastWord) || "BEGIN".equals(lastWord)) {
				buf.append(command.text);
				buf.append(getIndentOfLine(document, line));
				buf.append('\t');
			} else if (command.offset < docLength &&
					document.search(document.getLineInformationOfOffset(command.offset).getOffset(), "end", true, false, true)<command.offset) {
				int indLine= findMatchingOpenBegin(document, line, command.offset, 0);
				if (indLine == -1) {
					indLine= 0;
				}
				buf.append(command.text);
				String openingIndent = getIndentOfLine(document, indLine); 
				buf.append(openingIndent);
//				buf.append('\t');
				// Replacing indent of current line
				String currentIndent = getIndentOfLine(document, line);
				if(!currentIndent.equals(openingIndent)) {
					int initialOffset = command.offset;
					command.offset = document.getLineOffset(line);
					command.length += initialOffset - command.offset;
					int whiteEnd = findEndOfWhiteSpace(document, command.offset, initialOffset);
					command.text = openingIndent + document.get(whiteEnd, initialOffset-whiteEnd) + buf.toString();
					return;
					//command.addCommand(document.getLineOffset(line), currentIndent.length(), openingIndent, command.owner);
				}
			} else {
				buf.append(command.text);
				int start= document.getLineOffset(line);
				int whiteend= findEndOfWhiteSpace(document, start, command.offset);
				buf.append(document.get(start, whiteend - start));
				if (getBeginCount(document, start, command.offset, true) > 0) {
					buf.append('\t');
				}
			}
			command.text= buf.toString();

		} catch (BadLocationException excp) {
			log.error("AutoIndent.error.bad_location_1",excp); //$NON-NLS-1$
		}
	}

	/**
	 * Set the indent of a bracket based on the command provided in the supplied document.
	 * @param document - the document being parsed
	 * @param command - the command being performed
	 */
	 protected void smartInsertAfterBracket(IDocument document, DocumentCommand command) {
		if (command.offset == -1 || document.getLength() == 0)
			return;

		try {
			int p= (command.offset == document.getLength() ? command.offset - 1 : command.offset);
			int line= document.getLineOfOffset(p);
			int start= document.getLineOffset(line);
			int whiteend= findEndOfWhiteSpace(document, start, command.offset);

			// shift only when line does not contain any text up to the closing bracket
			if (whiteend == command.offset) {
				// evaluate the line with the opening bracket that matches out closing bracket
				int indLine= findMatchingOpenBegin(document, line, command.offset, 1);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					StringBuffer replaceText= new StringBuffer(getIndentOfLine(document, indLine));
					// add the rest of the current line including the just added close bracket
					replaceText.append(document.get(whiteend, command.offset - whiteend));
					replaceText.append(command.text);
					// modify document command
					command.length= command.offset - start;
					command.offset= start;
					command.text= replaceText.toString();
				}
			}
		} catch (BadLocationException excp) {
			log.error("AutoIndent.error.bad_location_2"); //$NON-NLS-1$
		}
	}
}
