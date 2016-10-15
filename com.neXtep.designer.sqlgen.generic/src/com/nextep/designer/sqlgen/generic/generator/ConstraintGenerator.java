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

import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.ForeignKeyAction;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseRawObject;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.generic.GenericMessages;
import com.nextep.designer.sqlgen.generic.strategies.UniqueKeyGenerateDropsStrategy;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class ConstraintGenerator extends SQLGenerator {

	private final static Log LOGGER = LogFactory.getLog(ConstraintGenerator.class);

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
			r = new UniqueKeyGenerateDropsStrategy().generateDrop(this, result.getTarget(),
					getVendor());

		}
		// Regenerating from source
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		IKeyConstraint c = (IKeyConstraint) model;
		IGenerationResult r = GenerationFactory.createGenerationResult();
		ISQLScript dropScript = new SQLScript(c.getName(),
				"Drop script", "", ScriptType.PKCONSTRAINT); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL(prompt("Dropping constraint '" + c.getName() + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("ALTER TABLE " + escape(c.getConstrainedTable().getName()) //$NON-NLS-1$
				+ " DROP CONSTRAINT " + escape(c.getName())); //$NON-NLS-1$
		switch (c.getConstraintType()) {
		case FOREIGN:
			dropScript.setScriptType(ScriptType.FKCONSTRAINT);
			ForeignKeyConstraint fk = (ForeignKeyConstraint) c;
			try {
				r.addPrecondition(new DatabaseReference(fk.getRemoteConstraint().getType(), fk
						.getRemoteConstraint().getName(), fk.getRemoteConstraint()
						.getConstrainedTable().getName()));
				r.addPrecondition(new DatabaseReference(IElementType
						.getInstance(IBasicTable.TYPE_ID), fk.getRemoteConstraint()
						.getConstrainedTable().getName()));
			} catch (UnresolvedItemException e) {
				LOGGER.warn(MessageFormat.format(
						GenericMessages.getString("generator.constraint.unresolvedDependencyMsg"), //$NON-NLS-1$
						fk.getName()), e);
			}
			for (IDatabaseRawObject dbObj : fk.getEnforcingIndex()) {
				r.addPrecondition(new DatabaseReference(dbObj.getType(), dbObj.getName()));
			}
			break;
		}
		dropScript.appendSQL(getSQLCommandWriter().closeStatement());

		r.addDropScript(new DatabaseReference(c.getType(), c.getName(), c.getConstrainedTable()
				.getName()), dropScript);
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		// Getting the constraint model
		IKeyConstraint c = (IKeyConstraint) model;
		// Initializing our result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		// Initializing constraint creation script
		ISQLScript constraintScript = getSqlScript(c.getName(), c.getDescription(),
				ScriptType.PKCONSTRAINT);

		try {
			c.checkConsistency();
		} catch (InconsistentObjectException e) {
			LOGGER.warn(MessageFormat.format(
					GenericMessages.getString("generator.constraint.inconsistentFK"), //$NON-NLS-1$
					c.getName(), c.getConstrainedTable().getName(), e.getReason()));
			// Adding a warning in the SQL script
			constraintScript
					.appendSQL(prompt("Inconsistent foreign key " + c.getName() + " has been skipped")); //$NON-NLS-1$ //$NON-NLS-2$
			genResult.addAdditionScript(new DatabaseReference(c.getType(), c.getName()),
					constraintScript);
			return genResult;
		}
		final StringBuffer commentBuf = new StringBuffer(50);
		commentBuf.append("Creating"); //$NON-NLS-1$

		switch (c.getConstraintType()) {
		case UNIQUE:
			commentBuf.append(" Unique"); //$NON-NLS-1$
			break;
		case PRIMARY:
			commentBuf.append(" Primary"); //$NON-NLS-1$
			break;
		case FOREIGN:
			ForeignKeyConstraint fk = (ForeignKeyConstraint) c;
			// Updating preconditions
			genResult.addPrecondition(new DatabaseReference(fk.getRemoteConstraint().getType(), fk
					.getRemoteConstraint().getName(), fk.getRemoteConstraint()
					.getConstrainedTable().getName()));
			genResult.addPrecondition(new DatabaseReference(IElementType.getInstance("TABLE"), fk //$NON-NLS-1$
					.getRemoteConstraint().getConstrainedTable().getName()));
			// Appending script
			commentBuf.append(" Foreign"); //$NON-NLS-1$
			constraintScript.setScriptType(ScriptType.FKCONSTRAINT);
			break;
		}
		constraintScript.appendSQL(prompt(commentBuf.toString() + " Key constraint '" + c.getName() //$NON-NLS-1$
				+ "' on table '" + c.getConstrainedTable().getName() + "'")); //$NON-NLS-1$ //$NON-NLS-2$
		constraintScript
				.appendSQL("ALTER TABLE " + escape(c.getConstrainedTable().getName()) + " ADD "); //$NON-NLS-1$ //$NON-NLS-2$
		// Hook for constraint name definition BEFORE constraint type
		constraintScript.appendSQL(getConstraintNameBefore(c) + NEWLINE);
		// Constraint type
		switch (c.getConstraintType()) {
		case UNIQUE:
			constraintScript.appendSQL(" UNIQUE "); //$NON-NLS-1$
			break;
		case PRIMARY:
			constraintScript.appendSQL(" PRIMARY KEY "); //$NON-NLS-1$
			break;
		case FOREIGN:
			constraintScript.appendSQL(" FOREIGN KEY "); //$NON-NLS-1$
			break;
		}
		boolean first = true;
		for (IBasicColumn col : c.getColumns()) {
			constraintScript.appendSQL(NEWLINE
					+ "      " + (first ? "(" : ",") + escape(col.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			first = false;
		}
		switch (c.getConstraintType()) {
		case FOREIGN:
			ForeignKeyConstraint fk = (ForeignKeyConstraint) c;
			constraintScript.appendSQL(") REFERENCES " //$NON-NLS-1$
					+ escape(fk.getRemoteConstraint().getConstrainedTable().getName()));
			first = true;
			for (IBasicColumn col : fk.getRemoteConstraint().getColumns()) {
				constraintScript.appendSQL(NEWLINE
						+ "      " + (first ? "(" : ",") + escape(col.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				first = false;
			}
			break;
		}

		constraintScript.appendSQL(")"); //$NON-NLS-1$
		if (c.getConstraintType() == ConstraintType.FOREIGN) {
			final ForeignKeyConstraint fk = (ForeignKeyConstraint) c;
			if (fk.getOnUpdateAction() != ForeignKeyAction.NO_ACTION) {
				constraintScript.appendSQL(" ON UPDATE " + fk.getOnUpdateAction().getSql()); //$NON-NLS-1$
			}
			if (fk.getOnDeleteAction() != ForeignKeyAction.NO_ACTION) {
				constraintScript.appendSQL(" ON DELETE " + fk.getOnDeleteAction().getSql()); //$NON-NLS-1$
			}
		}

		// Hook for constraint name definition AFTER constraint type and column definition
		constraintScript.appendSQL(getConstraintNameAfter(c));
		constraintScript.appendSQL(NEWLINE).appendSQL(getSQLCommandWriter().closeStatement());

		genResult.addAdditionScript(new DatabaseReference(c.getType(), c.getName(), c
				.getConstrainedTable().getName()), constraintScript);

		// Returning our script
		return genResult;
	}

	protected String getConstraintNameBefore(IKeyConstraint c) {
		return ""; //$NON-NLS-1$
	}

	protected String getConstraintNameAfter(IKeyConstraint c) {
		return ""; //$NON-NLS-1$
	}

}
