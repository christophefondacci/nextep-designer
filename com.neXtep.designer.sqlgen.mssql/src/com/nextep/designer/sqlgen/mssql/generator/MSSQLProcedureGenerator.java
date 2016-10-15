/**
 * Copyright (c) 2011 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.designer.sqlgen.mssql.generator;

import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Bruno Gautier
 */
public class MSSQLProcedureGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IProcedure procedure = (IProcedure) model;
		final String procName = procedure.getName();
		final String sqlSource = procedure.getSQLSource();
		final IDatatype procReturnType = procedure.getReturnType();
		final boolean isFunction = (procReturnType != null && procReturnType.getName() != null && !procReturnType
				.getName().trim().isEmpty())
				|| sqlSource.contains("FUNCTION"); //$NON-NLS-1$

		if (sqlSource == null || "".equals(sqlSource.trim())) { //$NON-NLS-1$
			return null;
		}

		ISQLScript createScript = getSqlScript(procName, procedure.getDescription(),
				ScriptType.PROC);
		createScript.appendSQL(prompt("Creating " + (isFunction ? "function" : "procedure") + " '" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ procName + "'...")); //$NON-NLS-1$
		createScript.appendSQL(GenerationHelper.removeLastStatementDelimiter(sqlSource, getParser()
				.getStatementDelimiter()));
		closeLastStatement(createScript);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addAdditionScript(new DatabaseReference(procedure.getType(), procName),
				createScript);

		// Adding dependencies
		GenerationHelper.addSqlGenerationPreconditions(genResult, procedure.getName(), procedure,
				IElementType.getInstance(IProcedure.TYPE_ID));

		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IProcedure procedure = (IProcedure) model;
		final String procName = procedure.getName();
		final IDatatype procReturnType = procedure.getReturnType();
		final boolean isFunction = (procReturnType != null && procReturnType.getName() != null && !procReturnType
				.getName().trim().isEmpty())
				|| procedure.getSQLSource().contains("FUNCTION"); //$NON-NLS-1$

		ISQLScript dropScript = getSqlScript(procName, procedure.getDescription(), ScriptType.PROC);
		dropScript.appendSQL(prompt("Dropping procedure '" + procName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("DROP ").appendSQL((isFunction ? "FUNCTION " : "PROCEDURE ")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.appendSQL(escape(procName));
		closeLastStatement(dropScript);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(procedure.getType(), procName), dropScript);

		return genResult;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		return generateFullSQL(result.getSource());
	}

}
