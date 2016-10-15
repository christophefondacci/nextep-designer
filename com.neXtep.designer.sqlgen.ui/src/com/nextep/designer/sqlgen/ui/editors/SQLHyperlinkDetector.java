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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.sqlgen.ui.model.ITypedObjectTextProvider;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;

public class SQLHyperlinkDetector implements IHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		
		if (region == null || textViewer == null)
			return null;

		IDocument document= textViewer.getDocument();
		
		return detectHyperlinks(document, region);
	}

	/**
	 * Detects hyperlinks of the specified document at the given region. 
	 * 
	 * @param document document to analyze
	 * @param region region to analyse
	 * @return an array of {@link IHyperlink}
	 */
	public static IHyperlink[] detectHyperlinks(IDocument document, IRegion region) {
		int offset= region.getOffset();

		if (document == null)
			return null;

		IRegion lineInfo;
		String line;
		try {
			lineInfo= document.getLineInformationOfOffset(offset);
			line= document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}

		//Retrieving proposals
		ITypedObjectTextProvider provider = SQLEditorUIServices.getInstance().getTypedObjectTextProvider();
		List<String> allProposals = provider.listProvidedElements();
		List<IHyperlink> links = new ArrayList<IHyperlink>();
		for(String s : allProposals) {
			if(line.toUpperCase().contains(s.toUpperCase())) {
				// More accurate search
				Pattern p = Pattern.compile("(\\W|\\s|^)"+s.toUpperCase()+"(\\W|\\s|$)");
				Matcher m = p.matcher(line.toUpperCase());
				while(m.find()) {
					if(offset>=(m.start()+lineInfo.getOffset()) && offset <= (m.end()+lineInfo.getOffset())) {
						// Eliminating encapsulators
						String matched = m.group();
						int matchedOffset = matched.indexOf(s.toUpperCase());
						
						
						IRegion r = new Region(lineInfo.getOffset()+m.start() + matchedOffset, s.length());
						ITypedObject obj = provider.getElement(s);
						links.add(new TypedObjectHyperlink(r,s,obj));
					}
				}
				
			}
		}
		if(links.size()==0) {
			return null;
		}
		return links.toArray(new IHyperlink[links.size()]);
	}
}
