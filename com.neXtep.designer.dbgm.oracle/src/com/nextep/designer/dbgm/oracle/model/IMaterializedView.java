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
package com.nextep.designer.dbgm.oracle.model;

import com.nextep.datadesigner.dbgm.model.ISqlBased;
import com.nextep.datadesigner.model.ITypedObject;

public interface IMaterializedView extends ITypedObject, IOracleTable, ISqlBased {

	public static final String VIEW_TYPE_ID = "MAT_VIEW"; //$NON-NLS-1$

	/**
	 * @return the time of refresh of this materialized view
	 */
	RefreshTime getRefreshTime();

	/**
	 * Defines the refresh time of this materialized view
	 * 
	 * @param time new refresh time
	 */
	void setRefreshTime(RefreshTime time);

	/**
	 * @return the refresh method of this materialized view
	 */
	RefreshMethod getRefreshMethod();

	/**
	 * Defines the refresh method of this materialized view
	 * 
	 * @param method
	 */
	void setRefreshMethod(RefreshMethod method);

	/**
	 * @return an expression to evaluate as the start date of the materialized view refresh. This
	 *         information is used depending on the {@link RefreshTime}
	 */
	String getStartExpr();

	/**
	 * Defines an expression to evaluate as the start date of a materialized view refresh.
	 * Used/ignored depending on the {@link RefreshTime}.
	 * 
	 * @param sqlExpr an SQL expression generating the start date
	 */
	void setStartExpr(String sqlExpr);

	/**
	 * @return an expression evaluating the next refresh date of this materialized view.
	 */
	String getNextExpr();

	/**
	 * Defines an expression evaluating the next refresh date.
	 * 
	 * @param sqlExpr SQL expression returning the next refresh date
	 */
	void setNextExpr(String sqlExpr);

	/**
	 * @return the build type of this materialized view
	 */
	BuildType getBuildType();

	/**
	 * Defines the build type of this materialized view.
	 * 
	 * @param buildType new build type
	 */
	void setBuildType(BuildType buildType);

	/**
	 * @return the materialized view type
	 */
	MaterializedViewType getViewType();

	/**
	 * Defines the materialized view type
	 * 
	 * @param type new materialized view type
	 */
	void setViewType(MaterializedViewType type);

	/**
	 * @return whether this materialized view enables query rewrite by the Oracle engine.
	 */
	boolean isQueryRewriteEnabled();

	/**
	 * Defines whether this materialized view enables query rewrite by the Oracle engine.
	 * 
	 * @param enabled new query rewrite state
	 */
	void setQueryRewriteEnabled(boolean enabled);
}
