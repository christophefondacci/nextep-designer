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

import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

public class OracleCheckConstraintGenerator extends SQLGenerator {

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		IGenerationResult r = doDrop(result.getTarget());
		r.integrate(generateFullSQL(result.getSource()));
		return r;
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		final ICheckConstraint c = (ICheckConstraint) model;
		ISQLScript s = new SQLScript(c.getName(), "", "", ScriptType.CHECKCONS);
		s.appendSQL("Prompt Dropping check constraint '" + c.getName() + "' on table '"
				+ c.getConstrainedTable().getName() + "'..." + NEWLINE);
		s.appendSQL("ALTER TABLE " + getName(c.getConstrainedTable()) + " DROP CONSTRAINT "
				+ getName(c) + NEWLINE + "/" + NEWLINE);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(new DatabaseReference(c.getType(), c.getName()), s);
		r.addPrecondition(new DatabaseReference(c.getConstrainedTable().getType(), c
				.getConstrainedTable().getName()));
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final ICheckConstraint c = (ICheckConstraint) model;
		ISQLScript s = new SQLScript(c.getName(), "", "", ScriptType.CHECKCONS);
		s.appendSQL("Prompt Creating check constraint '" + c.getName() + "' on table '"
				+ c.getConstrainedTable().getName() + "'..." + NEWLINE);
		s.appendSQL("ALTER TABLE " + getName(c.getConstrainedTable()) + " ADD CONSTRAINT "
				+ getName(c) + " CHECK (");
		s.appendSQL(c.getCondition() + ")" + NEWLINE + "/" + NEWLINE);

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(c.getType(), c.getName()), s);
		r.addPrecondition(new DatabaseReference(c.getConstrainedTable().getType(), c
				.getConstrainedTable().getName()));
		return r;
	}

}
