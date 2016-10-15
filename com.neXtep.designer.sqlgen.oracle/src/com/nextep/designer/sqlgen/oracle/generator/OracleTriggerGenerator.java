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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleTriggerGenerator extends SQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(OracleTriggerGenerator.class);

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// Regenerating from source
		return generateFullSQL(result.getSource());
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final ITrigger t = (ITrigger) model;
		final String triggerName = getName(t);
		final String rawName = t.getName();

		ISQLScript s = getSqlScript(t.getName(), t.getDescription(), ScriptType.TRIGGER);
		s.appendSQL(prompt("Dropping trigger '" + rawName + "'..."));
		s.appendSQL("DROP TRIGGER ").appendSQL(triggerName);
		closeLastStatement(s);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(t.getType(), rawName), s);
		r.addPrecondition(new DatabaseReference(t.getTriggableRef().getType(),
				((INamedObject) VersionHelper.getReferencedItem(t.getTriggableRef())).getName()));
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final ITrigger t = (ITrigger) model;
		final String triggerName = getName(t);
		final String rawName = t.getName();
		final INamedObject table = (INamedObject) VersionHelper.getReferencedItem(t
				.getTriggableRef());
		final String tableName = getName((IDatabaseObject<?>) table);

		ISQLScript s = getSqlScript(t.getName(), t.getDescription(), ScriptType.TRIGGER);
		s.appendSQL(prompt("Creating trigger '" + rawName + "' on table '" + table.getName()
				+ "'..."));
		// Creating the intro creation statement if not user-defined
		if (!t.isCustom()) {
			s.appendSQL("CREATE OR REPLACE TRIGGER ").appendSQL(triggerName).appendSQL(" ")
					.appendSQL(t.getTime().name()).appendSQL(" ");
			if (t.getEvents().size() == 0) {
				LOGGER.warn(t.getType().getName() + " " + rawName
						+ ": No events defined for this trigger, skipping.");
				return null;
			}
			boolean first = true;
			for (TriggerEvent e : t.getEvents()) {
				if (first) {
					first = false;
				} else {
					s.appendSQL(" OR ");
				}
				s.appendSQL(e.name());
			}
			s.appendSQL(" ON ").appendSQL(tableName).appendSQL(NEWLINE);
			s.appendSQL("FOR EACH ROW").appendSQL(NEWLINE);
		} else {
			s.appendSQL("CREATE OR REPLACE ");
		}
		// Appending SQL source code
		final String sql = t.getSql();
		final String convertedSql = sql.replaceFirst("(?i)" + rawName, triggerName);
		s.appendSQL(convertedSql.trim()).appendSQL(NEWLINE);
		if (!t.isCustom()) {
			s.appendSQL(NEWLINE);
		}
		closeLastStatement(s);
		s.appendSQL("SHOW ERRORS").appendSQL(NEWLINE);

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(t.getType(), rawName), s);

		return r;
	}

}
