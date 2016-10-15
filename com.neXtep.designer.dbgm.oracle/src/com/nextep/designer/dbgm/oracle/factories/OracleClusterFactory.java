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

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.oracle.impl.OracleCluster;
import com.nextep.designer.dbgm.oracle.impl.OracleClusteredTable;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Factory of Oracle clusters
 * @author Christophe
 *
 */
public class OracleClusterFactory extends OracleTableFactory {

	@SuppressWarnings("unchecked")
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		super.rawCopy(source, destination);
		final IOracleCluster src = (IOracleCluster)source.getVersionnedObject().getModel();
		final IOracleCluster tgt = (IOracleCluster)destination.getVersionnedObject().getModel();
		for(IOracleClusteredTable t : src.getClusteredTables()) {
			final IOracleClusteredTable cl = new OracleClusteredTable();
			cl.setReference(t.getReference());
			cl.setTableReference(t.getTableReference());
			for(IReference clusterColRefSrc : t.getColumnMappings().keySet()) {
				cl.setColumnReferenceMapping(clusterColRefSrc, t.getColumnReferenceMapping(clusterColRefSrc));
			}
			tgt.getClusteredTables().add(cl);
		}
	}

	@Override
	public IVersionable<IBasicTable> createVersionable() {
		return new OracleCluster();
	}
}
