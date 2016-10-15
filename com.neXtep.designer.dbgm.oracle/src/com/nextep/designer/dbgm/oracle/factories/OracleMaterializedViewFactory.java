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

import com.nextep.designer.dbgm.oracle.impl.OracleMaterializedView;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.vcs.model.IVersionable;

public class OracleMaterializedViewFactory extends OracleTableFactory {

	@Override
	public IVersionable<?> createVersionable() {
		return new OracleMaterializedView();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		final IMaterializedView src = (IMaterializedView)source.getVersionnedObject().getModel();
		final IMaterializedView tgt = (IMaterializedView)destination.getVersionnedObject().getModel();

		tgt.setRefreshMethod(src.getRefreshMethod());
		tgt.setRefreshTime(src.getRefreshTime());
		tgt.setViewType(src.getViewType());
		tgt.setStartExpr(src.getStartExpr());
		tgt.setNextExpr(src.getNextExpr());
		tgt.setBuildType(src.getBuildType());
		tgt.setSql(src.getSql());
		tgt.setQueryRewriteEnabled(src.isQueryRewriteEnabled());
		super.rawCopy(source, destination);
	}

}
