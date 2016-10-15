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
package com.nextep.designer.sqlgen.oracle.parser;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import com.nextep.datadesigner.dbgm.model.ParameterType;
import com.nextep.designer.sqlgen.text.SQLWordDetector;

/**
 * @author Christophe Fondacci
 *
 */
public class ParameterScanner extends RuleBasedScanner {

	public static final IToken DECL_TOKEN = new Token("__sql_decl");
	public static final IToken DEFAULTEXPR_TOKEN = new Token("__sql_default");
	public ParameterScanner() {

		IRule[] rules = new IRule[2];
		WordRule wr = new WordRule(new SQLWordDetector(),DECL_TOKEN);
		for(ParameterType t : ParameterType.values()) {
			wr.addWord(t.name(), new Token(t));
		}
		rules[0] = wr;
		rules[1] = new EndOfLineRule(":=",DEFAULTEXPR_TOKEN);
		setRules(rules);

	}
}
