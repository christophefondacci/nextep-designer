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
 * This specific comparison attribute allows to compare
 * with a case insensitive equality check. Should only 
 * be used for String comparison and, in most cases, for
 * object names.
 * 
 * @author Christophe Fondacci
 *
 */
public class ComparisonNameAttribute extends ComparisonAttribute {

	public ComparisonNameAttribute(String name, String valueSource, String valueTarget, boolean caseSensitive) {
		super(name,valueSource,valueTarget);
		if(!caseSensitive && getDifferenceType()==DifferenceType.DIFFER) {
			setDifferenceType(valueSource.equalsIgnoreCase(valueTarget) ? DifferenceType.EQUALS : DifferenceType.DIFFER);
		}
	}
}
