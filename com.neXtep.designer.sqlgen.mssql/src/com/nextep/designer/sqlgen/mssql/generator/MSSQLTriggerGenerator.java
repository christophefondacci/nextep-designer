/**
 * Copyright (c) 2011 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.designer.sqlgen.mssql.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.dbgm.model.TriggerTime;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.helpers.GenerationHelper;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Bruno Gautier
 */
public class MSSQLTriggerGenerator extends SQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(MSSQLTriggerGenerator.class);

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final ITrigger trigger = (ITrigger) model;
		final String triggerName = trigger.getName();
		final TriggerTime time = trigger.getTime();

		final String tableName = ((INamedObject) VersionHelper.getReferencedItem(trigger
				.getTriggableRef())).getName();

		ISQLScript createScript = getSqlScript(triggerName, trigger.getDescription(),
				ScriptType.TRIGGER);
		createScript.appendSQL(prompt("Creating trigger '" + triggerName + "' on table '" //$NON-NLS-1$ //$NON-NLS-2$
				+ tableName + "'...")); //$NON-NLS-1$

		// Creating the "CREATE...AS" part of the statement if not user-defined
		if (!trigger.isCustom()) {
			// Getting trigger time
			String timeName = time.name();

			// We check if the specified trigger time is supported
			if (time.equals(TriggerTime.BEFORE)) {
				LOGGER.warn("SQL Server does not support BEFORE triggers. " //$NON-NLS-1$
						+ "Please choose a different trigger time. " //$NON-NLS-1$
						+ "Skipping generation."); //$NON-NLS-1$
				return null;
			} else if (time.equals(TriggerTime.INSTEAD)) {
				timeName += " OF"; //$NON-NLS-1$
			}

			// Building trigger SQL
			createScript.appendSQL("CREATE TRIGGER ").appendSQL(escape(triggerName)) //$NON-NLS-1$
					.appendSQL(" ON ").appendSQL(escape(tableName)).appendSQL(NEWLINE) //$NON-NLS-1$
					.appendSQL(timeName);
			String separator = " "; //$NON-NLS-1$
			for (TriggerEvent event : trigger.getEvents()) {
				createScript.appendSQL(separator).appendSQL(event.name());
				separator = ", "; //$NON-NLS-1$
			}
			createScript.appendSQL(" AS").appendSQL(NEWLINE); //$NON-NLS-1$
		} else {
			createScript.appendSQL("CREATE "); //$NON-NLS-1$
		}

		// Appending SQL source code
		createScript.appendSQL(GenerationHelper.removeLastStatementDelimiter(trigger.getSql(),
				getParser().getStatementDelimiter()));
		closeLastStatement(createScript);
		// createScript.appendSQL(NEWLINE);

		// Building result
		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addAdditionScript(new DatabaseReference(trigger.getType(), triggerName),
				createScript);

		return genResult;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final ITrigger trigger = (ITrigger) model;
		final String triggerName = trigger.getName();

		ISQLScript dropScript = getSqlScript(triggerName, trigger.getDescription(),
				ScriptType.TRIGGER);
		dropScript.appendSQL(prompt("Dropping trigger '" + triggerName + "'...")); //$NON-NLS-1$ //$NON-NLS-2$
		dropScript.appendSQL("DROP TRIGGER ").appendSQL(escape(triggerName)); //$NON-NLS-1$
		closeLastStatement(dropScript);

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addDropScript(new DatabaseReference(trigger.getType(), triggerName), dropScript);

		return genResult;
	}

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// We drop the old trigger
		IGenerationResult genResult = doDrop(result.getTarget());
		// We generate the new trigger
		genResult.integrate(generateFullSQL(result.getSource()));
		return genResult;
	}

}
