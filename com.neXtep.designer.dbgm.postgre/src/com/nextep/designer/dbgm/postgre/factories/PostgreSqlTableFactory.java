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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.postgre.factories;

import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.factories.TableFactory;
import com.nextep.designer.dbgm.helpers.FactoryHelper;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlTable;
import com.nextep.designer.dbgm.postgre.model.impl.PostgreSqlTable;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class PostgreSqlTableFactory extends TableFactory {

	@Override
	public IVersionable<?> createVersionable() {
		return new PostgreSqlTable();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		super.rawCopy(source, destination);
		final IPostgreSqlTable src = (IPostgreSqlTable) source.getVersionnedObject().getModel();
		final IPostgreSqlTable tgt = (IPostgreSqlTable) destination.getVersionnedObject()
				.getModel();
		if (src.getPhysicalProperties() != null) {
			IPhysicalProperties tgtProps = CorePlugin.getTypedObjectFactory().create(
					ITablePhysicalProperties.class);
			tgt.setPhysicalProperties(tgtProps);
			FactoryHelper.copyPhysicalProperties(src, tgt);
			tgtProps.setParent(tgt);
		}

		for (IReference r : src.getInheritances()) {
			tgt.addInheritanceRef(r);
		}

		for (ICheckConstraint c : src.getCheckConstraints()) {
			ICheckConstraint newCheck = CorePlugin.getTypedObjectFactory().create(
					ICheckConstraint.class);
			newCheck.setName(c.getName());
			newCheck.setConstrainedTable(tgt);
			newCheck.setCondition(c.getCondition());
			newCheck.setDescription(c.getDescription());
			newCheck.setReference(c.getReference());
			tgt.addCheckConstraint(newCheck);
		}

	}

}
