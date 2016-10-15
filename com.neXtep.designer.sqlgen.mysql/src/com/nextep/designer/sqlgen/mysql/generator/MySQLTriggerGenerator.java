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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class MySQLTriggerGenerator extends SQLGenerator {

	private static final Log log = LogFactory.getLog(MySQLTriggerGenerator.class);

	/**
	 * @see com.nextep.datadesigner.sqlgen.impl.SQLGenerator#generateDiff(com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// Dropping the target
		IGenerationResult r = doDrop(result.getTarget());
		// Regenerating from source
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#doDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult doDrop(Object model) {
		ITrigger t = (ITrigger) model;

		ISQLScript s = new SQLScript(t.getName(), t.getDescription(), "", ScriptType.TRIGGER);
		s.appendSQL("-- Dropping trigger '" + t.getName() + "'..." + NEWLINE);
		s.appendSQL("DROP TRIGGER " + escape(t.getName()) + ";" + NEWLINE);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(t.getType(), t.getName()), s);

		return r;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.ISQLGenerator#generateFullSQL(java.lang.Object)
	 */
	@Override
	public IGenerationResult generateFullSQL(Object model) {
		ITrigger t = (ITrigger) model;
		INamedObject table = (INamedObject) VersionHelper.getReferencedItem(t.getTriggableRef());

		ISQLScript s = new SQLScript(t.getName(), t.getDescription(), "", ScriptType.TRIGGER);
		s.appendSQL("-- Creating trigger '" + t.getName() + "' on table '" + table.getName()
				+ "'..." + NEWLINE);
		// Changing delimiter
		s.appendSQL("DELIMITER |;" + NEWLINE);
		// Creating the intro creation statement if not user-defined
		if (!t.isCustom()) {
			s.appendSQL("CREATE TRIGGER " + escape(t.getName()) + " " + t.getTime().name() + " ");
			if (t.getEvents().size() > 1) {
				log.warn(t.getType().getName()
						+ " "
						+ t.getName()
						+ ": MySQL does not support multi-events trigger. You must create as many triggers as events. Only the first event will be generated");
			} else if (t.getEvents().size() == 0) {
				log.warn(t.getType().getName() + " " + t.getName()
						+ ": No events defined for this trigger, skipping.");
				return null;
			}
			s.appendSQL(t.getEvents().iterator().next().name() + " ON " + escape(table.getName())
					+ NEWLINE);
			s.appendSQL("FOR EACH ROW" + NEWLINE);
		} else {
			// Appending SQL source code
			if (!t.getSql().toUpperCase().trim().startsWith("CREATE")) {
				s.appendSQL("CREATE ");
			}
		}
		s.appendSQL(t.getSql() + NEWLINE);
		if (!t.isCustom()) {
			s.appendSQL(NEWLINE);
		}
		s.appendSQL("|;" + NEWLINE);

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(t.getType(), t.getName()), s);

		return r;
	}

}
