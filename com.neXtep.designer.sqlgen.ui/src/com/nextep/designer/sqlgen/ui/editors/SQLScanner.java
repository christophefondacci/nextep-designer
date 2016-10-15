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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.text.SQLWhiteSpaceDetector;
import com.nextep.designer.sqlgen.text.SQLWordDetector;

/**
 * @author Christophe Fondacci
 * 
 */
public class SQLScanner extends RuleBasedScanner {

	public SQLScanner(DBVendor vendor) {
		IToken def = new Token(SQLAttributeManager.getAttribute(SQLAttributeManager.DEFAULT));
		// Initializing the tokens map by word type
		Map<String, IToken> tokensMap = new HashMap<String, IToken>();
		tokensMap.put(ISQLParser.DDL, new Token(SQLAttributeManager.getAttribute(ISQLParser.DDL)));
		tokensMap.put(ISQLParser.LANG, new Token(SQLAttributeManager.getAttribute(ISQLParser.DDL)));
		tokensMap.put(ISQLParser.DML, new Token(SQLAttributeManager.getAttribute(ISQLParser.DML)));
		tokensMap
				.put(ISQLParser.FUNC, new Token(SQLAttributeManager.getAttribute(ISQLParser.FUNC)));
		tokensMap.put(ISQLParser.VAR, new Token(SQLAttributeManager.getAttribute(ISQLParser.VAR)));
		tokensMap.put(ISQLParser.DATATYPE,
				new Token(SQLAttributeManager.getAttribute(ISQLParser.DATATYPE)));
		tokensMap.put(ISQLParser.SPECIAL,
				new Token(SQLAttributeManager.getAttribute(ISQLParser.SPECIAL)));

		List<IRule> rules = new ArrayList<IRule>();
		// Retrieving correct parser
		ISQLParser parser = GeneratorFactory.getSQLParser(DBGMHelper.getCurrentVendor());
		// Initializing word rule
		WordRule wr = new WordRule(new SQLWordDetector(), def, true);
		// Adding words by type
		for (String type : parser.getTypedTokens().keySet()) {
			List<String> wordList = parser.getTypedTokens().get(type);
			// Processing the word list
			for (String word : wordList) {
				final IToken token = tokensMap.get(type);
				wr.addWord(word, token);
			}
		}
		rules.add(wr);
		// Adding a whitespace generic detector
		rules.add(new WhitespaceRule(new SQLWhiteSpaceDetector()));
		// Adding var handling
		rules.add(new WordRule(new VarWordDetector(), tokensMap.get(ISQLParser.VAR), true)); // new
																								// SingleLineRule(parser.getVarSeparator()," ",tokensMap.get(ISQLParser.VAR)));
		// Adding comment
		rules.add(new MultiLineRule("'", "'", new Token(SQLPartitionScanner.COMMENT)));
		setRules(rules.toArray(new IRule[rules.size()]));
	}

	private class VarWordDetector implements IWordDetector {
		ISQLParser parser;

		public VarWordDetector() {
			this.parser = GeneratorFactory.getSQLParser(DBGMHelper.getCurrentVendor());
		}

		/**
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
		 */
		@Override
		public boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
		}

		/**
		 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
		 */
		@Override
		public boolean isWordStart(char c) {
			return parser.getVarSeparator().charAt(0) == c;
		}

	}
}
