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

import java.util.Collection;
import java.util.List;

import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.dbgm.model.IUserCollection;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleUserTypeGenerator extends SQLGenerator {

	private static final List<String> DATATYPES = DBGMHelper.getDatatypeProvider(DBVendor.ORACLE)
			.listSupportedDatatypes();

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		Collection<IReferencer> dependentElts = CorePlugin.getService(IReferenceManager.class)
				.getReverseDependencies(result.getSource());
		IGenerationResult r = GenerationFactory.createGenerationResult();
		ISQLGenerator collectionGenerator = getGenerator(IElementType
				.getInstance(IUserCollection.TYPE_ID));
		for (IReferencer ref : dependentElts) {
			if (ref instanceof IUserCollection) {
				r.integrate(collectionGenerator.doDrop(ref));
				r.integrate(collectionGenerator.generateFullSQL(ref));
			}
		}
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IUserType type = (IUserType) model;
		final String typeName = getName(type);
		final String rawName = type.getName();

		// Initializing script
		ISQLScript script = getSqlScript(typeName, type.getDescription(), ScriptType.TYPE);
		script.appendSQL(prompt("Dropping type '" + rawName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		script.appendSQL("DROP TYPE ").appendSQL(typeName); //$NON-NLS-1$
		closeLastStatement(script);

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(type.getType(), rawName), script);
		// Exiting
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IOracleUserType type = (IOracleUserType) model;
		final String typeName = getName(type);

		// Initializing script
		ISQLScript script = getSqlScript(typeName, type.getDescription(), ScriptType.TYPE);
		script.appendSQL(prompt("Creating type '" + typeName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		script.appendSQL("CREATE OR REPLACE TYPE ").appendSQL(typeName) //$NON-NLS-1$
				.appendSQL(" AS OBJECT (").appendSQL(NEWLINE); //$NON-NLS-1$
		boolean first = true;
		IGenerationResult genResult = GenerationFactory.createGenerationResult(typeName);

		// Generating inner columns
		for (ITypeColumn c : type.getColumns()) {
			if (first) {
				script.appendSQL("     "); //$NON-NLS-1$
				first = false;
			} else {
				script.appendSQL("    ,"); //$NON-NLS-1$
			}
			script.appendSQL(getColumnText(c)).appendSQL(NEWLINE);
			// Checking preconditions
			if (c.getDatatype() != null
					&& !DATATYPES.contains(c.getDatatype().getName().toUpperCase())) {
				genResult.addPrecondition(new DatabaseReference(IElementType
						.getInstance(IUserType.TYPE_ID), c.getDatatype().getName()));
				genResult.addPrecondition(new DatabaseReference(IElementType
						.getInstance(IUserCollection.TYPE_ID), c.getDatatype().getName()));
			}
		}
		// Forcing parse
		DBGMHelper.parse(type);
		for (IProcedure p : type.getProcedures()) {
			script.appendSQL("    ,").appendSQL(p.getHeader().trim()).appendSQL(NEWLINE); //$NON-NLS-1$
		}
		script.appendSQL(")"); //$NON-NLS-1$
		closeLastStatement(script);

		// Building generation result
		genResult.addAdditionScript(new DatabaseReference(type.getType(), typeName), script);

		// Creating type body
		if (type.getTypeBody() != null && !"".equals(type.getTypeBody().trim())) { //$NON-NLS-1$
			ISQLScript body = getSqlScript(typeName, type.getDescription(), ScriptType.TYPE_BODY);
			body.appendSQL(prompt("Creating type body '" + typeName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$

			body.appendSQL("CREATE OR REPLACE ").appendSQL(type.getTypeBody().trim()); //$NON-NLS-1$
			closeLastStatement(body);

			body.appendSQL("SHOW ERRORS").appendSQL(NEWLINE); //$NON-NLS-1$
			genResult.addAdditionScript(new DatabaseReference(type.getType(), "BODY " + typeName), //$NON-NLS-1$
					body);
		}

		return genResult;
	}

	private String getColumnText(ITypeColumn c) {
		String sqlText = escape(c.getName()) + " "; //$NON-NLS-1$
		if (c.getDatatype() != null) {
			sqlText += DBGMHelper.getDatatypeLabel(c.getDatatype());
		}
		return sqlText;
	}

}
