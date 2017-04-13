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
package com.nextep.designer.sqlgen.ui.impl;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import com.nextep.datadesigner.dbgm.model.ISqlBased;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class SQLSearchLabelProvider extends BaseLabelProvider implements IStyledLabelProvider {

	// Maximum size of a search result label provided by this IStyledLabelProvider.
	private static final int FRAME_MAX_SIZE = 80;

	// Minimum number of characters around the matching text.
	private static final int FRAME_MIN_SIZE = 10;

	// Minimum number of characters around the matching text.
	private static final Styler MATCH_STYLER = new Styler() {

		@Override
		public void applyStyles(TextStyle textStyle) {
			textStyle.font = FontFactory.FONT_BOLD;
			textStyle.background = FontFactory.SHADOW_GRAPH3_COLOR;
		}
	};

	SQLSearchViewPage page;

	public SQLSearchLabelProvider(SQLSearchViewPage page) {
		this.page = page;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ITypedObject) {
			return ImageFactory.getImage(((ITypedObject) element).getType().getTinyIcon());
		}
		return null;
	}

	public String getText(Object element) {
		return getBasicText(element).getText();
	}

	private BasicString getBasicText(Object element) {
		BasicString bString = null;

		if (element instanceof Match) {
			final Match match = (Match) element;

			if (match.getElement() instanceof ISqlBased) {
				final ISqlBased sqlBased = (ISqlBased) match.getElement();
				final String sql = sqlBased.getSql();

				/*
				 * ISSUE GH-18: We fetch the entire line that contains the match, with a limit of
				 * FRAME_MAX_SIZE characters.
				 */

				/*
				 * We search for the newline character to find the beginning and the end of the line
				 * that contains the match. This way we will be able to find the line boundaries
				 * whatever the format of the input string (Windows CRLF or Linux/OSX LF).
				 */
				// +1 to exclude the newline character from the selection
				int lineStart = sql.lastIndexOf('\n', match.getOffset()) + 1;

				/*
				 * We start searching for a newline character at the beginning of the match instead
				 * of the end in case the match spans multiple lines (regular expression search).
				 */
				int lineEnd = sql.indexOf('\n', match.getOffset());

				/*
				 * If no newline character has been found after the end of the match, we set the end
				 * of the selection at the end of the input string.
				 */
				if (lineEnd < 0) {
					lineEnd = sql.length();
				}

				/*
				 * If the match spans multiple lines (regular expression search), the size of the
				 * selection is limited so as not to exceed the end of the current line.
				 */
				int maxSelectionLength = lineEnd - match.getOffset();

				/*
				 * Now that we have found the position of the line containing the match, we try to
				 * define a selection window of FRAME_MAX_SIZE characters, with a minimum of
				 * FRAME_MIN_SIZE characters before and after the match.
				 */
				int minSelectionIndex = match.getOffset()
						+ Math.min(match.getLength(), maxSelectionLength) + FRAME_MIN_SIZE
						- FRAME_MAX_SIZE;
				int selectionStart = Math.max(minSelectionIndex, lineStart);
				int selectionEnd = Math.min(selectionStart + FRAME_MAX_SIZE, lineEnd);

				/*
				 * If the last character of the selection window is a carriage return character or a
				 * newline character then we remove it from the selection.
				 */
				while (sql.charAt(selectionEnd - 1) == '\r' || sql.charAt(selectionEnd - 1) == '\n') {
					selectionEnd--;
				}

				bString = new BasicString(sql.substring(selectionStart, selectionEnd),
						selectionStart, selectionEnd - selectionStart);
			}
		} else if (element instanceof INamedObject) {
			String elementName = ((INamedObject) element).getName();
			bString = new BasicString(elementName, 0, elementName.length());
		} else {
			String unknownString = "Unknown"; //$NON-NLS-1$
			bString = new BasicString(unknownString, 0, unknownString.length());
		}

		return bString;
	}

	@Override
	public StyledString getStyledText(Object element) {
		StyledString sString = null;

		if (element instanceof Match) {
			final Match match = (Match) element;
			final BasicString bString = getBasicText(element);
			final String text = bString.getText();

			// We compute the start and end positions of the match inside the match text
			int matchStart = match.getOffset() - bString.getOffset();
			int matchEnd = matchStart
					+ Math.min(match.getLength(), bString.getLength() - matchStart);

			sString = new StyledString("... "); //$NON-NLS-1$
			sString.append(text.substring(0, matchStart));
			sString.append(text.substring(matchStart, matchEnd), MATCH_STYLER);
			sString.append(text.substring(matchEnd));
			sString.append(" ..."); //$NON-NLS-1$

			if (match.getElement() instanceof ISqlBased) {
				final String sql = ((ISqlBased) match.getElement()).getSql();
				final IDocument d = new Document(sql);

				try {
					/*
					 * ISSUE GH-17: Line numbers as returned by the IDocument#getLineOfOffset(int)
					 * method are numbered from 0. So we add 1 to match the line number displayed in
					 * the SQL code editor.
					 */
					int line = d.getLineOfOffset(match.getOffset()) + 1;
					sString.append(" (line " + line + ")", StyledString.COUNTER_STYLER); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (BadLocationException e) {
					// Nothing to do here, we forget line addition
				}
			}
		} else if (element instanceof INamedObject) {
			sString = new StyledString(getText(element));

			// Retrieving search result for match computation
			final SQLSearchResult result = (SQLSearchResult) page.getInput();
			final int matchCount = result.getMatches(element).length;

			if (matchCount > 0) {
				sString.append(" (" + matchCount + " match", StyledString.COUNTER_STYLER) //$NON-NLS-1$ //$NON-NLS-2$
						.append((matchCount > 1 ? "es)" : ")"), StyledString.COUNTER_STYLER); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			sString = new StyledString(getText(element));
		}

		return sString;
	}

	/*
	 * Convenience class used to hold the text of a textual match and its position in the element in
	 * which the match is reported against.
	 */
	private final class BasicString {

		private String text;
		private int offset;
		private int length;

		public BasicString(String text, int offset, int length) {
			this.text = text;
			this.offset = offset;
			this.length = length;
		}

		public String getText() {
			return text;
		}

		public int getOffset() {
			return offset;
		}

		public int getLength() {
			return length;
		}

	}

}
