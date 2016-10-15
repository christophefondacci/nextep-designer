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
package com.nextep.designer.sqlgen.oracle.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.NameComparator;
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
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.merge.OracleTableMerger;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.PhysicalOrganisation;
import com.nextep.designer.dbgm.oracle.services.DBOMHelper;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * SQL generator for Oracle database. This class generates a table object model
 * to SQL. This generator can now be invoked from a JDBC workspace, so
 * implementation should not assume we have oracle specific interfaces and
 * should always check before casting.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleTableGenerator extends SQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(OracleTableGenerator.class);

	private final ISQLGenerator commentGenerator;
	private final ISQLGenerator tablePhysPropsGenerator;

	public OracleTableGenerator() {
		// Column comment is not a typed object so we directly instantiate the
		// generator
		this.commentGenerator = new OracleColumnCommentGenerator();

		this.tablePhysPropsGenerator = GeneratorFactory.getGenerator(
				IElementType.getInstance(ITablePhysicalProperties.TYPE_ID), DBVendor.ORACLE);
		tablePhysPropsGenerator.setVendor(DBVendor.ORACLE);
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IBasicTable table = (IBasicTable) model;
		final String tabName = getName(table);
		final String tabDesc = table.getDescription();

		IOracleTable oracleTable = null;
		if (table instanceof IOracleTable) {
			oracleTable = (IOracleTable) table;
		}

		IGenerationResult genResult = GenerationFactory.createGenerationResult(tabName);
		// Verifying consistency
		if (table.getColumns().size() == 0) {
			throw new ErrorException("Table <" + tabName
					+ "> has no column defined, cannot generate.");
		}

		// Initializing our column generation result
		IGenerationResult columnGeneration = generateChildren(table.getColumns(), false);
		// Generating columns comments
		final IGenerationResult commentGeneration = generateChildren(table.getColumns(),
				commentGenerator, false);

		// Getting constraint generation result (resolving dependencies)
		List<IKeyConstraint> keys = new ArrayList<IKeyConstraint>(table.getConstraints());
		Collections.sort(keys, NameComparator.getInstance());
		IGenerationResult keyGeneration = generateChildren(keys, true);

		// Getting Check constraint generation result (resolving dependencies)
		IGenerationResult checkGeneration = null;
		if (oracleTable != null) {
			List<ICheckConstraint> checks = new ArrayList<ICheckConstraint>(
					oracleTable.getCheckConstraints());
			Collections.sort(checks, NameComparator.getInstance());
			checkGeneration = generateChildren(checks, true);
		}

		// Initializing output script
		ISQLScript tableScript = getSqlScript(tabName, tabDesc, ScriptType.TABLE);
		tableScript.appendSQL(prompt("Creating table '" + tabName + "'")); //$NON-NLS-1$ //$NON-NLS-2$
		tableScript.appendSQL("CREATE "); //$NON-NLS-1$

		// Handling temporary tables
		if (table.isTemporary()) {
			tableScript.appendSQL("GLOBAL TEMPORARY "); //$NON-NLS-1$
		}
		tableScript.appendSQL("TABLE ").appendSQL(tabName); //$NON-NLS-1$

		// Appending columns definition since these are partials
		addCommaSeparatedScripts(tableScript, " ( ", "", columnGeneration.getAdditions()); //$NON-NLS-1$ //$NON-NLS-2$
		// Adding physical properties, if any
		final IOracleClusteredTable clusteredTable = DBOMHelper.getClusterFor(table);
		if (clusteredTable != null) {
			tableScript.appendSQL(") CLUSTER " + getName(clusteredTable.getCluster()) + " ("); //$NON-NLS-1$ //$NON-NLS-2$
			// Browsing cluster columns in cluster column orders
			boolean first = true;
			for (IBasicColumn c : clusteredTable.getCluster().getColumns()) {
				final IBasicColumn tabCol = clusteredTable.getColumnMapping(c.getReference());
				if (first) {
					first = false;
				} else {
					tableScript.appendSQL(", "); //$NON-NLS-1$
				}
				if (tabCol != null) {
					tableScript.appendSQL(tabCol.getName());
				} else {
					LOGGER.warn("Unable to generate correct SQL statement for cluster column mapping on "
							+ tabName);
				}
			}
			tableScript.appendSQL(")").appendSQL(NEWLINE); //$NON-NLS-1$
			genResult.addPrecondition(new DatabaseReference(clusteredTable.getCluster().getType(),
					clusteredTable.getCluster().getName()));
		} else if (oracleTable != null && oracleTable.getPhysicalProperties() != null) {
			final IOracleTablePhysicalProperties props = oracleTable.getPhysicalProperties();
			if (tablePhysPropsGenerator != null) {
				// Generating primary key for IOT tables
				if (props.getPhysicalOrganisation() == PhysicalOrganisation.INDEX) {
					IKeyConstraint pk = DBGMHelper.getPrimaryKey(table);
					// Generating inline primary key constraint for IOT
					if (pk != null) {
						tableScript.appendSQL("          ,constraint ").appendSQL(getName(pk)) //$NON-NLS-1$
								.appendSQL(" primary key ("); //$NON-NLS-1$
						boolean first = true;
						for (IBasicColumn c : pk.getColumns()) {
							if (!first) {
								tableScript.appendSQL(","); //$NON-NLS-1$
							} else {
								first = false;
							}
							tableScript.appendSQL(c.getName());
						}
						tableScript.appendSQL(")").appendSQL(NEWLINE); //$NON-NLS-1$
					}
				}
				// Closing column declaration
				tableScript.appendSQL(")").appendSQL(NEWLINE); //$NON-NLS-1$
				IGenerationResult propGeneration = tablePhysPropsGenerator.generateFullSQL(props);
				if (propGeneration != null) {
					Collection<ISQLScript> generatedScripts = propGeneration.getAdditions();
					for (ISQLScript s : generatedScripts) {
						tableScript.appendScript(s);
					}
				}
			} else {
				// Closing column declaration
				tableScript.appendSQL(")").appendSQL(NEWLINE); //$NON-NLS-1$
			}
		} else {
			// Closing column declaration
			tableScript.appendSQL(")").appendSQL(NEWLINE); //$NON-NLS-1$
		}
		// Terminating table definition
		tableScript.appendSQL(getParser().getStatementDelimiter()).appendSQL(NEWLINE);

		// Generating table comment
		if (tabDesc != null && !"".equals(tabDesc.trim())) { //$NON-NLS-1$
			tableScript.appendSQL("COMMENT ON TABLE ").appendSQL(tabName).appendSQL(" IS '") //$NON-NLS-1$ //$NON-NLS-2$
					.appendSQL(tabDesc.replace("'", "''")).appendSQL("'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			closeLastStatement(tableScript);
		}
		for (ISQLScript s : columnGeneration.getUpdates()) {
			tableScript.appendScript(s);
		}
		// Generating results
		genResult.addAdditionScript(new DatabaseReference(table.getType(), tabName), tableScript);
		genResult.integrate(keyGeneration);
		genResult.integrate(checkGeneration);
		genResult.integrate(commentGeneration);

		return genResult;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IBasicTable newTable = (IBasicTable) result.getSource();
		final IBasicTable oldTable = (IBasicTable) result.getTarget();
		final String newTabName = getName(newTable);
		final String rawTabName = newTable.getName();
		final String newTabDesc = newTable.getDescription();

		// Switching a temporary table state is equivalent to drop / create
		if (newTable.isTemporary() != oldTable.isTemporary()) {
			IGenerationResult r = doDrop(oldTable);
			r.integrate(generateFullSQL(newTable));
			return r;
		}

		// Generating child columns
		IGenerationResult columnsGeneration = generateTypedChildren(TableMerger.CATEGORY_COLUMNS,
				result, IElementType.getInstance("COLUMN"), false); //$NON-NLS-1$
		// Generating column comments
		IGenerationResult commentsGeneration = generateChildren(TableMerger.CATEGORY_COLUMNS,
				result, commentGenerator, false);
		// Generating child constraints
		IGenerationResult keysGeneration = generateTypedChildren(TableMerger.CATEGORY_KEYS, result,
				IElementType.getInstance("FOREIGN_KEY"), true); //$NON-NLS-1$
		// Generating child check constraints
		IGenerationResult checksGeneration = generateTypedChildren(
				OracleTableMerger.CATEGORY_CHECKS, result,
				IElementType.getInstance(ICheckConstraint.TYPE_ID), true);
		// Generating the physical properties alter
		IGenerationResult physGeneration = generateChildren(TableMerger.ATTR_PHYSICAL, result,
				tablePhysPropsGenerator, false);

		// Generating our SQL ALTER TABLE script
		ISQLScript alterScript = getSqlScript(newTabName, newTabDesc, ScriptType.TABLE);

		// Should we rename this table
		if (isRenamed(result)) {
			alterScript.appendSQL(prompt("Renaming table '" + oldTable.getName() + "' to '" //$NON-NLS-1$ //$NON-NLS-2$
					+ rawTabName + "'...")); //$NON-NLS-1$
			alterScript.appendSQL("ALTER TABLE ").appendSQL(getName(oldTable.getName(), newTable)) //$NON-NLS-1$
					.appendSQL(" RENAME TO ").appendSQL(newTabName); //$NON-NLS-1$
			closeLastStatement(alterScript);
		}

		// Generating alter column section
		if (!columnsGeneration.getAdditions().isEmpty()
				|| !columnsGeneration.getUpdates().isEmpty()) {
			alterScript.appendSQL(prompt("Altering table '" + rawTabName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$

			// Renaming columns first
			for (ISQLScript s : new ArrayList<ISQLScript>(columnsGeneration.getUpdates())) {
				if (s.getSql().trim().toUpperCase().startsWith("ALTER TABLE")) { //$NON-NLS-1$
					alterScript.appendSQL(s.getSql());
					columnsGeneration.getUpdates().remove(s);
				}
			}

			if (!columnsGeneration.getAdditions().isEmpty()
					|| !columnsGeneration.getUpdates().isEmpty()) {
				alterScript.appendSQL("ALTER TABLE ").appendSQL(newTabName).appendSQL(NEWLINE); //$NON-NLS-1$

				// Adding scripts
				addCommaSeparatedScripts(alterScript, "   ADD    (", "   )", //$NON-NLS-1$ //$NON-NLS-2$
						columnsGeneration.getAdditions());

				// We consider removed columns as modify (set to nullable)
				addCommaSeparatedScripts(alterScript, "   MODIFY (", "   )", //$NON-NLS-1$ //$NON-NLS-2$
						columnsGeneration.getUpdates());

				alterScript.appendSQL(getParser().getStatementDelimiter()).appendSQL(NEWLINE);
			}
			columnsGeneration.getAdditions().clear();
			columnsGeneration.getAddedReferences().clear();
			columnsGeneration.getUpdates().clear();
			columnsGeneration.getUpdatedReferences().clear();
		}

		// Generating comments
		if (newTabDesc != null && !"".equals(newTabDesc.trim())) { //$NON-NLS-1$
			if (oldTable == null || oldTable.getDescription() == null
					|| !newTabDesc.equals(oldTable.getDescription())) {
				alterScript.appendSQL("COMMENT ON TABLE ").appendSQL(newTabName).appendSQL(" IS '") //$NON-NLS-1$ //$NON-NLS-2$
						.appendSQL(newTabDesc.replace("'", "''")).appendSQL("'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				closeLastStatement(alterScript);
			}
		}

		// Generating our result
		IGenerationResult genResult = GenerationFactory.createGenerationResult(rawTabName);
		if (!"".equals(alterScript.getSql())) { //$NON-NLS-1$
			genResult.addAdditionScript(new DatabaseReference(newTable.getType(), rawTabName),
					alterScript);
		}
		genResult.integrate(columnsGeneration); // Will only contain any
												// residual DROP script
		genResult.integrate(keysGeneration);
		genResult.integrate(commentsGeneration);
		IOracleClusteredTable clusteredTab = DBOMHelper.getClusterFor(newTable);
		if (clusteredTab == null) {
			genResult.integrate(physGeneration);
		}
		genResult.integrate(checksGeneration);

		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IOracleTable table = (IOracleTable) model;
		final String tabName = getName(table);
		final String rawTabName = table.getName();

		ISQLScript script = getSqlScript(tabName, table.getDescription(), ScriptType.TABLE);

		script.appendSQL(prompt("Dropping table '" + rawTabName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		script.appendSQL("DROP TABLE ").appendSQL(tabName); //$NON-NLS-1$
		closeLastStatement(script);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(table.getType(), rawTabName), script);
		GenerationHelper.addForeignKeyPreconditions(genResult, table);

		return genResult;
	}

}
