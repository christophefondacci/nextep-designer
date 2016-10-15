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

import java.util.List;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.dbgm.gui.navigators.DiagramNavigator;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.gef.DBGMGraphicalEditor;
import com.nextep.designer.dbgm.ui.DiagramEditorInput;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * @author Christophe Fondacci
 */
public class DiagramUIController extends AbstractUIController implements ITypedObjectUIController {

	private static DiagramUIController instance = null;

	public DiagramUIController() {
		super();
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.ITEM_ADDED);
	}

	public static DiagramUIController getInstance() {
		if (instance == null) {
			instance = new DiagramUIController();
		}
		return instance;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	public IDisplayConnector initializeEditor(Object content) {
		// return new DiagramEditorGUI((IDiagram)content,this);
		return null;
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
		return new DiagramNavigator((IDiagram) model, this);
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
		IVersionContainer container = (IVersionContainer) parent;
		String name = "NEW_DIAGRAM";
		int index = 1;
		boolean nameOk = false;
		final IVersionContainer view = VCSPlugin.getService(IWorkspaceService.class).getCurrentWorkspace();
		while (!nameOk) {
			List<IVersionable<?>> diagrams = VersionHelper.getAllVersionables(view,
					IElementType.getInstance(IDiagram.TYPE_ID));
			nameOk = true;
			for (IVersionable<?> v : diagrams) {
				if (name.equals(v.getName())) {
					nameOk = false;
					name = "NEW_DIAGRAM" + (index++);
					break;
				}
			}
		}

		IVersionable<IDiagram> newDiagram = (IVersionable<IDiagram>) VersionableFactory
				.createVersionable(IDiagram.class);
		newDiagram.setName(name);
		container.addVersionable(newDiagram, new ImportPolicyAddOnly());
		CorePlugin.getIdentifiableDao().save(newDiagram);
		CorePlugin.getIdentifiableDao().save(container);

		return newDiagram;
	}

	@Override
	public String getEditorId() {
		return DBGMGraphicalEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new DiagramEditorInput((IDiagram) model);
	}

	@Override
	public void defaultOpen(ITypedObject model) {
		super.defaultOpen(model);
		// Showing outline by default
		try {
			CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView("org.eclipse.ui.views.ContentOutline");
		} catch (PartInitException e) {
			throw new ErrorException(e);
		}
	}
}
