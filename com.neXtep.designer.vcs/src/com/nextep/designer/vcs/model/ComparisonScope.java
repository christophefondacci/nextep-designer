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
package com.nextep.designer.vcs.model;

import java.util.Arrays;
import java.util.List;

/**
 * Defines the scope of a comparison. Depending on a given comparison, a comparison item may or may
 * not impact the comparison result according to its scope.
 * 
 * @author Christophe Fondacci
 */
public enum ComparisonScope {
	/** This scope indicates the item will only impact a comparison to/from database */
	DATABASE("repoToDb"), //$NON-NLS-1$
	/** This scope indicates the item will only impact a comparison within the repository */
	REPOSITORY("repo", DATABASE), //$NON-NLS-1$
	/** This scope indicates the item will is being transferred from database to repository */
	DB_TO_REPOSITORY("dbToRepo", DATABASE), //$NON-NLS-1$
	/** Default. This scope indicates the item will always impact the comparison result */
	ALL("all", DATABASE, REPOSITORY, DB_TO_REPOSITORY); //$NON-NLS-1$

	private String code;
	private List<ComparisonScope> compatibleScopes;

	ComparisonScope(String code, ComparisonScope... compatibleScopes) {
		this.code = code;
		this.compatibleScopes = Arrays.asList(compatibleScopes);
	}

	public boolean isCompatible(ComparisonScope scope) {
		return compatibleScopes.contains(scope) || scope == this;
	}

	public String getCode() {
		return code;
	}

}
