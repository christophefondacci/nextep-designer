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
package com.nextep.designer.sqlgen.mysql.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.mergers.TableMerger;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * MySql generator for tables.<br>
 * Starting from 1.0.3, this generator will be invoked by JDBC workspaces so
 * implementation should never assume that the current generated table is a
 * {@link IMySQLTable} and should always check the implementation before
 * accessing MySql-specific features.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MySQLTableGenerator extends MySQLGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// Getting our source table to generate
		final IBasicTable newTable = (IBasicTable) result.getSource();
		final IBasicTable oldTable = (IBasicTable) result.getTarget();
		final String newTabName = newTable.getName();
		final String oldTabName = oldTable.getName();

		// Generating child columns
		IGenerationResult columnsGeneration = generateTypedChildren(TableMerger.CATEGORY_COLUMNS,
				result, IElementType.getInstance(IBasicColumn.TYPE_ID), false);
		// Generating child constraints
		IGenerationResult keysGeneration = generateTypedChildren(TableMerger.CATEGORY_KEYS, result,
				IElementType.getInstance(ForeignKeyConstraint.TYPE_ID), true);

		// Generating our SQL ALTER TABLE script
		ISQLScript alterScript = new SQLScript(newTabName, newTable.getDescription(), "", //$NON-NLS-1$
				ScriptType.TABLE);

		// Should we rename this table?
		if (isRenamed(result)) {
			alterScript.appendSQL(prompt("Renaming table '" + oldTabName + "' to '" //$NON-NLS-1$ //$NON-NLS-2$
					+ newTabName + "'...")); //$NON-NLS-1$
			alterScript.appendSQL("ALTER TABLE ").appendSQL(escape(oldTabName)) //$NON-NLS-1$
					.appendSQL(" RENAME TO ").appendSQL(escape(newTabName)); //$NON-NLS-1$
			closeLastStatement(alterScript);
		}

		// Generating alter columns section
		List<ISQLScript> colsAdditions = columnsGeneration.getAdditions();
		List<ISQLScript> colsUpdates = columnsGeneration.getUpdates();
		if (!colsAdditions.isEmpty() || !colsUpdates.isEmpty()) {
			alterScript.appendSQL(prompt("Altering table '" + newTabName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$

			alterScript.appendSQL("ALTER TABLE ").appendSQL(escape(newTabName)); //$NON-NLS-1$

			// Prefixing all addition scripts with an ADD COLUMN command
			for (ISQLScript script : colsAdditions) {
				script.setSql("ADD COLUMN " + script.getSql()); //$NON-NLS-1$
			}

			List<ISQLScript> colsChanges = new ArrayList<ISQLScript>(colsAdditions);
			colsChanges.addAll(colsUpdates);

			// Adding columns additions and updates to the alter SQL script
			addCommaSeparatedScripts(alterScript, "", "", colsChanges); //$NON-NLS-1$ //$NON-NLS-2$

			closeLastStatement(alterScript);

			// Clearing the addition and update scripts so that they won't be
			// integrated later
			colsAdditions.clear();
			columnsGeneration.getAddedReferences().clear();
			colsUpdates.clear();
			columnsGeneration.getUpdatedReferences().clear();
		}

		// Handling charset / engine updates
		if (newTable instanceof IMySQLTable && oldTable instanceof IMySQLTable) {
			final IMySQLTable mysqlSrc = (IMySQLTable) newTable;
			final IMySQLTable mysqlTgt = (IMySQLTable) oldTable;
			final String srcCharset = mysqlSrc.getCharacterSet();
			final String tgtCharset = mysqlTgt.getCharacterSet();
			final String srcCollation = mysqlSrc.getCollation();
			final String tgtCollation = mysqlTgt.getCollation();

			// If charset OR collation differs, we need a charset conversion
			if ((srcCharset != null && !srcCharset.equals(tgtCharset))
					|| (srcCollation != null && !"".equals(srcCollation.trim()) && !srcCollation //$NON-NLS-1$
							.equals(tgtCollation))) {
				alterScript.appendSQL(prompt("Changing table character set")); //$NON-NLS-1$
				alterScript.appendSQL("ALTER TABLE ").appendSQL(escape(newTabName)) //$NON-NLS-1$
						.appendSQL(" CONVERT TO CHARACTER SET ").appendSQL(srcCharset); //$NON-NLS-1$
				if (srcCollation != null && !"".equals(srcCollation.trim())) { //$NON-NLS-1$
					alterScript.appendSQL(" COLLATE ").appendSQL(srcCollation); //$NON-NLS-1$
				}
				closeLastStatement(alterScript);
			}

			if (mysqlSrc.getEngine() != null && !mysqlSrc.getEngine().equals(mysqlTgt.getEngine())) {
				alterScript.appendSQL(prompt("Changing '" + newTabName //$NON-NLS-1$
						+ "' storage engine from " + mysqlTgt.getEngine() + " to " //$NON-NLS-1$ //$NON-NLS-2$
						+ mysqlSrc.getEngine()));
				alterScript.appendSQL("ALTER TABLE ").appendSQL(escape(newTabName)) //$NON-NLS-1$
						.appendSQL(" Engine=").appendSQL(mysqlSrc.getEngine()); //$NON-NLS-1$
				closeLastStatement(alterScript);
			}
		}

		// Generating table comments
		final String srcDesc = newTable.getDescription();
		if (!isEmpty(srcDesc) && !srcDesc.equals(oldTable.getDescription())) {
			alterScript.appendSQL("ALTER TABLE ").appendSQL(escape(newTabName)) //$NON-NLS-1$
					.appendSQL(" COMMENT '").appendSQL(getComment(newTable).replace("'", "''")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.appendSQL("'"); //$NON-NLS-1$
			closeLastStatement(alterScript);
		}

		// Generating our result
		IGenerationResult r = GenerationFactory.createGenerationResult(newTabName);
		if (!"".equals(alterScript.getSql())) { //$NON-NLS-1$
			r.addAdditionScript(new DatabaseReference(newTable.getType(), newTabName), alterScript);
		}
		r.integrate(columnsGeneration); // Will only contain any residual DROP
										// script
		r.integrate(keysGeneration);

		return r;
	}

	private String getComment(INamedObject o) {
		final String desc = o.getDescription();
		if (desc.length() <= 60) {
			return desc;
		} else {
			return desc.substring(0, 60);
		}
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		IMySQLTable table = (IMySQLTable) model;

		ISQLScript script = new SQLScript(table.getName(), table.getDescription(), "",
				ScriptType.TABLE);
		// USing the reference manager
		script.appendSQL("-- Dropping table '" + table.getName() + "'..." + NEWLINE);
		script.appendSQL("DROP TABLE " + escape(table.getName()) + ";" + NEWLINE);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(table.getType(), table.getName()), script);

		// Generating FK table dependencies (bugfix for DES-708)
		// It allows 2 "full" table drops of parent / child to be sorted
		// properly because
		// dependencies will be resolved
		GenerationHelper.addForeignKeyPreconditions(r, table);
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		// Getting our table model
		IBasicTable t = (IBasicTable) model;
		IMySQLTable mysqlTable = null;
		if (model instanceof IMySQLTable) {
			mysqlTable = (IMySQLTable) model;
		}
		// Verifying consistency
		// if (t.getColumns().size() == 0) {
		// throw new ErrorException("Table <" + t.getName()
		// + "> has no column defined, cannot generate.");
		// }
		// Initializing our column generation result
		IGenerationResult columnGeneration = generateChildren(t.getColumns(), false);
		// Getting constraint generation result (resolving dependencies)
		List<IKeyConstraint> keysList = new ArrayList<IKeyConstraint>(t.getConstraints());
		Collections.sort(keysList, NameComparator.getInstance());
		IGenerationResult keyGeneration = generateChildren(keysList, true);

		// Initializing output script
		SQLScript tableScript = new SQLScript(t.getName(), t.getDescription(), NEWLINE,
				ScriptType.TABLE);
		tableScript.appendSQL("-- Creating table '" + t.getName() + "'" + NEWLINE); //$NON-NLS-1$//$NON-NLS-2$
		tableScript.appendSQL("CREATE "); //$NON-NLS-1$
		if (t.isTemporary()) {
			tableScript.appendSQL("TEMPORARY "); //$NON-NLS-1$
		}
		tableScript.appendSQL("TABLE " + escape(t.getName())); //$NON-NLS-1$
		// Appending columns definition since these are partials
		addCommaSeparatedScripts(tableScript, " ( ", ")", columnGeneration.getAdditions());
		// Appending engine
		if (mysqlTable != null) {
			tableScript.appendSQL(" Engine=" + mysqlTable.getEngine());
			// Appending table character set
			if (mysqlTable.getCharacterSet() != null
					&& !"".equals(mysqlTable.getCharacterSet().trim())) {
				tableScript.appendSQL(" default charset=" + mysqlTable.getCharacterSet());
				// Appending table collation
				final String collation = mysqlTable.getCollation();
				if (collation != null && !"".equals(collation.trim())) {
					tableScript.appendSQL(" collate " + collation.trim());
				}
			}
			tableScript.appendSQL(NEWLINE);
		}
		// Adding table description section
		if (!isEmpty(t.getDescription())) {
			tableScript.appendSQL(" COMMENT '" + getComment(t).replace("'", "''") + "'" + NEWLINE);
		}
		// Terminating table definition
		tableScript.appendSQL(";" + NEWLINE);

		// Generating results
		IGenerationResult r = GenerationFactory.createGenerationResult(t.getName());
		r.addAdditionScript(new DatabaseReference(t.getType(), t.getName()), tableScript);
		r.integrate(keyGeneration);

		return r;
	}

}
