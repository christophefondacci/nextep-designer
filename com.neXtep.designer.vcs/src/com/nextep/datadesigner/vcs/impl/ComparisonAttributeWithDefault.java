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
package com.nextep.datadesigner.vcs.impl;

import com.nextep.designer.vcs.model.DifferenceType;

/**
 * A comparison attribute which can have a default value. 
 * This default value is considered equal to null / undefined values
 * in the source or target.
 *  
 * @author Christophe
 *
 */
public class ComparisonAttributeWithDefault extends ComparisonAttribute {

	public ComparisonAttributeWithDefault(String name, String valueSource, String valueTarget, String defaultValue) {
		super(name,valueSource,valueTarget);
		handleDefault(valueSource, valueTarget, defaultValue);
	}
	public ComparisonAttributeWithDefault(String name, String valueSource, String valueTarget, String defaultValue, boolean ignoreSourceNullOrEmpty) {
		super(name,valueSource,valueTarget,ignoreSourceNullOrEmpty);
		handleDefault(valueSource, valueTarget, defaultValue);
	}
	private void handleDefault(String valueSource, String valueTarget, String defaultValue) {
		// Checking default cases
		if((valueSource==null || "".equals(valueSource.trim())) && defaultValue.equals(valueTarget)) {
			setDifferenceType(DifferenceType.EQUALS);
		} else if( defaultValue.equals(valueSource) && (valueTarget==null || "".equals(valueTarget.trim()))) {
			setDifferenceType(DifferenceType.EQUALS);
		}
	}
}
