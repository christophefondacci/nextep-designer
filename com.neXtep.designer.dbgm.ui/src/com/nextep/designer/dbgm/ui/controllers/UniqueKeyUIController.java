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
package com.nextep.designer.dbgm.ui.controllers;

import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.dbgm.gui.UniqueKeyEditor;
import com.nextep.datadesigner.dbgm.gui.navigators.ConstraintNavigator;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.external.VersionableDisplayDecorator;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public class UniqueKeyUIController extends AbstractUIController implements
		ITypedObjectUIController, IEventListener {

	private static UniqueKeyUIController instance = null;

	public UniqueKeyUIController() {
		super();
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.COLUMN_ADDED);
		addSaveEvent(ChangeEvent.COLUMN_REMOVED);
	}

	public static UniqueKeyUIController getInstance() {
		if (instance == null) {
			instance = new UniqueKeyUIController();
		}
		return instance;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	public IDisplayConnector initializeEditor(Object content) {
		final IBasicTable t = ((IKeyConstraint) content).getConstrainedTable();
		return new VersionableDisplayDecorator(new UniqueKeyEditor((UniqueKeyConstraint) content,
				this), VersionHelper.getVersionable(t));
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeGraphical(java.lang.Object)
	 */
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	public INavigatorConnector initializeNavigator(Object model) {
		IKeyConstraint c = (IKeyConstraint) model;
		// if(listening.add(c)) {
		// c.addListener(this);
		// }
		return new ConstraintNavigator(c, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	public Object newInstance(Object parent) {
		if (!(parent instanceof IBasicTable)) {
			throw new ErrorException("Cannot instantiate a constraint on a non table object.");
		}
		parent = VCSUIPlugin.getVersioningUIService().ensureModifiable(parent);
		IBasicTable t = (IBasicTable) parent;
		// Initializing proper unique key name
		String keyName = "";
		boolean hasPK = false;
		int ukCount = 0;
		if (t.getShortName() != null && !"".equals(t.getShortName())) {
			keyName += t.getShortName() + "_";
		}
		for (IKeyConstraint c : t.getConstraints()) {
			switch (c.getConstraintType()) {
			case PRIMARY:
				hasPK = true;
				break;
			case UNIQUE:
				ukCount++;
			}
		}
		keyName += (hasPK ? "UK" : "PK");
		keyName = getAvailableName(IElementType.getInstance(UniqueKeyConstraint.TYPE_ID), keyName);
		IKeyConstraint c = (IKeyConstraint) CorePlugin.getTypedObjectFactory().create(
				IKeyConstraint.class);
		c.setName(keyName);
		c.setConstrainedTable((IBasicTable) parent);
		if (!hasPK) {
			c.setConstraintType(ConstraintType.PRIMARY);
			// TODO: handle mysql PK names in a more elegant way
			if (VersionHelper.getCurrentView().getDBVendor() == DBVendor.MYSQL) {
				c.setName("PRIMARY");
			}
		}
		c.setConstrainedTable((IBasicTable) parent);

		save(c);
		((IBasicTable) parent).addConstraint(c);
		return c;
	}

	public void togglePrimaryKey(IKeyConstraint pkConstraint) {
		ConstraintType type = pkConstraint.getConstraintType();
		switch (type) {
		case PRIMARY:
			// If constraint was previously a primary key, we switch it to unique
			pkConstraint.setConstraintType(ConstraintType.UNIQUE);
			break;
		case UNIQUE:
			// Switching previous primary key to unique key
			for (IKeyConstraint c : pkConstraint.getConstrainedTable().getConstraints()) {
				switch (c.getConstraintType()) {
				case PRIMARY:
					c.setConstraintType(ConstraintType.UNIQUE);
					break;
				}
			}
			pkConstraint.setConstraintType(ConstraintType.PRIMARY);
			break;
		}
	}

	public void removeUniqueKey(IKeyConstraint c) {

	}

	@Override
	public String getEditorId() {
		return TypedFormRCPEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new TypedEditorInput(((IKeyConstraint) model).getConstrainedTable());
	}
}
