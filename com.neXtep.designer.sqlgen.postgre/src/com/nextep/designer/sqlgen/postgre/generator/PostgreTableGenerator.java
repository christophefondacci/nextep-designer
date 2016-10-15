/**
 * Copyright (c) 2011 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.designer.sqlgen.postgre.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.mergers.TableMerger;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.ICheckConstraintContainer;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlTable;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * This specific PostgreSQL table generator adds support for PostgreSQL table
 * alteration by generating the appropriate ALTER TABLE statement using
 * underlying column generations, and delegates the CREATE and DROP statements
 * generation to the generic JDBC generator.
 * 
 * @author Bruno Gautier
 */
public class PostgreTableGenerator extends SQLGenerator {

	private final static Log LOGGER = LogFactory.getLog(PostgreTableGenerator.class);
	private final ISQLGenerator jdbcGenerator;

	public PostgreTableGenerator() {
		jdbcGenerator = GeneratorFactory.getGenerator(
				IElementType.getInstance(IBasicTable.TYPE_ID), DBVendor.JDBC);
		jdbcGenerator.setVendor(DBVendor.POSTGRE);
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IBasicTable table = (IBasicTable) model;
		final IPostgreSqlTable postgreTable = (table instanceof IPostgreSqlTable) ? (IPostgreSqlTable) table
				: null;
		final String tabName = getName(table);
		final String rawName = table.getName();

		@SuppressWarnings("unchecked")
		final Set<IReference> inheritedTableRefs = postgreTable == null ? Collections.EMPTY_SET
				: postgreTable.getInheritances();

		// Verifying consistency
		if (table.getColumns().size() == 0 && inheritedTableRefs.size() == 0) {
			throw new ErrorException("Table <" + tabName
					+ "> has no column defined, cannot generate.");
		}
		// Initializing our column generation result
		IGenerationResult columnGeneration = generateChildren(table.getColumns(), false);

		// Getting constraint generation result (resolving dependencies)
		List<IKeyConstraint> keys = new ArrayList<IKeyConstraint>(table.getConstraints());
		Collections.sort(keys, NameComparator.getInstance());
		IGenerationResult keyGeneration = generateChildren(keys, true);

		// Getting Check constraint generation result (resolving dependencies)
		IGenerationResult checkGeneration = null;
		if (table instanceof ICheckConstraintContainer) {
			List<ICheckConstraint> checks = new ArrayList<ICheckConstraint>(
					((ICheckConstraintContainer) table).getCheckConstraints());
			Collections.sort(checks, NameComparator.getInstance());
			checkGeneration = generateChildren(checks, true);
		}

		// Initializing output script
		ISQLScript tableScript = getSqlScript(tabName, table.getDescription(), ScriptType.TABLE);
		tableScript.appendSQL(prompt("Creating table '" + tabName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		tableScript.appendSQL(getCreateTable(table)).appendSQL(escape(tabName));

		// Appending columns definition since these are partials
		addCommaSeparatedScripts(tableScript, " ( ", ")", columnGeneration.getAdditions()); //$NON-NLS-1$ //$NON-NLS-2$

		if (inheritedTableRefs.size() > 0) {
			String inheritSQL = " INHERITS FROM " + NEWLINE; //$NON-NLS-1$
			boolean first = true;
			for (IReference tableRef : inheritedTableRefs) {
				final IBasicTable inheritedTable = (IBasicTable) VersionHelper
						.getReferencedItem(tableRef);
				if (inheritedTable != null) {
					inheritSQL = inheritSQL.concat("    "); //$NON-NLS-1$
					if (!first) {
						inheritSQL = inheritSQL.concat(","); //$NON-NLS-1$
					} else {
						inheritSQL = inheritSQL.concat(" "); //$NON-NLS-1$
						first = false;
					}

					final String inheritedTabName = getName(inheritedTable);
					inheritSQL = inheritSQL.concat(inheritedTabName + NEWLINE);
				} else {
					LOGGER.warn("Inherited table reference not found from PostgreSql table '"
							+ table.getName() + "' : " + tableRef);
				}
			}
			inheritSQL = inheritSQL.concat(")" + NEWLINE);
			tableScript.appendSQL(inheritSQL);
		}

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
		IGenerationResult genResult = GenerationFactory.createGenerationResult(rawName);
		genResult.addAdditionScript(new DatabaseReference(table.getType(), rawName), tableScript);
		genResult.integrate(keyGeneration);
		genResult.integrate(checkGeneration);

		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		return jdbcGenerator.doDrop(model);
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		final IBasicTable newTable = (IBasicTable) result.getSource();
		final String newTableName = newTable.getName();
		final IBasicTable oldTable = (IBasicTable) result.getTarget();
		final String oldTableName = oldTable.getName();

		IGenerationResult genResult = GenerationFactory.createGenerationResult(newTableName);
		ISQLScript alterScript = getSqlScript(newTableName, newTable.getDescription(),
				ScriptType.TABLE);

		// Renaming the table if its name has changed
		if (isRenamed(result)) {
			alterScript
					.appendSQL(prompt("Renaming table '" + oldTableName + "' to '" + newTableName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			alterScript
					.appendSQL("ALTER TABLE ").appendSQL(escape(oldTableName)).appendSQL(" RENAME TO ") //$NON-NLS-1$ //$NON-NLS-2$
					.appendSQL(escape(newTableName));
			closeLastStatement(alterScript);
		}

		// Altering the columns
		IGenerationResult colsGeneration = generateTypedChildren(TableMerger.CATEGORY_COLUMNS,
				result, IElementType.getInstance(IBasicColumn.TYPE_ID), false);

		// Handling columns additions and updates
		List<ISQLScript> colsAdditions = colsGeneration.getAdditions();
		List<ISQLScript> colsUpdates = colsGeneration.getUpdates();
		if (!colsAdditions.isEmpty() || !colsUpdates.isEmpty()) {
			alterScript.appendSQL(prompt("Altering table '" + newTableName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$

			/*
			 * We start by renaming columns because RENAME command cannot be
			 * chained with other ALTER commands. We look for ALTER TABLE
			 * commands into the list of columns scripts. These scripts
			 * correspond to statements for renaming columns.
			 */
			for (ISQLScript script : new ArrayList<ISQLScript>(colsUpdates)) {
				if (script.getSql().trim().toUpperCase().startsWith("ALTER TABLE")) { //$NON-NLS-1$
					alterScript.appendSQL(script.getSql());
					colsUpdates.remove(script);
				}
			}

			if (!colsAdditions.isEmpty() || !colsUpdates.isEmpty()) {
				alterScript.appendSQL("ALTER TABLE ").appendSQL(escape(newTableName)); //$NON-NLS-1$

				// Prefixing all addition scripts with an ADD COLUMN command
				for (ISQLScript script : colsAdditions) {
					script.setSql("ADD COLUMN " + script.getSql()); //$NON-NLS-1$
				}

				List<ISQLScript> colsChanges = new ArrayList<ISQLScript>(colsAdditions);
				colsChanges.addAll(colsUpdates);

				// Adding columns additions and updates to the alter SQL script
				addCommaSeparatedScripts(alterScript, "", "", colsChanges); //$NON-NLS-1$ //$NON-NLS-2$

				// Clearing the addition and update scripts so that they won't
				// be integrated later
				colsAdditions.clear();
				colsGeneration.getAddedReferences().clear();
				colsUpdates.clear();
				colsGeneration.getUpdatedReferences().clear();

				alterScript.appendSQL(getSQLCommandWriter().closeStatement());
			}
		}

		/*
		 * Add the generated update script if not empty. This script contains
		 * both additions and updates. The deletion scripts will be integrated
		 * below. FIXME [BGA] Check if this script should be considered as an
		 * update script or an addition script.
		 */
		if (!"".equals(alterScript.getSql())) { //$NON-NLS-1$
			genResult.addUpdateScript(new DatabaseReference(newTable.getType(), newTableName),
					alterScript);
		}

		/*
		 * Handling columns deletions. The columns generation result now only
		 * contains residual DROP results since additions and updates have been
		 * removed when generating the alter script.
		 */
		genResult.integrate(colsGeneration);

		// Altering foreign key constraints
		IGenerationResult keysGeneration = generateTypedChildren(TableMerger.CATEGORY_KEYS, result,
				IElementType.getInstance(ForeignKeyConstraint.TYPE_ID), true);
		genResult.integrate(keysGeneration);
		// Generating the physical properties alter
		IGenerationResult physGeneration = generateTypedChildren(TableMerger.ATTR_PHYSICAL, result,
				IElementType.getInstance(ITablePhysicalProperties.TYPE_ID), false);
		genResult.integrate(physGeneration);

		return genResult;
	}

	protected String getCreateTable(IBasicTable t) {
		return "CREATE TABLE "; //$NON-NLS-1$
	}

}
