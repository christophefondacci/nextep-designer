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
package com.nextep.designer.sqlgen.mysql.strategies;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.sqlgen.strategies.DoDropStrategy;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * @author Christophe Fondacci
 */
public class ColumnNoDropStrategy extends DoDropStrategy implements IDropStrategy {

	/**
	 *
	 */
	public ColumnNoDropStrategy() {
		setName("Keep column, allow null values");
		setDescription("Generator will leave the column and optionnaly remove the NOT NULL condition");
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.strategies.NoDropStrategy#getId()
	 */
	@Override
	public String getId() {
		return ColumnNoDropStrategy.class.getName();
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.strategies.NoDropStrategy#generateDrop(java.lang.Object)
	 */
	@Override
	public IGenerationResult generateDrop(ISQLGenerator generator, Object modelToDrop) {
		IBasicColumn c = (IBasicColumn) modelToDrop;

		if (c.isNotNull()) {
			ISQLScript s = new SQLScript(c.getName(), "Column no drop strategy", "",
					ScriptType.DROP);
			s.appendSQL("-- Non-dropping column, removing NOT NULL condition" + NEWLINE);
			s.appendSQL("ALTER TABLE " + c.getParent().getName() + " MODIFY ");
			c.setNotNull(false);
			s.appendScript(generator.generateFullSQL(c).getAdditions().iterator().next());
			s.appendSQL(" NULL " + NEWLINE + ";" + NEWLINE);

			IGenerationResult r = GenerationFactory.createGenerationResult();
			r.addDropScript(
					new DatabaseReference(c.getType(), c.getParent().getName() + "." + c.getName()),
					s);
			// We keep the column as we received it
			c.setNotNull(true);
			return r;
		}
		return null;
	}
}
