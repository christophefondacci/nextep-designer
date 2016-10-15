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
package com.nextep.datadesigner.dbgm.gui.navigators;

import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.ui.DiagramEditorInput;
import com.nextep.designer.dbgm.ui.controllers.DiagramUIController;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.vcs.model.IDiagram;

/**
 * @author Christophe Fondacci
 *
 */
public class DiagramNavigator extends UntypedNavigator implements
		INavigatorConnector {


	public DiagramNavigator(IDiagram diagram, DiagramUIController controller) {
		super(diagram,controller);
		//this.controller=controller;
		//diagram.addListener(this);
	}
//	@Override
//	public void initializeChildConnectors() {
//		IDiagram diagram = (IDiagram)getModel();
//
//		for(IDiagramItem i : diagram.getItems()) {
//			this.addConnector(ControllerFactory.getController(IElementType.getInstance("REFERENCE")).initializeNavigator(i.getItemReference()));
//			// Forcing consistency
//			if(i.getParentDiagram()!=diagram) {
//				try {
//					Observable.deactivateListeners();
//					i.setParentDiagram(diagram);
//				} finally {
//					Observable.activateListeners();
//				}
//				
//			}
//		}
//	}

	@Override
	protected void createConnector(INavigatorConnector c, int index) {
		try {
			deactivateRegister();
			super.createConnector(c, index);
		} finally {
			activateRegister();
		}
	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
//	 */
//	@Override
//	public Image getConnectorIcon() {
//		return DBGMImages.ICON_GRAPH;
//	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case ITEM_ADDED:
//			IDiagramItem item = (IDiagramItem)data;
//			addConnector(ControllerFactory.getController(IElementType.getInstance("REFERENCE")).initializeNavigator(item.getItemReference()));
			refreshConnector();
			break;
		case ITEM_REMOVED:
//			this.removeConnector(this.getConnector(((IDiagramItem)data).getItemReference()));
			break;

		}
		refreshConnector();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#defaultAction()
	 */
	@Override
	public void defaultAction() {
		try {
			IDiagram diagram = (IDiagram)getModel();
			//Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("com.neXtep.designer.dbgm.ui.diagView", ((IdentifiedObject)this.getModel()).getUID().toString(), IWorkbenchPage.VIEW_ACTIVATE);
			CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new DiagramEditorInput(diagram), "com.neXtep.designer.dbgm.ui.diagramEditor");
			CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.eclipse.ui.views.ContentOutline");
		} catch( PartInitException e) {
			throw new ErrorException(e);
		}

	}

}
