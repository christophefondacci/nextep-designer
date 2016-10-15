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
package com.nextep.designer.sqlgen.generic.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.mergers.TableMerger;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class TableGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IBasicTable newTable = (IBasicTable) result.getSource();
		final IBasicTable oldTable = (IBasicTable) result.getTarget();
		final String newTabName = getName(newTable);
		final String oldTabName = getName(oldTable.getName(), newTable);
		final String escapedOldTabName = escape(oldTabName);
		final String escapedNewTabName = escape(newTabName);

		// Generating child columns
		IGenerationResult columnsGeneration = generateTypedChildren(TableMerger.CATEGORY_COLUMNS,
				result, IElementType.getInstance(IBasicColumn.TYPE_ID), false);
		// Generating child constraints
		IGenerationResult keysGeneration = generateTypedChildren(TableMerger.CATEGORY_KEYS, result,
				IElementType.getInstance(ForeignKeyConstraint.TYPE_ID), true);
		// Generating the physical properties alter
		IGenerationResult physGeneration = generateTypedChildren(TableMerger.ATTR_PHYSICAL, result,
				IElementType.getInstance(ITablePhysicalProperties.TYPE_ID), false);

		// Generating our SQL ALTER TABLE script
		ISQLScript alterScript = getSqlScript(newTabName, newTable.getDescription(),
				ScriptType.TABLE);

		// Should we rename this table
		if (isRenamed(result)) {
			alterScript.appendSQL(prompt("Renaming table '" + oldTabName + "' to '" + newTabName //$NON-NLS-1$ //$NON-NLS-2$
					+ "'...")); //$NON-NLS-1$
			alterScript.appendSQL("ALTER TABLE ").appendSQL(escapedOldTabName) //$NON-NLS-1$
					.appendSQL(" RENAME TO ").appendSQL(escapedNewTabName); //$NON-NLS-1$
			closeLastStatement(alterScript);
		}

		// Generating alter column section
		if (!columnsGeneration.getAdditions().isEmpty()
				|| !columnsGeneration.getUpdates().isEmpty()) {
			alterScript.appendSQL(prompt("Altering table '" + newTabName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Additions
		if (!columnsGeneration.getAdditions().isEmpty()) {
			for (ISQLScript s : columnsGeneration.getAdditions()) {
				alterScript.appendSQL("ALTER TABLE ").appendSQL(escapedNewTabName) //$NON-NLS-1$
						.appendSQL(" ADD ").appendSQL(s.getSql()); //$NON-NLS-1$
				// alterScript.appendSQL(getParser().getStatementDelimiter()).appendSQL(NEWLINE);
				closeLastStatement(alterScript);
			}
			columnsGeneration.getAdditions().clear();
			columnsGeneration.getAddedReferences().clear();
		}

		// column updates hook
		if (!columnsGeneration.getUpdates().isEmpty()) {
			addAlterColumnSQL(newTable, alterScript, columnsGeneration.getUpdates());
			columnsGeneration.getUpdates().clear();
			columnsGeneration.getUpdatedReferences().clear();
		}

		// Generating our result
		IGenerationResult r = GenerationFactory.createGenerationResult(newTabName);
		if (!"".equals(alterScript.getSql())) { //$NON-NLS-1$
			r.addUpdateScript(new DatabaseReference(newTable.getType(), newTabName), alterScript);
		}
		r.integrate(columnsGeneration); // Will only contain any residual DROP
										// script
		r.integrate(keysGeneration);
		r.integrate(physGeneration);
		return r;
	}

	/**
	 * Hook to allow extensions to plug alter column statements if they support
	 * it.
	 * 
	 * @param t
	 *            table being altered
	 * @param script
	 *            current generated SQL script to populate
	 * @param columnUpdates
	 *            SQL-generation of column alters
	 */
	protected void addAlterColumnSQL(IBasicTable t, ISQLScript script,
			List<ISQLScript> columnUpdates) {
		// Generic Entry-SQL compliant databases do not support column alters
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IBasicTable table = (IBasicTable) model;
		final String tableName = getName(table);

		ISQLScript script = getSqlScript(tableName, table.getDescription(), ScriptType.TABLE);
		// USing the reference manager
		script.appendSQL(prompt("Dropping table '" + tableName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		script.appendSQL("DROP TABLE ").appendSQL(escape(tableName)); //$NON-NLS-1$
		closeLastStatement(script);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(table.getType(), tableName), script);
		// Handling preconditions for proper drop order resolution
		GenerationHelper.addForeignKeyPreconditions(r, table);
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IBasicTable table = (IBasicTable) model;
		final String tabName = getName(table);

		// Verifying consistency
		if (table.getColumns().size() == 0) {
			throw new ErrorException("Table <" + tabName
					+ "> has no column defined, cannot generate.");
		}
		// Initializing our column generation result
		IGenerationResult columnGeneration = generateChildren(table.getColumns(), false);

		// Getting constraint generation result (resolving dependencies)
		List<IKeyConstraint> keys = new ArrayList<IKeyConstraint>(table.getConstraints());
		Collections.sort(keys, NameComparator.getInstance());
		IGenerationResult keyGeneration = generateChildren(keys, true);

		// Initializing output script
		ISQLScript tableScript = getSqlScript(tabName, table.getDescription(), ScriptType.TABLE);
		tableScript.appendSQL(prompt("Creating table '" + tabName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		tableScript.appendSQL(getCreateTable(table)).appendSQL(escape(tabName));

		// Appending columns definition since these are partials
		addCommaSeparatedScripts(tableScript, " ( ", ")", columnGeneration.getAdditions()); //$NON-NLS-1$ //$NON-NLS-2$

		// Generating physical implementation when supported
		if (table instanceof IPhysicalObject) {
			final IPhysicalObject physTable = (IPhysicalObject) table;
			final IPhysicalProperties props = physTable.getPhysicalProperties();
			ISQLGenerator propGenerator = getGenerator(IElementType
					.getInstance(ITablePhysicalProperties.TYPE_ID));
			if (propGenerator != null && props != null) {
				// Generating the physical implementation
				IGenerationResult propGeneration = propGenerator.generateFullSQL(props);
				// Aggregating implementation
				if (propGeneration != null) {
					Collection<ISQLScript> generatedScripts = propGeneration.getAdditions();
					for (ISQLScript s : generatedScripts) {
						tableScript.appendScript(s);
					}
				}
			}
		}
		closeLastStatement(tableScript);

		// Generating results
		IGenerationResult genResult = GenerationFactory.createGenerationResult(tabName);
		genResult.addAdditionScript(new DatabaseReference(table.getType(), tabName), tableScript);
		genResult.integrate(keyGeneration);

		return genResult;
	}

	protected String getCreateTable(IBasicTable t) {
		return "CREATE TABLE "; //$NON-NLS-1$
	}
}
