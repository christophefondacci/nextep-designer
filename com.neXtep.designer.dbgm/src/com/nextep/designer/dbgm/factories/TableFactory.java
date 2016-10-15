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
/**
 *
 */
package com.nextep.designer.dbgm.factories;

import java.util.HashMap;
import java.util.Map;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.VersionedTable;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class TableFactory extends VersionableFactory {
	private static TableFactory instance = null;
	public TableFactory() {}
	public static VersionableFactory getInstance() {
		if(instance == null) {
			instance = new TableFactory();
		}
		return instance;
	}
	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#copyOf(com.nextep.designer.vcs.model.IVersionable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		IBasicTable copy = (IBasicTable)destination.getVersionnedObject().getModel();
		//Copying specific table attributes
		Map<String,IBasicColumn> columnNames = new HashMap<String,IBasicColumn>();
		IBasicTable sourceTable = (IBasicTable)source.getVersionnedObject().getModel();
		copy.setShortName(sourceTable.getShortName());
		for(IBasicColumn c : sourceTable.getColumns()) {
			// Implicitly add the column to the table (former copy bug)
			IBasicColumn copiedColumn = FactoryUtil.copyColumn(copy,c);
			copy.addColumn(copiedColumn);
			columnNames.put(copiedColumn.getName(), copiedColumn);
		}
		// Copying index referencies
		for(IIndex index : sourceTable.getIndexes()) {
			copy.addIndex(index);
		}
		// Copying data sets
		for(IDataSet set : sourceTable.getDataSets()) {
			copy.addDataSet(set);
		}
		// Copying tiggers
		for(ITrigger t : sourceTable.getTriggers()) {
			copy.addTrigger(t);
		}
		//Copying constraints
		for(IKeyConstraint c : sourceTable.getConstraints()) {
			// Copying constraint
			IKeyConstraint copiedConstraint = null;
			//Switching constraint type for initialization TODO add a constraint factory if this need appears again
			switch(c.getConstraintType()) {
			case UNIQUE:
			case PRIMARY:
				copiedConstraint = createUniqueKey(c.getName(),c.getDescription(),copy);
				break;
			case FOREIGN:
				copiedConstraint = new ForeignKeyConstraint(c.getName(),c.getDescription(),copy);
				break;
			}
			// Copying constrained columns
			for(IReference r : c.getConstrainedColumnsRef()) {
				copiedConstraint.addConstrainedReference(r);
			}

			// Setting specific attributes
			switch(c.getConstraintType()) {
			case UNIQUE:
			case PRIMARY:
				copiedConstraint.setConstraintType(c.getConstraintType());
				break;
			case FOREIGN:
				final ForeignKeyConstraint refConstraint = (ForeignKeyConstraint)c;
				final ForeignKeyConstraint newFk = (ForeignKeyConstraint)copiedConstraint;
				newFk.setRemoteConstraint(refConstraint.getRemoteConstraint());
				newFk.setOnDeleteAction(refConstraint.getOnDeleteAction());
				newFk.setOnUpdateAction(refConstraint.getOnUpdateAction());
				break;

			}
			// IMPORTANT: Preserving constraint reference
			copiedConstraint.setReference(c.getReference());

			// Adding to table
			copy.addConstraint(copiedConstraint);
		}
		//Copying version attributes from superclass
		versionCopy(source,destination);
	}


	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#createVersionable()
	 */
	@Override
	public IVersionable<?> createVersionable() {
		return createVersionedTable();
	}

	private VersionedTable createVersionedTable() {
		VersionedTable v = new VersionedTable();
		return v;
	}
	
	protected UniqueKeyConstraint createUniqueKey(String name, String description, IBasicTable parent) {
		return new UniqueKeyConstraint(name,description,parent);
	}
}
