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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.dbgm.gui.ForeignKeyNewEditorGUI;
import com.nextep.datadesigner.dbgm.gui.navigators.ConstraintNavigator;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.external.VersionableDisplayDecorator;
import com.nextep.datadesigner.vcs.services.NamingService;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;

/**
 * @author Christophe Fondacci
 */
public class ForeignKeyUIController extends AbstractUIController implements
		ITypedObjectUIController, IEventListener {

	private static ForeignKeyUIController instance = null;

	public ForeignKeyUIController() {
		super();
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.COLUMN_ADDED);
		addSaveEvent(ChangeEvent.COLUMN_REMOVED);

	}

	public static ForeignKeyUIController getInstance() {
		if (instance == null) {
			instance = new ForeignKeyUIController();
		}
		return instance;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	public IDisplayConnector initializeEditor(Object content) {
		ForeignKeyConstraint fk = (ForeignKeyConstraint) content;
		return new VersionableDisplayDecorator(new ForeignKeyNewEditorGUI(fk, this),
				VersionHelper.getVersionable(fk.getConstrainedTable()));
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
		// IKeyConstraint c = (IKeyConstraint)model;
		// c.addListener(this);
		return new ConstraintNavigator((IKeyConstraint) model, this);
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
		IBasicTable t = (IBasicTable) parent;
		String fkName = (t.getShortName() == null ? "" : t.getShortName()) + "__FK";
		ForeignKeyConstraint c = new ForeignKeyConstraint(fkName, "", t);
		c.setName(getAvailableName(c.getType()));
		save(c);
		t.addConstraint(c);
		CorePlugin.getIdentifiableDao().save(t);

		// If no enforcing index found, query the user to create one
		if (c.getEnforcingIndex().isEmpty() && !c.getConstrainedColumnsRef().isEmpty()) {
			NamingService.getInstance().adjustName(c);
			boolean create = MessageDialog.openQuestion(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					DBGMUIMessages.getString("createEnforcingIndexTitle"),
					DBGMUIMessages.getString("createEnforcingIndexQuestion"));
			if (create) {
				IIndex i = (IIndex) UIControllerFactory.getController(
						IElementType.getInstance(IIndex.INDEX_TYPE)).emptyInstance(c.getName(),
						c.getConstrainedTable());
				// i.setIndexedTableRef(c.getConstrainedTable().getReference());
				for (IReference colRef : c.getConstrainedColumnsRef()) {
					i.addColumnRef(colRef);
				}
			}
		}
		return c;
	}

	public Object emptyInstance(String name, Object parent) {
		IBasicTable t = (IBasicTable) parent;
		ForeignKeyConstraint c = new ForeignKeyConstraint(name, "", t);
		save(c);
		t.addConstraint(c);
		CorePlugin.getIdentifiableDao().save(t);
		return c;
	}

	@Override
	public String getEditorId() {
		return TypedFormRCPEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new TypedEditorInput(((ForeignKeyConstraint) model).getConstrainedTable());
	}
}
