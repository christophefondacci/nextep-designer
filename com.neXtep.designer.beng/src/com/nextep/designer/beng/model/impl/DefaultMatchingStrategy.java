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
package com.nextep.designer.beng.model.impl;

import com.nextep.designer.beng.model.base.AbstractMatchingStrategy;

/**
 * @author Christophe Fondacci
 */
public class DefaultMatchingStrategy extends AbstractMatchingStrategy {

	@Override
	public boolean matches(long releaseNumber, long versionBound) {
		return releaseNumber < versionBound;
	}

	@Override
	public long computeVersionBound(String versionPattern) {
		return computeVersionBound(versionPattern, true);
	}

}
