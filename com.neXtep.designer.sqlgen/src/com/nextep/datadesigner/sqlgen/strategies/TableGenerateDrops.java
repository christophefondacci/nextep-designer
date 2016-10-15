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
package com.nextep.datadesigner.sqlgen.strategies;

import java.util.Collection;
import java.util.Collections;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.impl.GenerationResult;

/**
 * @author Christophe Fondacci
 *
 */
public class TableGenerateDrops extends DoDropStrategy {

	public TableGenerateDrops() {
		setName("Drop dependent objects");
		setDescription("The generator will generate drops of all known dependent objects");
	}
	
	/**
	 * @see com.nextep.datadesigner.sqlgen.strategies.DoDropStrategy#getId()
	 */
	@Override
	public String getId() {
		return TableGenerateDrops.class.getName();
	}
	
	/**
	 * @see com.nextep.datadesigner.sqlgen.strategies.DoDropStrategy#generateDrop(com.nextep.datadesigner.sqlgen.model.ISQLGenerator, java.lang.Object)
	 */
	@Override
	public IGenerationResult generateDrop(ISQLGenerator generator,
			Object modelToDrop) {
		IBasicTable table = (IBasicTable)modelToDrop;
		
		IGenerationResult result = new GenerationResult(table.getName());
		// USing the reference manager
		Collection<IReferencer> dependencies = null;
		try {
			dependencies = CorePlugin.getService(IReferenceManager.class).getReverseDependencies(table);
		} catch( ErrorException e) {
			dependencies = Collections.emptyList();
		}
		for(IReferencer r : dependencies) {
			// On dependent objects
			if(r instanceof ITypedObject) {
				final ITypedObject typedObj = (ITypedObject)r;
				if(typedObj.getType()==IElementType.getInstance(ForeignKeyConstraint.TYPE_ID)) {
					ISQLGenerator g = getGenerator(typedObj.getType());
					// If we know how to drop, we drop it
					if(g!=null) {
						result.integrate(g.generateDrop(r));
					}
				}
			}
		}
		// Eventually dropping our table
		result.integrate(generator.doDrop(table));
		// Returning our result
		return result;
	}
}
