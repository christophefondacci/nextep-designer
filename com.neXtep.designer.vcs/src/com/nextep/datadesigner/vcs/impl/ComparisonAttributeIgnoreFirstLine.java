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
 * A child class only making public the setDifferenceType method.
 * We don't want to make it public for general-purpose attribute
 * comparison.<br>
 * In some cases, we might need to force a difference type, even when
 * source and target values are not strictly equals. This corresponds 
 * to string differences which would result in the same database object.
 * 
 * @author Christophe
 *
 */
public class ComparisonAttributeIgnoreFirstLine extends ComparisonAttribute {

	public ComparisonAttributeIgnoreFirstLine(String name, String valueSource, String valueTarget) {
		super(name,valueSource,valueTarget);
		// Checking ORACLE vendor to avoid unexpected MySQL regressions, should be ok to remove this
		if(getDifferenceType()!=DifferenceType.EQUALS && valueSource!=null && valueTarget!=null) {
			final int srcIndex = valueSource.indexOf('\n');
			final int tgtIndex = valueTarget.indexOf('\n');
			if(srcIndex>-1 && tgtIndex>-1 && valueSource.length()>srcIndex && valueTarget.length()>tgtIndex) {
				if(valueSource.substring(srcIndex).equals(valueTarget.substring(tgtIndex))) {
					setDifferenceType(DifferenceType.EQUALS);
				}
			}
		}
	}

}
