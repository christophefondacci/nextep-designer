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
package com.nextep.datadesigner.impl;

import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.INamedObject;

/**
 * A class helper which manage implementation of
 * INamedObject. A class implementing this interface
 * should delegate the process to this helper.
 *
 * @author Christophe Fondacci
 *
 */
public class NamedObjectHelper implements INamedObject {

	private String description;
	private String name;
	private IFormatter f;

	public NamedObjectHelper(String name, String description) {
		this.name=name;
		this.description=description;
		this.f=IFormatter.NOFORMAT;
	}
	public NamedObjectHelper(String name, String description, IFormatter f) {
		this.name=f.format(name);
		this.description=description;
		this.f=f;

	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setDescription(String description) {
		this.description=description == null ? "" : description;
	}

	@Override
	public void setName(String name) {
		this.name=f.format(name);
	}
	public void setFormatter(IFormatter f) {
		this.f=f;
	}
	@Override
	public String toString() {
		return name;
	}
}
