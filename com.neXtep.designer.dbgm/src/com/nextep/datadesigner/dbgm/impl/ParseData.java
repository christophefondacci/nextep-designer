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
package com.nextep.datadesigner.dbgm.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.designer.dbgm.sql.TextPosition;

/**
 * @author Christophe Fondacci
 *
 */
public class ParseData implements IParseData {

	private Map<Object,TextPosition> positionsMap;

	public ParseData() {
		positionsMap = new HashMap<Object, TextPosition>();
	}
	/**
	 * @see com.nextep.datadesigner.dbgm.model.IParseData#getPosition(java.lang.Object)
	 */
	@Override
	public TextPosition getPosition(Object entity) {
		return positionsMap.get(entity);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IParseData#setPosition(java.lang.Object, org.eclipse.jface.text.Position)
	 */
	@Override
	public void setPosition(Object entity, TextPosition position) {
		positionsMap.put(entity,position);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IParseData#getPositions()
	 */
	@Override
	public Collection<TextPosition> getPositions() {
		return positionsMap.values();
	}
	/**
	 * @see com.nextep.datadesigner.dbgm.model.IParseData#getEntity(int)
	 */
	@Override
	public Object getEntity(int offset) {
		for(Object o : positionsMap.keySet()) {
			TextPosition p = positionsMap.get(o);
			if(p.offset<= offset && p.offset+p.length>offset) {
				return o;
			}
		}
		// No matching position
		return null;
	}
}
