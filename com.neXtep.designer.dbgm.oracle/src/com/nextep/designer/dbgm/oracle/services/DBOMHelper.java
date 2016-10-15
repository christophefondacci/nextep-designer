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
package com.nextep.designer.dbgm.oracle.services;

import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Provides helping methods for Oracle related model.
 * 
 * @author Christophe
 *
 */
public class DBOMHelper {

	/**
	 * Retrieves any cluster which would contain this table.
	 * 
	 * @param t table to look for in cluster definitions
	 * @return the cluster containing this table or <code>null</code> if no cluster 
	 * 		   contains this table.
	 */
	public static IOracleClusteredTable getClusterFor(IBasicTable t) {
		List<IVersionable<?>> clusters = VersionHelper.getAllVersionables(VersionHelper.getCurrentView(), IElementType.getInstance(IOracleCluster.CLUSTER_TYPE_ID));
		for(IVersionable<?> v : clusters) {
			final IOracleCluster cluster = (IOracleCluster)v.getVersionnedObject().getModel();
			for(IOracleClusteredTable ct : cluster.getClusteredTables()) {
				if(ct.getTableReference().equals(t.getReference())) {
					return ct;
				}
			}
		}
		return null;
	}
}
