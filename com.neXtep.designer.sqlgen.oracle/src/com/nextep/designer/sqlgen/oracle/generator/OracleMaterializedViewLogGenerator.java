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

import java.util.Collection;

import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLogPhysicalProperties;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

public class OracleMaterializedViewLogGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		// Dropping / recreating
		// TODO implement diff on materialized view log physical properties
		// properly
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.integrate(doDrop(result.getTarget()));
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		IMaterializedViewLog log = (IMaterializedViewLog) model;

		ISQLScript s = new SQLScript(log.getName(), null, "", ScriptType.MVIEW_LOG);
		s.appendSQL("Prompt Dropping materialized view log on '" + getName(log.getTable()) + "'..."
				+ NEWLINE);
		s.appendSQL("DROP MATERIALIZED VIEW LOG ON " + getName(log.getTable()) + NEWLINE + "/"
				+ NEWLINE);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(log.getType(), log.getTable().getName()), s);
		r.addPrecondition(new DatabaseReference(log.getTable().getType(), log.getTable().getName()));
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		IMaterializedViewLog log = (IMaterializedViewLog) model;

		ISQLScript s = new SQLScript(log.getName(), null, "", ScriptType.MVIEW_LOG);
		s.appendSQL("Prompt Creating materialized view log on '" + log.getTable().getName()
				+ "'..." + NEWLINE);
		s.appendSQL("CREATE MATERIALIZED VIEW LOG ON " + getName(log.getTable()));
		if (log.getPhysicalProperties() != null) {
			IMaterializedViewLogPhysicalProperties props = (IMaterializedViewLogPhysicalProperties) log
					.getPhysicalProperties();
			ISQLGenerator propGenerator = getGenerator(props.getType());
			IGenerationResult propGeneration = propGenerator.generateFullSQL(props);
			if (propGeneration != null) {
				s.appendSQL(NEWLINE);
				Collection<ISQLScript> generatedScripts = propGeneration.getAdditions();
				for (ISQLScript propScript : generatedScripts) {
					s.appendScript(propScript);
				}
			}
		}
		String prefix = " WITH";
		if (log.isPrimaryKey()) {
			s.appendSQL(prefix + " PRIMARY KEY");
			prefix = ",";
		}
		if (log.isRowId()) {
			s.appendSQL(prefix + " ROWID");
			prefix = ",";
		}
		if (log.isSequence()) {
			s.appendSQL(prefix + " SEQUENCE");
			prefix = ",";
		}
		s.appendSQL((log.isIncludingNewValues() ? " INCLUDING" : " EXCLUDING") + " NEW VALUES");
		s.appendSQL(NEWLINE + "/" + NEWLINE);
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(log.getType(), log.getName()), s);
		r.addPrecondition(new DatabaseReference(log.getTable().getType(), log.getTable().getName()));
		return r;
	}

}
