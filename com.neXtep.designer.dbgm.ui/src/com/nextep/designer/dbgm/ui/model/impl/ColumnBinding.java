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
package com.nextep.designer.dbgm.ui.model.impl;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.dbgm.ui.model.IColumnBinding;

/**
 * @author Christophe Fondacci
 */
public class ColumnBinding implements IColumnBinding {

	private IBasicColumn column, associatedColumn;
	private IMarker marker;

	public ColumnBinding(IBasicColumn column, IBasicColumn associatedColumn) {
		this(column, associatedColumn, null);
	}

	public ColumnBinding(IBasicColumn column, IBasicColumn associatedColumn, IMarker marker) {
		this.column = column;
		this.associatedColumn = associatedColumn;
		this.marker = marker;
	}

	@Override
	public IBasicColumn getColumn() {
		return column;
	}

	@Override
	public IBasicColumn getAssociatedColumn() {
		return associatedColumn;
	}

	@Override
	public IMarker getMarker() {
		return marker;
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(IBasicColumn.TYPE_ID);
	}
}
