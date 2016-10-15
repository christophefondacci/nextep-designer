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
package com.nextep.designer.dbgm.mysql.ctrl;

import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.dbgm.mysql.ui.impl.MySQLUniqueKeyEditor;
import com.nextep.designer.dbgm.ui.controllers.UniqueKeyUIController;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * Overloading default controller to reroute MySQL Unique key creations to Index creations.
 * 
 * @author Christophe
 */
public class MySQLUniqueKeyController extends UniqueKeyUIController {

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new MySQLUniqueKeyEditor((UniqueKeyConstraint) content, this);
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		final IBasicTable t = (IBasicTable) parent;
		// If table already has a PK, we create an index
		if (DBGMHelper.getPrimaryKey(t) != null) {
			final IIndex index = (IIndex) UIControllerFactory.getController(
					IElementType.getInstance(IIndex.INDEX_TYPE)).emptyInstance(name, parent);
			index.setIndexType(IndexType.UNIQUE);
			return index;
		} else {
			UniqueKeyConstraint uk = (UniqueKeyConstraint) super.emptyInstance(name, parent);
			uk.setConstraintType(ConstraintType.PRIMARY);
			return uk;
		}
	}

	@Override
	public Object newInstance(Object parent) {
		final IBasicTable t = (IBasicTable) parent;
		// If table already has a PK, we create an index
		if (DBGMHelper.getPrimaryKey(t) != null) {
			final IIndex index = (IIndex) UIControllerFactory.getController(
					IElementType.getInstance(IIndex.INDEX_TYPE)).newInstance(parent);
			index.setIndexType(IndexType.UNIQUE);
			return index;
		} else {
			return super.newInstance(parent);
		}
	}
}
