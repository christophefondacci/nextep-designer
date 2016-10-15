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
package com.nextep.datadesigner.sqlgen.impl;

import com.nextep.datadesigner.sqlgen.model.IPrototype;
import com.nextep.datadesigner.sqlgen.model.IPrototypeMatcher;

/**
 * Default prototype implementation.
 * Should not be overriden.
 * 
 * @author Christophe Fondacci
 *
 */
public class Prototype implements IPrototype {

	private String name;
	private String description;
	private String template;
	private int cursorPos;
	private IPrototypeMatcher matcher;
	public Prototype(String name, String description, String template, int cursorPosition) {
		this.name=name;
		this.description = description;
		this.template = template;
		this.cursorPos = cursorPosition;
		matcher = new DefaultPrototypeMatcher();
	}
	public Prototype(String name, String description, String template, int cursorPosition,IPrototypeMatcher matcher) {
		this.name=name;
		this.description = description;
		this.template = template;
		this.cursorPos = cursorPosition;
		this.matcher = matcher;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getTemplate() {
		return template;
	}
	public int getCursorPosition() {
		return cursorPos;
	}
	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPrototype#getPrototypeMatcher()
	 */
	@Override
	public IPrototypeMatcher getPrototypeMatcher() {
		return matcher;
	}
}
