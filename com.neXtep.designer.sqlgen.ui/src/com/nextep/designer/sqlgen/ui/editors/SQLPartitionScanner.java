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
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.ISQLParser;

/**
 * @author Christophe Fondacci
 *
 */
public class SQLPartitionScanner extends RuleBasedPartitionScanner {

    public static final String COMMENT = "__sql_comment";
    public static final String SINGLECOMMENT = "__sql_singlecomment";
    public static final String PROMPT = "__sql_prompt";
    public static final String STRING = "__sql_string";

    public SQLPartitionScanner(DBVendor vendor) {
        IToken comment 		= new Token(COMMENT);
        IToken str 			= new Token(STRING);
        IToken singleComment= new Token(SINGLECOMMENT);
        IToken prompt 		= new Token(PROMPT);

        List<IPredicateRule> pRules = new ArrayList<IPredicateRule>();

        pRules.add(new EndOfLineRule("--",singleComment));
        pRules.add(new EndOfLineRule("REM ",singleComment));
        pRules.add(new EndOfLineRule("Rem ",singleComment));
        pRules.add(new EndOfLineRule("rem ",singleComment));
        pRules.add(new EndOfLineRule("REM\r",singleComment));
        pRules.add(new EndOfLineRule("Rem\r",singleComment));
        pRules.add(new EndOfLineRule("rem\r",singleComment));        
        pRules.add(new EndOfLineRule("PROMPT ",prompt));
        pRules.add(new EndOfLineRule("Prompt ", prompt));
        pRules.add(new EndOfLineRule("prompt ", prompt));
        final ISQLParser p = GeneratorFactory.getSQLParser(vendor);
        pRules.add(new EndOfLineRule(p.getPromptCommand(),prompt));
        pRules.add(new MultiLineRule("/*","*/",comment));
        pRules.add(new EmptyCommentRule(comment));

        // Adding vendor specific string delimiters
        ISQLParser parser = GeneratorFactory.getSQLParser(vendor);
        String startChar = new String(new char[] {parser.getStringDelimiter()});
        pRules.add(new MultiLineRule(startChar,startChar,str,'\\'));
        pRules.add(new MultiLineRule("\"","\"",str));

        setPredicateRules(pRules.toArray(new IPredicateRule[pRules.size()]));

    }
    
	/**
	 * Detector for empty comments.
	 */
	static class EmptyCommentDetector implements IWordDetector {

		/*
		 * @see IWordDetector#isWordStart
		 */
		public boolean isWordStart(char c) {
			return (c == '/');
		}

		/*
		 * @see IWordDetector#isWordPart
		 */
		public boolean isWordPart(char c) {
			return (c == '*' || c == '/' );
		}
	}


	/**
	 * Word rule for empty comments.
	 */
	static class EmptyCommentRule extends WordRule implements IPredicateRule {

		private IToken fSuccessToken;
		/**
		 * Constructor for EmptyCommentRule.
		 * @param successToken
		 */
		public EmptyCommentRule(IToken successToken) {
			super(new EmptyCommentDetector(),successToken);
			fSuccessToken= successToken;
			addWord("/**/", fSuccessToken); //$NON-NLS-1$
		}

		/*
		 * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
		 */
		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			return evaluate(scanner);
		}

		/*
		 * @see IPredicateRule#getSuccessToken()
		 */
		public IToken getSuccessToken() {
			return fSuccessToken;
		}
	}
}
