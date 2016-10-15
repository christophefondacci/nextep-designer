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
package com.nextep.designer.dbgm.ui.jface;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.datadesigner.dbgm.model.IDomainVendorType;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 *
 * @author Christophe Fondacci
 */
public class DomainTypeContentProvider implements IStructuredContentProvider, IEventListener {

	private IDomain domain;
	private TableViewer viewer;
	
	@Override
	public Object[] getElements(Object inputElement) {
		return domain.getVendorTypes().toArray();
	}

	@Override
	public void dispose() {
		unregisterListeners();
	}
	private void unregisterListeners() {
		if(domain!=null) {
			Designer.getListenerService().unregisterListener(domain, this);
			for(IDomainVendorType t :domain.getVendorTypes()) {
				Designer.getListenerService().unregisterListener(t, this);
				Designer.getListenerService().unregisterListener(t, UIControllerFactory.getController(t));
			}
		}
	}
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		unregisterListeners();
		if(newInput != null) {
			this.domain = (IDomain)newInput;
			Designer.getListenerService().registerListener(viewer.getControl(),domain, this);
			for(IDomainVendorType t :domain.getVendorTypes()) {
				Designer.getListenerService().registerListener(viewer.getControl(),t, this);
				Designer.getListenerService().registerListener(viewer.getControl(),t, UIControllerFactory.getController(t));
			}
			this.viewer = (TableViewer)viewer;
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case GENERIC_CHILD_ADDED:
			viewer.add(data);
			Designer.getListenerService().registerListener(viewer.getControl(),(IDomainVendorType)data, this);
			Designer.getListenerService().registerListener(viewer.getControl(),(IDomainVendorType)data, UIControllerFactory.getController(data));
			break;
		case GENERIC_CHILD_REMOVED:
			viewer.remove(data);
			Designer.getListenerService().unregisterListener((IDomainVendorType)data, this);
			Designer.getListenerService().unregisterListener((IDomainVendorType)data, UIControllerFactory.getController(data));
			break;
		default:
			viewer.update(source, null);
		}
		
	}

}
