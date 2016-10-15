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
package com.nextep.designer.core.model;

import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;


/**
 * This enumeration represents the available target types.
 * The designer application will lookup those types to determine
 * which database connection to pick for a given task.
 *
 * @author Christophe Fondacci
 *
 */
public class TargetType extends NamedObservable implements ITypedObject {

	public static final TargetType DEVELOPMENT = new TargetType("Development","Target for developing database updates");
	public static final TargetType ASSEMBLY = new TargetType("Assembly","Target on which deployment packages are built");
	public static final TargetType REFERENCE = new TargetType("Reference","A reference iso-production database");

	public static final String TYPE_ID = "TARGET_TYPE";
	
	public static final TargetType getDefaultTargetType() {
		return DEVELOPMENT;
	}
	private TargetType(String label, String description) {
		setName(label);
		setDescription(description);
	}
	public String getLabel() {
		return getName();
	}
	public String name() {
		return getName().toUpperCase();
	}
	public static TargetType valueOf(String name) {
		for(TargetType t : values()) {
			if(t.name().equals(name)) {
				return t;
			}
		}
        if (name == null)
            throw new NullPointerException("Name is null");
        throw new IllegalArgumentException(
            "No enum const TargetType." + name);
		
	}
	public static TargetType[] values() {
		return new TargetType[] { DEVELOPMENT,ASSEMBLY,REFERENCE };
	}
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}
}
