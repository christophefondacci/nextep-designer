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
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Christophe Fondacci
 *
 */
public class FastReferenceScanner extends RuleBasedScanner {

	public static final IToken REF_TOKEN = new Token("__sql_ref");
	private static final RuleBasedScanner instance = new FastReferenceScanner();
	public static final RuleBasedScanner getInstance() {
		return instance;
	}
	private FastReferenceScanner() {
		List<IRule> rules = new ArrayList<IRule>();
		
		rules.add(new SingleLineRule(" ",".",REF_TOKEN));
		rules.add(new SingleLineRule("\t",".",REF_TOKEN));
		rules.add(new SingleLineRule("\n",".",REF_TOKEN));
		
		setRules(rules.toArray(new IRule[rules.size()]));
	}
}
