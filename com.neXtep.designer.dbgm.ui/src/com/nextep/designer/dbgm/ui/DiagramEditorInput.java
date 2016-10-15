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
package com.nextep.designer.dbgm.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 *
 */
public class DiagramEditorInput implements IEditorInput, IModelOriented<IDiagram>, IEventListener {

	private IDiagram diagram;
//	private IPersistableElement persistor;
	public DiagramEditorInput(IDiagram diagram) {
		this.diagram = diagram;
		if(diagram!=null) {
			Designer.getListenerService().registerListener(this,diagram, this);
		}
//		persistor = new DiagramEditorPersistable(diagram);
	}
	public IDiagram getDiagram() {
		return diagram;
	}
	/**
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	@Override
	public boolean exists() {
		return true;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(DBGMImages.ICON_GRAPH);
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	@Override
	public String getName() {
		return diagram.getName();
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		IVersionable<IDiagram> v = VersionHelper.getVersionable(diagram);
		String prefix = "";
		if(v != null && v.getContainer()!=null) {
			prefix = v.getContainer().getName() + "/";
		}
		return prefix + (diagram !=null ? diagram.getName() : "");
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DiagramEditorInput) {
			return getDiagram() == ((DiagramEditorInput)obj).getDiagram();
		}
		return false;
	}
	@Override
	public IDiagram getModel() {
		return diagram;
	}
	@Override
	public void setModel(IDiagram model) {
		this.diagram = model;
	}
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// Nothing to do here, only listening to have proper model switch
	}
}
