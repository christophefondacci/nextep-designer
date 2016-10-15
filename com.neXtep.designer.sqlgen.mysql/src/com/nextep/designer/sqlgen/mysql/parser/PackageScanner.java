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
package com.nextep.designer.sqlgen.mysql.parser;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import com.nextep.datadesigner.dbgm.model.ParameterType;
import com.nextep.designer.sqlgen.text.SQLWhiteSpaceDetector;

/**
 * @author Christophe Fondacci
 *
 */
public class PackageScanner extends RuleBasedScanner {

	public static final IToken PROC_TOKEN= new Token("__sql_procedure");
	public static final IToken PROC_ENDSPEC_TOKEN= new Token("__sql_proc_endspec");
//	public static final IToken PROC_SPEC_TOKEN= new Token("__sql_procspec");
	public static final IToken FUNC_TOKEN= new Token("__sql_function");
	public static final IToken BEGIN_TOKEN= new Token("__sql_begin");
	public static final IToken END_TOKEN= new Token("__sql_end");
	public static final IToken WORD_TOKEN= new Token("__sql_word");
	public static final IToken SEMICOLON_TOKEN = new Token("__sql_semicolon");
	public static final IToken PACKAGE_TOKEN = new Token("__sql_pkg");
	public static final IToken COMMENT_TOKEN = new Token("__sql_comment");
	public static final IToken STRING_TOKEN = new Token("__sql_string");
	
	public static final IToken PROC_MEMBER_TOKEN = new Token("__sql_proc_member");
	// Parameter-related tokens
	public static final IToken DECLSTART_TOKEN = new Token("__sql_declstart");
	public static final IToken DECLEND_TOKEN = new Token("__sql_declend");
	public static final IToken NEWPARAM_TOKEN = new Token("__sql_newparam");
	public static final IToken ASSIGN_TOKEN = new Token("__sql_assign");
	public static final IToken RETURN_TOKEN = new Token("__sql_return");
	
	public PackageScanner() {
		List<IRule> rules = new ArrayList<IRule>();
		//rules.add(new WhitespaceRule(new SQLWhiteSpaceDetector()));
		rules.add(new EndOfLineRule("--",COMMENT_TOKEN));
		rules.add(new EndOfLineRule("REM",COMMENT_TOKEN));
		rules.add(new MultiLineRule("/*","*/",COMMENT_TOKEN));
		rules.add(new MultiLineRule("'","'",STRING_TOKEN,'\\'));
		
		
		rules.add(new CharRule('(',DECLSTART_TOKEN));
		rules.add(new CharRule(')',DECLEND_TOKEN));
		rules.add(new CharRule(',',NEWPARAM_TOKEN));
		rules.add(new CharRule('=',ASSIGN_TOKEN));
//		rules.add(new MultiLineRule("PROCEDURE","IS",PROC_TOKEN));
//		rules.add(new MultiLineRule("PROCEDURE","AS",PROC_TOKEN));
//		rules.add(new MultiLineRule("FUNCTION","IS",PROC_TOKEN));
//		rules.add(new MultiLineRule("FUNCTION","AS",PROC_TOKEN));
//		rules.add(new MultiLineRule("CREATE","IS",PACKAGE_TOKEN));
		rules.add(new CharRule(';',SEMICOLON_TOKEN));
		WordRule wr = new WordRule(new PLSQLWordDetector(),WORD_TOKEN);
		wr.addWord("BEGIN", BEGIN_TOKEN);
		wr.addWord("END", END_TOKEN);
		wr.addWord("PROCEDURE", PROC_TOKEN);
		wr.addWord("FUNCTION", PROC_TOKEN);
		wr.addWord("IS", PROC_ENDSPEC_TOKEN);
		wr.addWord("AS", PROC_ENDSPEC_TOKEN);
		wr.addWord("PACKAGE", PACKAGE_TOKEN);
		wr.addWord("BODY", PACKAGE_TOKEN);
		wr.addWord("MEMBER", PROC_MEMBER_TOKEN);
		wr.addWord("MAP", PROC_MEMBER_TOKEN);
		wr.addWord("ORDER", PROC_MEMBER_TOKEN);
		for(ParameterType t : ParameterType.values()) {
			wr.addWord(t.name(), new Token(t));
		}
		wr.addWord("RETURN", RETURN_TOKEN);
		rules.add(wr);
		rules.add(new WhitespaceRule(new SQLWhiteSpaceDetector()));
		setRules(rules.toArray(new IRule[rules.size()]));

	}
}
