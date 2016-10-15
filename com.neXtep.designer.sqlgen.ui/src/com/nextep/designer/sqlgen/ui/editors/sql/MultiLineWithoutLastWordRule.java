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
package com.nextep.designer.sqlgen.ui.editors.sql;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;

/**
 * A rule similar to the MultiLineRule except that it
 * unreads the last matching token.
 * 
 * @author Christophe Fondacci
 *
 */
public class MultiLineWithoutLastWordRule extends MultiLineRule {

	private IToken returnedToken;
	private String endWord;
	public MultiLineWithoutLastWordRule(String start, String end, IToken returnedToken) {
		super(start,end,returnedToken);
		this.returnedToken = returnedToken;
		this.endWord = end;
	}
	/**
	 * @see org.eclipse.jface.text.rules.PatternRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
	 */
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		IToken t =  super.evaluate(scanner);
		if(t == returnedToken) {
			// We unread the last read word
			for(int i = 0 ; i < endWord.length();i++) {
				scanner.unread();
			}
		}
		return t;
	}
}
