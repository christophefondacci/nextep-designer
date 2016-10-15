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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
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

public class SQLSearchLabelProvider extends BaseLabelProvider implements IStyledLabelProvider {

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

	// @Override
	public String getText(Object element) {
		if (element instanceof INamedObject) {
			return ((INamedObject) element).getName();
		} else if (element instanceof Match) {
			Match match = (Match) element;
			if (match.getElement() instanceof ISqlBased) {
				ISqlBased sqlBased = (ISqlBased) match.getElement();
				final String sql = sqlBased.getSql();
				return "..."
						+ sql.substring(Math.max(0, match.getOffset() - 10),
								Math.min(match.getOffset() + match.getLength() + 10, sql.length()))
								.replaceAll("(\n|\r)", "") + "...";
			}
		}
		return "Unknown";
	}

	@Override
	public StyledString getStyledText(Object element) {
		// Retrieving text to display
		final String txt = getText(element);
		// Retrieving search result for match computation
		final SQLSearchResult result = (SQLSearchResult) page.getInput();
		// Specific match case, simply highlight the matched elements
		if (element instanceof Match) {
			return highlightMatches((Match) element, txt,
					((SQLSearchQuery) result.getQuery()).getSearchedText());
		}
		// Else we append a match suffix with match count
		StyledString s = new StyledString(txt);
		Match[] matches = result.getMatches(element);
		if (matches != null && matches.length > 0) {
			s.append(" (" + matches.length + " matches)", StyledString.COUNTER_STYLER);
		}
		return s;
	}

	private StyledString highlightMatches(Match match, String text, String searchedText) {
		Pattern p = Pattern.compile(FindReplaceDocumentAdapter.escapeForRegExPattern(searchedText
				.toUpperCase()));
		Matcher m = p.matcher(text.toUpperCase());
		int offset = 0;
		StyledString styled = new StyledString();
		while (m.find()) {
			styled.append(text.substring(offset, m.start()));
			styled.append(searchedText, new Styler() {

				@Override
				public void applyStyles(TextStyle textStyle) {
					textStyle.font = FontFactory.FONT_BOLD;
					textStyle.background = FontFactory.SHADOW_GRAPH3_COLOR;
				}
			});
			offset = m.end();
		}
		if (text.length() > offset) {
			styled.append(text.substring(offset, text.length()));
		}
		if (match.getElement() instanceof ISqlBased) {
			final String s = ((ISqlBased) match.getElement()).getSql();
			final IDocument d = new Document();
			d.set(s);
			try {
				int line = d.getLineOfOffset(match.getOffset());
				styled.append(" (line " + line + ")", StyledString.COUNTER_STYLER);
			} catch (BadLocationException e) {
				// Nothing to do here, we forget line addition
			}
		}

		return styled;
	}

}
