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
package com.nextep.datadesigner.vcs.impl;

import com.nextep.datadesigner.impl.StringAttribute;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;

/**
 * A comparison item which dynamically evaluates the
 * difference type based on nullity and <code>equals</code>
 * method.
 *
 * @author Christophe Fondacci
 *
 */
public class ComparisonAttribute extends AbstractComparisonItem {

	private DifferenceType type;
	private String name;
	public ComparisonAttribute(String name, String valueSource, String valueTarget) {
		this(name,valueSource,valueTarget,false);
	}
	/**
	 * Builds a new comparison attribute.<br>
	 * This constructor allows to specify whether the null and empty
	 * source items should be ignored in the comparison.<br>
	 * Ignoring the item will make them EQUAL if the source value is 
	 * empty or null. Use only after understanding all the impacts.
	 * 
	 * @param name attribute name
	 * @param valueSource source value
	 * @param valueTarget target value
	 * @param ignoreSourceNullAndEmpty if <code>true</code> the difference
	 * 		  type will be set to EQUALS if the source value is empty or 
	 * 		  null. This allow to ignore some items for which <code>null</code>
	 * 		  value means <i>undefined</i> rather than a null value. 
	 */
	public ComparisonAttribute(String name, String valueSource, String valueTarget,boolean ignoreSourceNullAndEmpty) {
		super(valueSource == null ? null : new StringAttribute(name,valueSource), valueTarget == null ? null : new StringAttribute(name,valueTarget), null);
		this.name=name;
		// Special use case for considering equality for null or empty source values
		if(ignoreSourceNullAndEmpty) {
			if(valueSource == null || "".equals(valueSource)) {
				type = DifferenceType.EQUALS;
				return;
			}
		}
		if(getSource() == null && getTarget() == null) {
			type = DifferenceType.EQUALS;
		} else if(getSource() == null ) {
			type = DifferenceType.MISSING_SOURCE;
		} else if(getTarget() == null) {
			type = DifferenceType.MISSING_TARGET;
		} else {
			type = (getSource().equals(getTarget()) ? DifferenceType.EQUALS : DifferenceType.DIFFER);
		}
	}
	public ComparisonAttribute(String name, String valueSource, String valueTarget,ComparisonScope scope) {
		this(name,valueSource,valueTarget);
		setScope(scope);
	}
	/**
	 * @see com.nextep.datadesigner.vcs.impl.AbstractComparisonItem#getDifferenceType()
	 */
	@Override
	public DifferenceType getDifferenceType() {
		return type;
	}
	/**
	 * Setter for child implementation
	 * @param type new difference type
	 */
	protected void setDifferenceType(DifferenceType type) {
		this.type=type;
	}
	public String getName() {
		return name;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.AbstractComparisonItem#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance("ATTRIBUTE");
	}
}
