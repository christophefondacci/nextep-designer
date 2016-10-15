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
package com.nextep.designer.sqlgen.mssql.generator;

import java.util.Collection;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IDatatype;
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
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Bruno Gautier
 */
public class MSSQLUserTypeGenerator extends SQLGenerator {

	private static final List<String> BASE_TYPES = DBGMHelper.getDatatypeProvider(DBVendor.MSSQL)
			.listSupportedDatatypes();

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IUserType userType = (IUserType) model;
		final String userTypeName = userType.getName();
		final ITypeColumn typeColumn = userType.getColumns().get(0);
		final IDatatype typeColumnDatatype = typeColumn.getDatatype();
		final String typeColumnDatatypeName = typeColumnDatatype.getName();

		ISQLScript createScript = getSqlScript(userTypeName, userType.getDescription(),
				ScriptType.TYPE);
		createScript.appendSQL(prompt("Creating user-defined data type '" + userTypeName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		createScript.appendSQL("CREATE TYPE ").appendSQL(escape(userTypeName)).appendSQL(" FROM "); //$NON-NLS-1$ //$NON-NLS-2$

		IGenerationResult genResult = GenerationFactory.createGenerationResult(userTypeName);
		createScript.appendSQL(DBGMHelper.getDatatypeLabel(typeColumnDatatype));

		// Checking preconditions. If the aliased type is not a base type, we add pre-conditions for
		// both user type and user collection.
		if (!BASE_TYPES.contains(typeColumnDatatypeName.toUpperCase())) {
			genResult.addPrecondition(new DatabaseReference(IElementType
					.getInstance(IUserType.TYPE_ID), typeColumnDatatypeName));
			genResult.addPrecondition(new DatabaseReference(IElementType
					.getInstance(IUserCollection.TYPE_ID), typeColumnDatatypeName));
		}
		closeLastStatement(createScript);

		genResult.addAdditionScript(new DatabaseReference(userType.getType(), userTypeName),
				createScript);

		return genResult;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final ISQLGenerator collectionGenerator = getGenerator(IElementType
				.getInstance(IUserCollection.TYPE_ID));

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		Collection<IReferencer> dependentElts = CorePlugin.getService(IReferenceManager.class)
				.getReverseDependencies(result.getSource());
		for (IReferencer ref : dependentElts) {
			if (ref instanceof IUserCollection) {
				genResult.integrate(collectionGenerator.doDrop(ref));
				genResult.integrate(collectionGenerator.generateFullSQL(ref));
			}
		}
		genResult.integrate(generateFullSQL(result.getSource()));
		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IUserType userType = (IUserType) model;
		final String userTypeName = userType.getName();

		ISQLScript script = getSqlScript(userTypeName, userType.getDescription(), ScriptType.TYPE);
		script.appendSQL(prompt("Dropping type '" + userTypeName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		script.appendSQL("DROP TYPE ").appendSQL(escape(userTypeName)); //$NON-NLS-1$
		closeLastStatement(script);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(userType.getType(), userTypeName), script);

		return genResult;
	}

}
