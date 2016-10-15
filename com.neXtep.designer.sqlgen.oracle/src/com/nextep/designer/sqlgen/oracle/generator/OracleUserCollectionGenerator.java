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
package com.nextep.designer.sqlgen.oracle.generator;

import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.model.IUserCollection;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleUserCollectionGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return generateFullSQL(result.getSource());
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		IUserCollection type = (IUserCollection) model;
		final String typeName = getName(type);

		// Initializing script
		ISQLScript script = new SQLScript(ScriptType.TYPE);
		script.appendSQL(prompt("Prompt Dropping type '" + type.getName() + "'..."));
		script.appendSQL("DROP TYPE " + typeName);
		closeLastStatement(script);

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(type.getType(), type.getName()), script);
		r.addPrecondition(new DatabaseReference(IElementType.getInstance(IUserType.TYPE_ID), type
				.getDatatype().getName()));
		// Exiting
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		IUserCollection coll = (IUserCollection) model;
		final String collName = getName(coll);
		final String rawName = coll.getName();

		ISQLScript s = new SQLScript(ScriptType.TYPE);
		s.appendSQL(prompt("Creating collection type '" + rawName + "'..."));
		s.appendSQL("CREATE OR REPLACE TYPE " + collName + " AS ");
		switch (coll.getCollectionType()) {
		case NESTED_TABLE:
			s.appendSQL("TABLE OF ");
			break;
		case VARRAY:
			s.appendSQL("VARRAY(" + coll.getSize() + ") OF ");
			break;
		}
		s.appendSQL(DBGMHelper.getDatatypeLabel(coll.getDatatype()));
		closeLastStatement(s);

		IGenerationResult r = GenerationFactory.createGenerationResult(rawName);
		r.addAdditionScript(new DatabaseReference(coll.getType(), rawName), s);
		r.addPrecondition(new DatabaseReference(IElementType.getInstance(IUserType.TYPE_ID), coll
				.getDatatype().getName()));

		// Building dependencies
		GenerationHelper.addSqlGenerationPreconditions(r, coll.getName(), s,
				IElementType.getInstance(IUserType.TYPE_ID));

		// Returning result
		return r;
	}

}
