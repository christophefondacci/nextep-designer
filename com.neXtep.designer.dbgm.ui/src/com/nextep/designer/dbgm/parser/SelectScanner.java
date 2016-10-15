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
///**
// * Copyright (c) 2008 neXtep Softwares.
// * All rights reserved. Terms of the neXtep licence
// * are available at http://www.nextep-softwares.com
// */
//package com.nextep.designer.dbgm.parser;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.eclipse.jface.text.rules.ICharacterScanner;
//import org.eclipse.jface.text.rules.IRule;
//import org.eclipse.jface.text.rules.IToken;
//import org.eclipse.jface.text.rules.IWordDetector;
//import org.eclipse.jface.text.rules.RuleBasedScanner;
//import org.eclipse.jface.text.rules.Token;
//import org.eclipse.jface.text.rules.WordRule;
//
///**
// * A very simple select scanner.
// * 
// * @author Christophe Fondacci
// *
// */
//public class SelectScanner extends RuleBasedScanner {
//
//	public static final Token TOKEN_SELECT 	= new Token("__sql_select");
//	public static final Token TOKEN_FROM 	= new Token("__sql_from");
//	public static final Token TOKEN_WHERE 	= new Token("__sql_where");
//	public static final Token TOKEN_AS 		= new Token("__sql_as");
//	public static final Token TOKEN_WORD	= new Token("__sql_word");
//	public static final Token TOKEN_DOT		= new Token("__sql_dot");
//	public static final Token TOKEN_COMMA	= new Token("__sql_comma");
//	public static final Token TOKEN_GROUP	= new Token("__sql_group");
//	public static final Token TOKEN_ORDER	= new Token("__sql_order");
//	
//	public SelectScanner() {
//		List<IRule> rules = new ArrayList<IRule>();
//		WordRule wr = new WordRule(new IWordDetector() {
//			@Override
//			public boolean isWordPart(char c) {
//				return Character.isJavaIdentifierPart(c) || c == '$';
//			}
//			@Override
//			public boolean isWordStart(char c) {
//				return Character.isJavaIdentifierStart(c);
//			}
//		},TOKEN_WORD,true);
//		wr.addWord("SELECT", TOKEN_SELECT);
//		wr.addWord("FROM", TOKEN_FROM);
//		wr.addWord("WHERE", TOKEN_WHERE);
//		wr.addWord("AS", TOKEN_AS);
//		wr.addWord("GROUP", TOKEN_GROUP);
//		wr.addWord("ORDER", TOKEN_ORDER);
//		rules.add(wr);
//		rules.add(new IRule() {
//			@Override
//			public IToken evaluate(ICharacterScanner scanner) {
//				int c = scanner.read();
//				if(c == '.') {
//					return TOKEN_DOT;
//				} else if(c == ',') {
//					return TOKEN_COMMA;
//				} else {
//					scanner.unread();
//				}
//				return Token.UNDEFINED;
//			}
//		});
//		setRules(rules.toArray(new IRule[rules.size()]));
//	}
//}
