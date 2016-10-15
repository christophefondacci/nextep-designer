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
package com.nextep.designer.dbgm.ui.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.gui.TypeColumnEditorGUI;
import com.nextep.datadesigner.dbgm.gui.navigators.TypeColumnNavigator;
import com.nextep.datadesigner.dbgm.impl.TypeColumn;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.ui.model.base.AbstractUIController;

/**
 * @author Christophe Fondacci
 */
public class TypeColumnUIController extends AbstractUIController {

	private static final Log log = LogFactory.getLog(TypeColumnUIController.class);

	/**
	 * 
	 */
	public TypeColumnUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new TypeColumnEditorGUI((IUserType) content, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeGraphical(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new TypeColumnNavigator((ITypeColumn) model, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	@Override
	public Object newInstance(Object parent) {
		IUserType type = (IUserType) parent;
		// Creating column and assigning its parent
		ITypeColumn col = new TypeColumn();
		col.setParent(type);
		// Defining a name
		col.setName("COLUMN" + (type.getColumns().size() + 1));
		// Saving
		save(type);
		save(col);
		// Eventually adding to parent type
		type.addColumn(col);
		log.info("Column <" + col.getName() + "> added to user-defined type <" + type.getName()
				+ "> and saved to repository.");
		return col;
	}

	@Override
	public boolean isEditable() {
		return false;
	}
}
