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
package com.nextep.designer.dbgm.oracle.factories;

import com.nextep.designer.dbgm.oracle.impl.OracleMaterializedViewLog;
import com.nextep.designer.dbgm.oracle.impl.external.MaterializedViewLogPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLogPhysicalProperties;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class OracleMaterializedViewLogFactory extends VersionableFactory {

	@Override
	public IVersionable<?> createVersionable() {
		return new OracleMaterializedViewLog();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		final IMaterializedViewLog src= (IMaterializedViewLog)source.getVersionnedObject().getModel();
		final IMaterializedViewLog tgt= (IMaterializedViewLog)destination.getVersionnedObject().getModel();
		
		tgt.setIncludingNewValues(src.isIncludingNewValues());
		tgt.setPrimaryKey(src.isPrimaryKey());
		tgt.setRowId(src.isRowId());
		tgt.setSequence(src.isSequence());
		tgt.setTableReference(src.getTableReference());
		if(src.getPhysicalProperties()!=null) {
			IMaterializedViewLogPhysicalProperties props = new MaterializedViewLogPhysicalProperties();
			tgt.setPhysicalProperties(props);
			OracleFactoryHelper.copyPhysicalProperties(src, tgt);
			props.setParent(tgt);
		}
		versionCopy(source, destination);
	}

}
