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
/**
 *
 */
package com.nextep.datadesigner.sqlgen.impl;

import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.impl.GenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class SQLScriptGenerator extends SQLGenerator implements ISQLGenerator {

	/**
	 * Simply return the script
	 * 
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateFullSQL(java.lang.Object)
	 */
	public IGenerationResult generateFullSQL(Object model) {
		final ISQLScript script = (ISQLScript) model;
		// Processing new line conversion
		String sql = script.getSql();
		// First pass, converting everything to \n
		sql = sql.replace("\r\n", "\n");
		sql = sql.replace("\r", "\n");
		// Now converting everything to preference definition
		sql = sql.replace("\n", NEWLINE);
		ISQLScript s = new SQLScript(script.getName(), script.getDescription(), sql,
				ScriptType.CUSTOM);
		IGenerationResult r = new GenerationResult(s.getName());
		r.addAdditionScript(new DatabaseReference(s.getType(), s.getName()), s);
		return r;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#doDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult doDrop(Object model) {
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.impl.SQLGenerator#generateDiff(com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return null;
	}

}
