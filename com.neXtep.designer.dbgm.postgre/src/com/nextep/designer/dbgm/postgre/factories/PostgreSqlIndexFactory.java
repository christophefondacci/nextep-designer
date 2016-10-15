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

import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.factories.IndexFactory;
import com.nextep.designer.dbgm.helpers.FactoryHelper;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.postgre.model.impl.PostgreSqlIndex;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class PostgreSqlIndexFactory extends IndexFactory {

	@Override
	public IVersionable<?> createVersionable() {
		return new PostgreSqlIndex();
	}

	@Override
	public void rawCopy(IVersionable<?> source, IVersionable<?> destination) {
		super.rawCopy(source, destination);
		final IPhysicalObject src = (IPhysicalObject) source.getVersionnedObject().getModel();
		final IPhysicalObject tgt = (IPhysicalObject) destination.getVersionnedObject().getModel();
		if (src.getPhysicalProperties() != null) {
			IPhysicalProperties tgtProps = CorePlugin.getTypedObjectFactory().create(
					ITablePhysicalProperties.class);
			tgt.setPhysicalProperties(tgtProps);
			FactoryHelper.copyPhysicalProperties(src, tgt);
			tgtProps.setParent(tgt);
		}
	}

}
