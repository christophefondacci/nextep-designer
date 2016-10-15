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
import java.util.List;
import java.util.Map;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.designer.sqlgen.text.SQLWordDetector;

/**
 * @author Christophe Fondacci
 *
 */
public class FromScanner extends RuleBasedScanner {

	public static final IToken TABLE_TOKEN = new Token("__sql_table");
	public static final IToken ALIAS_TOKEN = new Token("__sql_alias");
	
	public FromScanner(Map<String,IBasicTable> tablesMap) {
		List<IRule> rules = new ArrayList<IRule>();
		WordRule wr = new WordRule(new SQLWordDetector(),ALIAS_TOKEN);
		for(String tableName : tablesMap.keySet()) {
			wr.addWord(tableName, new Token(tablesMap.get(tableName)));
		}
		rules.add(wr);
		setRules(rules.toArray(new IRule[rules.size()]));
	}
}
