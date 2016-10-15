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
package com.nextep.designer.dbgm.oracle.impl;

import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.oracle.model.BuildType;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.MaterializedViewType;
import com.nextep.designer.dbgm.oracle.model.RefreshMethod;
import com.nextep.designer.dbgm.oracle.model.RefreshTime;

public class OracleMaterializedView extends OracleTable implements
		IMaterializedView {

	private String nextExpr;
	private String startExpr;
	private String sqlQuery;
	private RefreshMethod method = RefreshMethod.FAST;
	private RefreshTime time = RefreshTime.DEMAND;
	private BuildType buildType = BuildType.IMMEDIATE;
	private MaterializedViewType viewType = MaterializedViewType.PRIMARY_KEY;
	private boolean queryRewriteEnabled = false;
	public OracleMaterializedView() {
		nameHelper.setFormatter(IFormatter.UPPERCASE);
	}
	@Override
	public IElementType getType() {
		return IElementType.getInstance(VIEW_TYPE_ID);
	}

	@Override
	public String getNextExpr() {
		return nextExpr;
	}

	@Override
	public RefreshMethod getRefreshMethod() {
		return method;
	}

	@Override
	public RefreshTime getRefreshTime() {
		return time;
	}

	@Override
	public String getSql() {
		return sqlQuery;
	}

	@Override
	public String getStartExpr() {
		return startExpr;
	}

	@Override
	public void setNextExpr(String sqlExpr) {
		this.nextExpr = sqlExpr;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setRefreshMethod(RefreshMethod method) {
		if(method!=this.method) {
			this.method=method;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setRefreshTime(RefreshTime time) {
		if(this.time!=time) {
			this.time = time;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setSql(String sql) {
		this.sqlQuery = sql;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setStartExpr(String sqlExpr) {
		this.startExpr = sqlExpr;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public BuildType getBuildType() {
		return buildType;
	}

	@Override
	public void setBuildType(BuildType buildType) {
		if(this.buildType!=buildType) {
			this.buildType=buildType;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public MaterializedViewType getViewType() {
		return viewType;
	}

	@Override
	public void setViewType(MaterializedViewType type) {
		if(this.viewType!=type) {
			this.viewType = type;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		if(getName()==null || "".equals(getName().trim())) {
			String elementTypeName = "";
			if(this instanceof ITypedObject) {
				elementTypeName = ((ITypedObject)this).getType().getName().toLowerCase() + " ";
			}
			throw new InconsistentObjectException("A " + elementTypeName + " name must be defined and not empty.");
		}
	}
	@Override
	public boolean isQueryRewriteEnabled() {
		return queryRewriteEnabled;
	}
	@Override
	public void setQueryRewriteEnabled(boolean enabled) {
		if(this.queryRewriteEnabled!=enabled) {
			this.queryRewriteEnabled = enabled;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}
}
