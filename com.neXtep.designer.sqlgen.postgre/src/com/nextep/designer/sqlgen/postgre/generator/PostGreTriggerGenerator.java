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
package com.nextep.designer.sqlgen.postgre.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.generic.generator.AbstractSQLGenerator;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

public class PostGreTriggerGenerator extends AbstractSQLGenerator {

	private final static Log log = LogFactory.getLog(PostGreTriggerGenerator.class);

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// Dropping the target
		IGenerationResult r = doDrop(result.getTarget());
		// Regenerating from source
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final ITrigger t = (ITrigger) model;
		final IReference ref = t.getTriggableRef();
		final INamedObject namedParent = (INamedObject) VersionHelper.getReferencedItem(ref);

		ISQLScript s = new SQLScript(t.getName(), t.getDescription(), "", ScriptType.TRIGGER);
		s.appendSQL("-- Dropping trigger '" + t.getName() + "'..." + NEWLINE);
		s.appendSQL("DROP TRIGGER " + getName(t) + " ON " + namedParent.getName() + " CASCADE;"
				+ NEWLINE);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(t.getType(), t.getName()), s);

		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		ITrigger t = (ITrigger) model;
		INamedObject table = (INamedObject) VersionHelper.getReferencedItem(t.getTriggableRef());

		ISQLScript s = new SQLScript(t.getName(), t.getDescription(), "", ScriptType.TRIGGER);
		s.appendSQL(prompt(" Creating trigger '" + t.getName() + "' on table '" + table.getName()
				+ "'..."));
		// Creating the intro creation statement if not user-defined
		if (!t.isCustom()) {
			s.appendSQL("CREATE TRIGGER " + t.getName() + NEWLINE + "  " + t.getTime().name() + " ");
			if (t.getEvents().size() == 0) {
				log.warn(t.getType().getName() + " " + t.getName()
						+ ": No events defined for this trigger, skipping.");
				return null;
			}
			String separator = "";
			for (TriggerEvent e : t.getEvents()) {
				s.appendSQL(separator + e.name());
				separator = " OR ";
			}
			s.appendSQL(" ON " + table.getName() + NEWLINE);
			s.appendSQL("  FOR EACH ROW" + NEWLINE);
		}
		// Appending SQL source code
		s.appendSQL(t.getSql() + NEWLINE);
		if (!t.isCustom()) {
			s.appendSQL(getParser().getStatementDelimiter() + NEWLINE);
		}

		// Building result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(t.getType(), t.getName()), s);

		return r;
	}

}
