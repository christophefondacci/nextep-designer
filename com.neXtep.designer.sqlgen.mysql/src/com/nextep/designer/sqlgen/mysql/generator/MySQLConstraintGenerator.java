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

import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseRawObject;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.mysql.model.IMySQLColumn;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.generic.GenericMessages;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.mysql.strategies.MySqlUniqueKeyGenerateDropsStrategy;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MySQLConstraintGenerator extends MySQLGenerator {

	private final static Log LOGGER = LogFactory.getLog(MySQLConstraintGenerator.class);

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		IKeyConstraint c = (IKeyConstraint) result.getTarget();
		// Dropping the target
		IGenerationResult r;
		switch (c.getConstraintType()) {
		case FOREIGN:
			r = doDrop(result.getTarget());
			break;
		default:
			r = new MySqlUniqueKeyGenerateDropsStrategy().generateDrop(this, result.getTarget(),
					getVendor());

		}
		// Regenerating from source
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final IKeyConstraint constraint = (IKeyConstraint) model;
		final String consName = constraint.getName();

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		ISQLScript dropScript = getSqlScript(consName, constraint.getDescription(),
				ScriptType.PKCONSTRAINT);

		// De-activating any auto-incremented column which would be enforced by a dropped UK
		switch (constraint.getConstraintType()) {
		case UNIQUE:
		case PRIMARY:
			/*
			 * We need to check each column of the unique key for the AUTO_INCREMENT attribute if it
			 * is a composite key, as it is permitted to specify this attribute on a secondary
			 * column.
			 */
			for (IBasicColumn col : constraint.getColumns()) {
				/*
				 * We need to check if we are in a MySQL context, i.e. if the columns referenced by
				 * the primary key constraint are instances of IMySQLColumn. This would not be the
				 * case if this DROP script is generated from a vendor-neutral workspace.
				 */
				if (col instanceof IMySQLColumn) {
					IMySQLColumn mysqlCol = (IMySQLColumn) col;
					if (mysqlCol.isAutoIncremented()) {
						String alterAutoInc = buildAutoIncrementAlter(mysqlCol, false);
						dropScript.appendSQL(alterAutoInc);
						break; // There can be only one AUTO_INCREMENT column per table
					}
				} else {
					// No need to check for the other columns if we are not in a MySQL context
					break;
				}
			}
			break;
		}

		dropScript.appendSQL(prompt("Dropping constraint '" + consName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("ALTER TABLE ") //$NON-NLS-1$
				.appendSQL(escape(constraint.getConstrainedTable().getName())).appendSQL(" DROP "); //$NON-NLS-1$

		switch (constraint.getConstraintType()) {
		case UNIQUE:
			dropScript.appendSQL("INDEX ").appendSQL(escape(consName)); //$NON-NLS-1$
			dropScript.setScriptType(ScriptType.INDEX);
			break;
		case PRIMARY:
			dropScript.appendSQL("PRIMARY KEY"); //$NON-NLS-1$
			dropScript.setScriptType(ScriptType.PKCONSTRAINT);
			break;
		case FOREIGN:
			dropScript.appendSQL("FOREIGN KEY ").appendSQL(escape(consName)); //$NON-NLS-1$
			dropScript.setScriptType(ScriptType.FKCONSTRAINT);
			final ForeignKeyConstraint fk = (ForeignKeyConstraint) constraint;
			try {
				genResult.addPrecondition(new DatabaseReference(fk.getRemoteConstraint().getType(),
						fk.getRemoteConstraint().getName(), fk.getRemoteConstraint()
								.getConstrainedTable().getName()));
				genResult.addPrecondition(new DatabaseReference(IElementType
						.getInstance(IBasicTable.TYPE_ID), fk.getRemoteConstraint()
						.getConstrainedTable().getName()));
			} catch (UnresolvedItemException e) {
				LOGGER.warn(MessageFormat.format(
						GenericMessages.getString("generator.constraint.unresolvedDependencyMsg"), //$NON-NLS-1$
						fk.getName()), e);
			}
			for (IDatabaseRawObject dbObj : fk.getEnforcingIndex()) {
				if (dbObj.getType() == IElementType.getInstance(IIndex.INDEX_TYPE)) {
					genResult.addPrecondition(new DatabaseReference(dbObj.getType(), dbObj
							.getName()));
				} else {
					genResult.addPrecondition(new DatabaseReference(dbObj.getType(), dbObj
							.getName(), fk.getConstrainedTable().getName()));
				}
			}
			break;
		}
		closeLastStatement(dropScript);

		genResult.addDropScript(new DatabaseReference(constraint.getType(), consName, constraint
				.getConstrainedTable().getName()), dropScript);
		return genResult;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IKeyConstraint constraint = (IKeyConstraint) model;
		final String consName = constraint.getName();
		final String tableName = constraint.getConstrainedTable().getName();

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		try {
			constraint.checkConsistency();
		} catch (InconsistentObjectException e) {
			throw new ErrorException("Foreign key <" + consName + "> of table <" + tableName
					+ "> appear to be inconsistent. Fix it before generating. Reason: "
					+ e.getReason());
		}

		// Initializing constraint creation script
		ISQLScript constraintScript = getSqlScript(consName, constraint.getDescription(),
				ScriptType.PKCONSTRAINT);
		// constraintScript.appendSQL(NEWLINE);

		String msg = "Creating "; //$NON-NLS-1$
		switch (constraint.getConstraintType()) {
		case UNIQUE:
			msg += "Unique"; //$NON-NLS-1$
			break;
		case PRIMARY:
			msg += "Primary"; //$NON-NLS-1$
			break;
		case FOREIGN:
			ForeignKeyConstraint fk = (ForeignKeyConstraint) constraint;
			// Updating preconditions
			genResult.addPrecondition(new DatabaseReference(fk.getRemoteConstraint().getType(), fk
					.getRemoteConstraint().getName(), fk.getRemoteConstraint()
					.getConstrainedTable().getName()));
			genResult.addPrecondition(new DatabaseReference(IElementType.getInstance("TABLE"), fk //$NON-NLS-1$
					.getRemoteConstraint().getConstrainedTable().getName()));
			// Appending script
			msg += "Foreign"; //$NON-NLS-1$
			constraintScript.setScriptType(ScriptType.FKCONSTRAINT);
			break;
		}
		constraintScript.appendSQL(prompt(msg + " Key constraint '" + consName + "' on table '" //$NON-NLS-1$ //$NON-NLS-2$
				+ tableName + "'...")); //$NON-NLS-1$
		constraintScript.appendSQL("ALTER TABLE ").appendSQL(escape(tableName)).appendSQL(" ADD ") //$NON-NLS-1$ //$NON-NLS-2$
				.appendSQL(NEWLINE);
		constraintScript.appendSQL("   CONSTRAINT"); //$NON-NLS-1$

		switch (constraint.getConstraintType()) {
		case UNIQUE:
			constraintScript.appendSQL(" UNIQUE ").appendSQL(escape(consName)); //$NON-NLS-1$
			break;
		case PRIMARY:
			constraintScript.appendSQL(" PRIMARY KEY"); //$NON-NLS-1$
			break;
		case FOREIGN:
			constraintScript.appendSQL(" ").appendSQL(escape(consName)).appendSQL(" FOREIGN KEY ") //$NON-NLS-1$ //$NON-NLS-2$
					.appendSQL(escape(consName));
			break;
		}

		boolean first = true;
		for (IBasicColumn col : constraint.getColumns()) {
			// TODO Maybe add a reference generator
			constraintScript.appendSQL(NEWLINE).appendSQL("      ").appendSQL((first ? "(" : ",")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					.appendSQL(escape(col.getName()));
			first = false;
		}
		constraintScript.appendSQL(")"); //$NON-NLS-1$

		// Adding specific foreign key constraints clauses
		if (constraint.getConstraintType().equals(ConstraintType.FOREIGN)) {
			final ForeignKeyConstraint fk = (ForeignKeyConstraint) constraint;

			constraintScript.appendSQL(" REFERENCES ").appendSQL( //$NON-NLS-1$
					escape(fk.getRemoteConstraint().getConstrainedTable().getName()));
			first = true;
			for (IBasicColumn col : fk.getRemoteConstraint().getColumns()) {
				constraintScript.appendSQL(NEWLINE).appendSQL("      ") //$NON-NLS-1$
						.appendSQL((first ? "(" : ",")).appendSQL(escape(col.getName())); //$NON-NLS-1$ //$NON-NLS-2$
				first = false;
			}
			constraintScript.appendSQL(")"); //$NON-NLS-1$

			/*
			 * For InnoDB storage engine, if ON DELETE or ON UPDATE are not specified, the default
			 * action is RESTRICT. Therefore we always generate these clauses so there is no
			 * ambiguity for their value.
			 */
			constraintScript.appendSQL(" ON UPDATE " + fk.getOnUpdateAction().getSql()); //$NON-NLS-1$
			constraintScript.appendSQL(" ON DELETE " + fk.getOnDeleteAction().getSql()); //$NON-NLS-1$
		}

		closeLastStatement(constraintScript);

		// Special AUTO_INCREMENT ALTER (since we need the primary key before setting an
		// AUTO_INCREMENT column)
		if (constraint.getConstraintType() == ConstraintType.PRIMARY
				|| constraint.getConstraintType() == ConstraintType.UNIQUE) {
			for (IBasicColumn col : constraint.getColumns()) {
				if (col instanceof IMySQLColumn) {
					IMySQLColumn myCol = (IMySQLColumn) col;
					if (myCol.isAutoIncremented()) {
						String autoIncrementSql = buildAutoIncrementAlter(myCol, true);
						constraintScript.appendSQL(autoIncrementSql);
						break;
					}
				}
			}
		}

		genResult.addAdditionScript(
				new DatabaseReference(constraint.getType(), consName, tableName), constraintScript);

		return genResult;
	}

	private String buildAutoIncrementAlter(IBasicColumn myCol, boolean autoIncremented) {
		ISQLScript alterAutoIncrementScript = getSqlScript(myCol.getName(), myCol.getDescription(),
				ScriptType.CUSTOM);

		alterAutoIncrementScript
				.appendSQL(prompt((autoIncremented ? "Activating" : "De-activating") //$NON-NLS-1$ //$NON-NLS-2$
						+ " AUTO_INCREMENT attribute...")); //$NON-NLS-1$
		alterAutoIncrementScript.appendSQL("ALTER TABLE ") //$NON-NLS-1$
				.appendSQL(escape(myCol.getParent().getName())).appendSQL(" MODIFY "); //$NON-NLS-1$

		// Appending column definition script
		ISQLScript colScript = getGenerator(IElementType.getInstance(IBasicColumn.TYPE_ID))
				.generateFullSQL(myCol).getAdditions().iterator().next();
		alterAutoIncrementScript.appendScript(colScript);

		// Adding AUTO_INCREMENT attribute if specified
		if (autoIncremented) {
			alterAutoIncrementScript.appendSQL(" AUTO_INCREMENT"); //$NON-NLS-1$
		}
		closeLastStatement(alterAutoIncrementScript);

		return alterAutoIncrementScript.getSql();
	}

}
