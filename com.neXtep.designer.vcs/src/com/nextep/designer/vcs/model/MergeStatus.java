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
/**
 *
 */
package com.nextep.designer.vcs.model;

/**
 * @author Christophe Fondacci
 *
 */
public enum MergeStatus {

	NOT_MERGED("Not merged"),
	MERGE_RESOLVED("Merged successfully (see contents)"),
	MERGE_UNRESOLVED("Unresolved conflicts"),
	NOT_MERGEABLE("Unmergeable item");

	String title;
	MergeStatus(String title) {
		this.title=title;
	}
	public MergeStatus compute(MergeStatus subStatus) {
		if(this != MERGE_RESOLVED && this != NOT_MERGED) {
			return this;
		} else {
			if(subStatus!=MERGE_RESOLVED) {
				return subStatus;
			} else {
				return MERGE_RESOLVED;
			}
		}
	}
	public String getTitle() {
		return title;
	}

}
