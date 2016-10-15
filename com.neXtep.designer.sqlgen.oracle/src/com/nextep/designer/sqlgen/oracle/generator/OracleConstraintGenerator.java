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

import java.text.MessageFormat;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.ForeignKeyAction;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.sqlgen.strategies.UniqueKeyGenerateDropsStrategy;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.OracleUniqueConstraint;
import com.nextep.designer.dbgm.oracle.model.IOracleTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.PhysicalOrganisation;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.generic.GenericMessages;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleConstraintGenerator extends SQLGenerator {

	private final static Log LOGGER = LogFactory.getLog(OracleConstraintGenerator.class);

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IKeyConstraint constraint = (IKeyConstraint) model;
		final String consName = getName(constraint);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		// Initializing constraint creation script
		ISQLScript constraintScript = getSqlScript(consName, constraint.getDescription(),
				ScriptType.PKCONSTRAINT);
		try {
			constraint.checkConsistency();
		} catch (InconsistentObjectException e) {
			LOGGER.warn(MessageFormat.format(
					GenericMessages.getString("generator.constraint.inconsistentFK"), //$NON-NLS-1$
					consName, constraint.getConstrainedTable().getName(), e.getReason()));
			// Adding a warning in the SQL script
			constraintScript
					.appendSQL(prompt("Inconsistent foreign key " + consName + " has been skipped")); //$NON-NLS-1$ //$NON-NLS-2$
			genResult.addAdditionScript(new DatabaseReference(constraint.getType(), consName),
					constraintScript);
			return genResult;
		}
		constraintScript.appendSQL("Prompt Creating");

		switch (constraint.getConstraintType()) {
		case UNIQUE:
			constraintScript.appendSQL(" Unique");
			break;
		case PRIMARY:
			constraintScript.appendSQL(" Primary");
			// Checking if this is a PK for a index-organized table
			final IBasicTable t = constraint.getConstrainedTable();
			if (t instanceof IOracleTable) {
				if (((IOracleTable) t).getPhysicalProperties() != null) {
					final IOracleTablePhysicalProperties props = ((IOracleTable) t)
							.getPhysicalProperties();
					if (props.getPhysicalOrganisation() == PhysicalOrganisation.INDEX) {
						// If this is an index organized table, we do not
						// generate the PK as it
						// would
						// already be created at table creation
						return null;
					}
				}
			}
			break;
		case FOREIGN:
			ForeignKeyConstraint fk = (ForeignKeyConstraint) constraint;
			// Updating preconditions
			genResult.addPrecondition(new DatabaseReference(fk.getRemoteConstraint().getType(), fk
					.getRemoteConstraint().getName(), fk.getRemoteConstraint()
					.getConstrainedTable().getName()));
			genResult.addPrecondition(new DatabaseReference(IElementType.getInstance("TABLE"), fk
					.getRemoteConstraint().getConstrainedTable().getName()));
			// Appending script
			constraintScript.appendSQL(" Foreign");
			constraintScript.setScriptType(ScriptType.FKCONSTRAINT);
			break;
		}
		constraintScript.appendSQL(" Key constraint '" + consName + "' on table '"
				+ constraint.getConstrainedTable().getName() + "'" + NEWLINE);
		constraintScript.appendSQL("ALTER TABLE " + getName(constraint.getConstrainedTable())
				+ " ADD (" + NEWLINE);
		constraintScript.appendSQL("   CONSTRAINT " + consName);
		switch (constraint.getConstraintType()) {
		case UNIQUE:
			constraintScript.appendSQL(" UNIQUE");
			break;
		case PRIMARY:
			constraintScript.appendSQL(" PRIMARY KEY");
			break;
		case FOREIGN:
			constraintScript.appendSQL(" FOREIGN KEY");
			break;
		}
		boolean first = true;
		for (IBasicColumn col : constraint.getColumns()) {
			// TODO Maybe add a reference generator
			constraintScript.appendSQL(NEWLINE + "      " + (first ? "(" : ",")
					+ escape(col.getName()));
			first = false;
		}
		constraintScript.appendSQL(")");

		// Adding specific foreign key constraints clauses
		if (constraint.getConstraintType().equals(ConstraintType.FOREIGN)) {
			final ForeignKeyConstraint fk = (ForeignKeyConstraint) constraint;

			constraintScript.appendSQL(" REFERENCES "
					+ getName(fk.getRemoteConstraint().getConstrainedTable()));
			first = true;
			for (IBasicColumn col : fk.getRemoteConstraint().getColumns()) {
				constraintScript.appendSQL(NEWLINE + "      " + (first ? "(" : ",")
						+ escape(col.getName()));
				first = false;
			}
			constraintScript.appendSQL(")");

			if (!fk.getOnDeleteAction().equals(ForeignKeyAction.NO_ACTION)) {
				constraintScript.appendSQL(" ON DELETE " + fk.getOnDeleteAction().getSql()); //$NON-NLS-1$
			}
		}

		constraintScript.appendSQL(NEWLINE);
		switch (constraint.getConstraintType()) {
		case UNIQUE:
		case PRIMARY:
			if (constraint instanceof OracleUniqueConstraint) {
				OracleUniqueConstraint uk = (OracleUniqueConstraint) constraint;
				if (uk.getPhysicalProperties() != null) {
					IPhysicalProperties props = uk.getPhysicalProperties();
					ISQLGenerator propGenerator = getGenerator(props.getType());
					if (propGenerator != null) {
						IGenerationResult propGeneration = propGenerator.generateFullSQL(props);
						if (propGeneration != null) {
							constraintScript.appendSQL("USING INDEX ");
							Collection<ISQLScript> generatedScripts = propGeneration.getAdditions();
							for (ISQLScript physScript : generatedScripts) {
								constraintScript.appendScript(physScript);
							}
						}
					}
				}
			}

		}
		constraintScript.appendSQL(")");
		closeLastStatement(constraintScript);

		genResult.addAdditionScript(new DatabaseReference(constraint.getType(), consName,
				constraint.getConstrainedTable().getName()), constraintScript);

		return genResult;
	}

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
		final String constraintName = getName(c);
		final String tabName = getName(c.getConstrainedTable());
		final String rawName = c.getName();

		ISQLScript dropScript = new SQLScript(rawName, "Drop script", "",
				c.getConstraintType() == ConstraintType.FOREIGN ? ScriptType.FKCONSTRAINT
						: ScriptType.PKCONSTRAINT);
		dropScript.appendSQL(prompt("Dropping constraint '" + rawName + "'..."));
		dropScript.appendSQL("ALTER TABLE " + tabName + " DROP CONSTRAINT ");
		dropScript.appendSQL(constraintName);
		switch (c.getConstraintType()) {
		case PRIMARY:
		case UNIQUE:
			dropScript.appendSQL(" CASCADE DROP INDEX");
			break;
		}
		closeLastStatement(dropScript);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(c.getType(), rawName), dropScript);
		if (c.getConstraintType() == ConstraintType.FOREIGN) {
			ForeignKeyConstraint fk = (ForeignKeyConstraint) c;
			try {
				genResult.addPrecondition(new DatabaseReference(fk.getRemoteConstraint().getType(),
						fk.getRemoteConstraint().getName(), fk.getRemoteConstraint()
								.getConstrainedTable().getName()));
				genResult.addPrecondition(new DatabaseReference(IElementType.getInstance("TABLE"),
						fk.getRemoteConstraint().getConstrainedTable().getName()));
			} catch (UnresolvedItemException e) {
				LOGGER.warn(MessageFormat.format(
						GenericMessages.getString("generator.constraint.unresolvedDependencyMsg"), //$NON-NLS-1$
						fk.getName()), e);
			}
		}
		return genResult;
	}

}
