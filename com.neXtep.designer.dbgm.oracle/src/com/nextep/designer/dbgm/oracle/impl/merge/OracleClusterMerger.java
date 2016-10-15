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
package com.nextep.designer.dbgm.oracle.impl.merge;

import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

public class OracleClusterMerger extends OracleTableMerger {

	public static final String CATEG_TABLES = "Clustered tables";
	
	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		final IOracleCluster src = (IOracleCluster)source;
		final IOracleCluster tgt = (IOracleCluster)target;
		
		final IComparisonItem result = super.doCompare(source, target);
		listCompare(CATEG_TABLES, result, src==null ? Collections.EMPTY_LIST : src.getClusteredTables(), tgt==null ? Collections.EMPTY_LIST : tgt.getClusteredTables());
		return result;
	}
	
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		final IOracleCluster cluster = (IOracleCluster)target;
		fillName(result, cluster);
		if(cluster.getName()==null) {
			return null;
		}
		save(cluster);
		List<?> clusteredTables = getMergedList(CATEG_TABLES, result, activity);
		for(Object o : clusteredTables) {
			final IOracleClusteredTable t = (IOracleClusteredTable)o;
			t.setCluster(cluster);
			save(t);
			cluster.getClusteredTables().add(t);
		}
		save(cluster);
		return cluster;
	}
	
}
