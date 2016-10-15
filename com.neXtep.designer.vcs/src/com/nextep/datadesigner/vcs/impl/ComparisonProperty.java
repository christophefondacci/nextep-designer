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

import com.nextep.datadesigner.impl.Property;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonProperty;

/**
 * Implementation of the {@link IComparisonProperty} as a POJO bean.
 * 
 * @author Christophe Fondacci
 */
public class ComparisonProperty extends Property implements IComparisonProperty {

	private String comparedValue;
	private DifferenceType differenceType;

	public ComparisonProperty(String name, String value) {
		this(name, value, null);
	}

	public ComparisonProperty(String name, String value, String comparedValue) {
		super(name, value);
		this.comparedValue = comparedValue;
	}

	@Override
	public String getComparedValue() {
		return comparedValue;
	}

	@Override
	public DifferenceType getDifferenceType() {
		return differenceType;
	}

	public void setDifferenceType(DifferenceType differenceType) {
		this.differenceType = differenceType;
	}
}
