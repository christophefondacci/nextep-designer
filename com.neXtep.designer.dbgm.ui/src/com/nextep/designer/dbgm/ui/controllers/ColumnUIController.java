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
import com.nextep.datadesigner.dbgm.gui.ColumnEditorGUI;
import com.nextep.datadesigner.dbgm.gui.navigators.ColumnNavigator;
import com.nextep.datadesigner.dbgm.impl.BasicColumn;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.ui.services.IVersioningUIService;

/**
 * @author Christophe Fondacci
 */
public class ColumnUIController extends AbstractUIController implements ITypedObjectUIController {

	// private static final Log log = LogFactory.getLog(ColumnController.class);

	public ColumnUIController() {
		super();
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	public IDisplayConnector initializeEditor(Object content) {
		IBasicTable table = null;
		if (content instanceof IBasicTable) {
			table = (IBasicTable) content;
		} else if (content instanceof IBasicColumn) {
			table = (IBasicTable) ((IBasicColumn) content).getParent();
		} else {
			throw new ErrorException(DBGMUIMessages.getString("column.controller.invalidParent")); //$NON-NLS-1$
		}
		return new ColumnEditorGUI(table, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeGraphical(java.lang.Object)
	 */
	public IDisplayConnector initializeGraphical(Object content) {
		// return new ColumnGraphConnector((IBasicColumn)content,this);
		return null;
	}

	/**
	 * Creates a new instance of a table column. Be careful: since the parent should be passed to
	 * this method, the argument will be the <code>IBasicTable</code> object rather than a
	 * <code>IBasicColumn</code> instance.
	 * 
	 * @param parent the IBasicTable parent table instance
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	public Object newInstance(Object parent) {
		IBasicTable t = (IBasicTable) parent;
		int index = t.getColumns().size();
		// log.info("Adding new column for table <" + t.getName() + ">");
		IBasicColumn c = (IBasicColumn) emptyInstance(getAvailableName(getType()), t);

		return c;
	}

	/**
	 * <b>WARNING</b> This empty instance is unsaved!
	 * 
	 * @see com.nextep.designer.ui.model.base.AbstractUIController#emptyInstance(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public final Object emptyInstance(String name, Object parent) {
		IBasicTable t = (IBasicTable) parent;
		t = CorePlugin.getService(IVersioningUIService.class).ensureModifiable(t);
		IBasicColumn c = instantiate(name, t);
		c.setDatatype(Datatype.getDefaultDatatype());
		c.setParent(t);
		t.addColumn(c);
		CorePlugin.getIdentifiableDao().save(c);
		// log.info("Column <" + c.getName() + "> added to table <" +t.getName() +
		// "> and saved to repository.");
		return c;
	}

	protected IBasicColumn instantiate(String name, Object parent) {
		if (name == null) {
			name = getAvailableName(getType());
		}
		return new BasicColumn(name, "", null, ((IBasicTable) parent).getColumns().size());
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	public INavigatorConnector initializeNavigator(Object model) {
		return new ColumnNavigator((IBasicColumn) model, this);
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public String getEditorId() {
		return TypedFormRCPEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new TypedEditorInput(((IBasicColumn) model).getParent());
	}
}
