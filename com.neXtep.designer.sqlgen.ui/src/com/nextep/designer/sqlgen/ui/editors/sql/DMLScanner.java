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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.text.SQLWordDetector;

/**
 * @author Christophe Fondacci
 */
public class DMLScanner extends RuleBasedScanner {

	public static final IToken SELECT_TOKEN = new Token("__sql_select");
	public static final IToken FROM_TOKEN = new Token("__sql_from");
	public static final IToken SET_TOKEN = new Token("__sql_set");
	public static final IToken UPDATE_TOKEN = new Token("__sql_update");
	public static final IToken INTO_TOKEN = new Token("__sql_into");
	public static final IToken INSERT_TOKEN = new Token("__sql_insert");
	public static final IToken WHERE_TOKEN = new Token("__sql_where");
	public static final IToken WORD_TOKEN = new Token("__sql_word");
	public static final IToken LEFTPAR_TOKEN = new Token("__sql_(");
	public static final IToken RIGHTPAR_TOKEN = new Token("__sql_)");
	// public static final IToken GROUP_TOKEN = new Token("__sql_group");
	// public static final IToken ORDER_TOKEN = new Token("__sql_order");
	// public static final IToken BY_TOKEN = new Token("__sql_by");
	public static final IToken DML_TOKEN = new Token("__sql_dml");
	public static final IToken COMMA_TOKEN = new Token("__sql_comma");

	public DMLScanner(ISQLParser parser) {
		List<IRule> rules = new ArrayList<IRule>();
		WordRule wr = new WordRule(new SQLWordDetector(), WORD_TOKEN, true);
		wr.addWord("SELECT", SELECT_TOKEN);
		wr.addWord("FROM", FROM_TOKEN);
		wr.addWord("INSERT", INSERT_TOKEN);
		wr.addWord("WHERE", WHERE_TOKEN);
		wr.addWord("UPDATE", UPDATE_TOKEN);
		wr.addWord("SET", SET_TOKEN);
		wr.addWord("INTO", INTO_TOKEN);
		// Building list of affected words
		List<String> affectedDml = Arrays.asList("SELECT", "FROM", "INSERT", "WHERE", "UPDATE",
				"SET", "INTO");
		List<String> dmlWords = parser.getTypedTokens().get(ISQLParser.DML);
		for (String dmlWord : dmlWords) {
			if (!affectedDml.contains(dmlWord)) {
				wr.addWord(dmlWord, DML_TOKEN);
			}
		}
		// wr.addWord("GROUP", GROUP_TOKEN);
		// wr.addWord("ORDER", ORDER_TOKEN);
		// wr.addWord("BY", BY_TOKEN);
		rules.add(wr);
		rules.add(new CharRule(',', COMMA_TOKEN));
		rules.add(new CharRule('(', LEFTPAR_TOKEN));
		rules.add(new CharRule(')', RIGHTPAR_TOKEN));
		// rules.add(new MultiLineWithoutLastWordRule("SELECT","FROM",SELECT_TOKEN));
		// rules.add(new MultiLineWithoutLastWordRule("FROM","WHERE",FROM_TOKEN));
		// rules.add(new MultiLineWithoutLastWordRule("WHERE",";",WHERE_TOKEN));
		//		
		setRules(rules.toArray(new IRule[rules.size()]));
	}

	/**
	 * A simple rule which returns a given token when the scanner matches a single specified
	 * character.
	 * 
	 * @author Christophe Fondacci
	 */
	private class CharRule implements IRule {

		private char car;
		private IToken returnedToken;

		/**
		 * @param car the character to match
		 * @param returnedToken the token to return when matching
		 */
		public CharRule(char car, IToken returnedToken) {
			this.car = car;
			this.returnedToken = returnedToken;
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int readCar = scanner.read();
			if (readCar == car) {
				return returnedToken;
			} else {
				scanner.unread();
			}
			return Token.UNDEFINED;
		}
	}
}
