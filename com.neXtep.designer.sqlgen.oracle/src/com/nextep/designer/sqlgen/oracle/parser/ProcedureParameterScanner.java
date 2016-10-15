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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import com.nextep.datadesigner.dbgm.model.ParameterType;
import com.nextep.designer.sqlgen.text.SQLWordDetector;

/**
 * @author Christophe Fondacci
 *
 */
public class ProcedureParameterScanner extends RuleBasedScanner {

	public static final IToken DECLSTART_TOKEN = new Token("__sql_declstart");
	public static final IToken DECLEND_TOKEN = new Token("__sql_declend");
	public static final IToken NEWPARAM_TOKEN = new Token("__sql_newparam");
	public static final IToken PARAM_TOKEN = new Token("__sql_param");
	public static final IToken DEFAULT_TOKEN = new Token("__sql_default");
	public static final IToken RETURN_TOKEN = new Token("__sql_return");
	public static final IToken ENDPROC_TOKEN = new Token("__sql_endproc");
	
	public ProcedureParameterScanner() {
		List<IRule> rules = new ArrayList<IRule>();
		rules.add(new CharRule('(',DECLSTART_TOKEN));
		rules.add(new CharRule(')',DECLEND_TOKEN));
		rules.add(new CharRule(',',NEWPARAM_TOKEN));
		rules.add(new MultiLineRule("=",",",DEFAULT_TOKEN));
		rules.add(new MultiLineRule("=",")",DEFAULT_TOKEN));
		
		WordRule wr = new WordRule(new SQLWordDetector(),PARAM_TOKEN,true);
		for(ParameterType t : ParameterType.values()) {
			wr.addWord(t.name(), new Token(t));
		}
		wr.addWord("RETURN", RETURN_TOKEN);
		wr.addWord("IS", ENDPROC_TOKEN);
		wr.addWord("AS", ENDPROC_TOKEN);
		rules.add(wr);
		// Setting rules
		setRules(rules.toArray(new IRule[rules.size()]));
	}
}
