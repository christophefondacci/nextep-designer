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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.mergers.TableMerger;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.merge.OracleTableMerger;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.RefreshTime;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleMaterializedViewGenerator extends SQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(OracleMaterializedViewGenerator.class);

	private final ISQLGenerator tablePhysPropsGenerator;

	public OracleMaterializedViewGenerator() {
		this.tablePhysPropsGenerator = GeneratorFactory.getGenerator(
				IElementType.getInstance(ITablePhysicalProperties.TYPE_ID), DBVendor.ORACLE);
		tablePhysPropsGenerator.setVendor(DBVendor.ORACLE);
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IMaterializedView src = (IMaterializedView) result.getSource();
		final IMaterializedView tgt = (IMaterializedView) result.getTarget();

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		if ((src.getSql() != null && tgt.getSql() != null && !src.getSql().trim()
				.equals(tgt.getSql().trim()))
				|| isRenamed(result)) {
			genResult.integrate(doDrop(tgt));
			genResult.integrate(generateFullSQL(src));
		} else {
			// Generating child constraints
			IGenerationResult keysGeneration = generateTypedChildren(TableMerger.CATEGORY_KEYS,
					result, IElementType.getInstance("FOREIGN_KEY"), true); //$NON-NLS-1$
			// Generating child check constraints
			IGenerationResult checksGeneration = generateTypedChildren(
					OracleTableMerger.CATEGORY_CHECKS, result,
					IElementType.getInstance(ICheckConstraint.TYPE_ID), true);
			// Generating the physical properties alter
			IGenerationResult physGeneration = generateChildren(OracleTableMerger.ATTR_PHYSICAL,
					result, tablePhysPropsGenerator, false);

			// Tagging child generations
			tagScriptType(keysGeneration, ScriptType.MAT_VIEW);
			tagScriptType(physGeneration, ScriptType.MAT_VIEW);
			genResult.integrate(keysGeneration);
			genResult.integrate(checksGeneration);
			genResult.integrate(physGeneration);
		}

		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IMaterializedView view = (IMaterializedView) model;
		final String viewName = getName(view);

		ISQLScript script = getSqlScript(viewName, view.getDescription(), ScriptType.TABLE);
		script.appendSQL(prompt("Dropping materialized view '" + viewName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		script.appendSQL("DROP MATERIALIZED VIEW ").appendSQL(viewName); //$NON-NLS-1$
		closeLastStatement(script);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(view.getType(), viewName), script);

		return genResult;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IMaterializedView view = (IMaterializedView) model;
		final String viewName = getName(view);
		final String rawName = view.getName();

		ISQLScript script = getSqlScript(viewName, null, ScriptType.MAT_VIEW);
		script.appendSQL(prompt("Creating materialized view '" + rawName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		script.appendSQL("CREATE MATERIALIZED VIEW ").appendSQL(viewName).appendSQL(" "); //$NON-NLS-1$ //$NON-NLS-2$

		if (view.getPhysicalProperties() != null) {
			final IPhysicalProperties props = view.getPhysicalProperties();
			IGenerationResult propGeneration = tablePhysPropsGenerator.generateFullSQL(props);
			if (propGeneration != null) {
				Collection<ISQLScript> generatedScripts = propGeneration.getAdditions();
				for (ISQLScript s : generatedScripts) {
					script.appendScript(s);
				}
			}
		}
		if (view.getBuildType() != null) {
			script.appendSQL(" BUILD ").appendSQL(view.getBuildType().name()); //$NON-NLS-1$
		}
		script.appendSQL(NEWLINE);
		if (view.getRefreshTime() != RefreshTime.NEVER) {
			script.appendSQL("  REFRESH ").appendSQL(view.getRefreshMethod().name()); //$NON-NLS-1$
			switch (view.getRefreshTime()) {
			case COMMIT:
			case DEMAND:
				script.appendSQL(" ON ").appendSQL(view.getRefreshTime().toString()); //$NON-NLS-1$
				break;
			case SPECIFY:
				if (view.getStartExpr() != null && !"".equals(view.getStartExpr().trim())) { //$NON-NLS-1$
					script.appendSQL(NEWLINE).appendSQL("  START WITH ") //$NON-NLS-1$
							.appendSQL(view.getStartExpr().trim());
				}
				if (view.getNextExpr() != null && !"".equals(view.getNextExpr().trim())) { //$NON-NLS-1$
					script.appendSQL(NEWLINE).appendSQL("  NEXT ") //$NON-NLS-1$
							.appendSQL(view.getNextExpr().trim());
				}
				break;
			}
			script.appendSQL(NEWLINE + " WITH ").appendSQL( //$NON-NLS-1$
					view.getViewType().name().replace("_", " ")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			script.appendSQL("  NEVER REFRESH"); //$NON-NLS-1$
		}
		script.appendSQL(" ").appendSQL((view.isQueryRewriteEnabled() ? "ENABLE" : "DISABLE")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.appendSQL(" QUERY REWRITE"); //$NON-NLS-1$
		String query = view.getSql();
		if (query != null && !"".equals(query.trim())) { //$NON-NLS-1$
			script.appendSQL(NEWLINE).appendSQL("  AS ").appendSQL(query.trim().replace(";", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		closeLastStatement(script);

		// Getting constraint generation result (resolving dependencies)
		IGenerationResult keyGeneration = generateChildren(view.getConstraints(), true);
		// Getting Check constraint generation result (resolving dependencies)
		IGenerationResult checkGeneration = generateChildren(view.getCheckConstraints(), true);
		// Tagging key generation
		tagScriptType(keyGeneration, ScriptType.MAT_VIEW);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addAdditionScript(new DatabaseReference(view.getType(), rawName), script);
		genResult.integrate(keyGeneration);
		genResult.integrate(checkGeneration);

		// Building dependencies
		GenerationHelper.addSqlGenerationPreconditions(genResult, view.getName(), view,
				IElementType.getInstance(IMaterializedView.VIEW_TYPE_ID));

		return genResult;
	}

	/**
	 * Tags all scripts from this generation result as the specified script
	 * type. This is used by this generator to avoid script ordering problems:
	 * PK before mat views but mat views may generate PK for themselves...
	 * 
	 * @param r
	 * @param type
	 */
	private void tagScriptType(IGenerationResult r, ScriptType type) {
		if (r == null)
			return;
		tagScriptType(r.getAdditions(), type);
		tagScriptType(r.getUpdates(), type);
		tagScriptType(r.getDrops(), type);
	}

	private void tagScriptType(Collection<ISQLScript> scripts, ScriptType type) {
		for (ISQLScript s : scripts) {
			s.setScriptType(type);
		}
	}

}
