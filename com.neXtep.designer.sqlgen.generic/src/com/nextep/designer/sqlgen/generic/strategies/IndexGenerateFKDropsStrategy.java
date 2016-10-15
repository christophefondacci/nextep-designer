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
package com.nextep.designer.sqlgen.generic.strategies;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.strategies.DoDropStrategy;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;

public class IndexGenerateFKDropsStrategy extends DoDropStrategy {

	private static final Log log = LogFactory.getLog(IndexGenerateFKDropsStrategy.class);

	public IndexGenerateFKDropsStrategy() {
		setName("Drop dependent foreign keys");
		setDescription("When dropping an index, this strategy will drop any foreign key enforced by the dropped index");
	}

	@Override
	public IGenerationResult generateDrop(ISQLGenerator generator, Object modelToDrop) {
		IIndex index = (IIndex) modelToDrop;
		IGenerationResult result = GenerationFactory.createGenerationResult();
		Collection<ForeignKeyConstraint> enforcedFkeys = DBGMHelper.getForeignKeysForIndex(index);
		for (ForeignKeyConstraint fk : enforcedFkeys) {
			// Generating foreign keys drops
			ISQLGenerator fkGenerator = getGenerator(IElementType.getInstance("FOREIGN_KEY"));
			// Dropping
			result.integrate(fkGenerator.generateDrop(fk));
		}
		// Integrating standard drop
		result.integrate(super.generateDrop(generator, modelToDrop));
		return result;
	}
}
