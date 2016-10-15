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
package com.nextep.designer.beng.model;

/**
 * A matching strategy interface allowing implementors to define the way deliveries are retrieved
 * from a release pattern. Depending on the strategy, boundary computation and match strategy can
 * vary. For example, a strategy can decide to consider that the pattern <code>1.0.5</code> matches
 * all deliveries <code>1.0.5.*</code>, another strategy may match exactly <code>1.0.5.0</code> and
 * another may match any release inferior to <code>1.0.6.0</code>...
 * 
 * @author Christophe Fondacci
 */
public interface IMatchingStrategy {

	/**
	 * Indicates whether the specified release number matches the specified bound
	 * 
	 * @param releaseNumber release number to check
	 * @param versionBound version bound
	 * @return <code>true</code> if the release number matches, else <code>false</code>
	 */
	boolean matches(long releaseNumber, long versionBound);

	/**
	 * Computes the version bound to use from the specified version pattern.
	 * 
	 * @param versionPattern the version pattern string
	 * @return the boundary release number
	 */
	long computeVersionBound(String versionPattern);
}
