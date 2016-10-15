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

import java.util.Collection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 *
 * @author Christophe Fondacci
 */
public class DomainContentProvider implements IStructuredContentProvider, IEventListener {

	private TableViewer viewer;
	private Collection<IDomain> domains;
	
	@Override
	public Object[] getElements(Object inputElement) {
		return domains.toArray();
	}

	@Override
	public void dispose() {
		unregisterListeners();
	}
	private void unregisterListeners() {
		if(domains != null) {
			for(IDomain d : domains) {
				Designer.getListenerService().unregisterListener(d, this);
				Designer.getListenerService().unregisterListener(d, UIControllerFactory.getController(d));
			}
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer)viewer;
		if(oldInput instanceof Collection) {
			unregisterListeners();
		}
		if(newInput instanceof Collection) {
			domains = (Collection<IDomain>)newInput;
			for(IDomain d : domains) {
				Designer.getListenerService().registerListener(viewer.getControl(), d, this);
				Designer.getListenerService().registerListener(viewer.getControl(), d, UIControllerFactory.getController(d));
			}
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		viewer.update(source, null);
	}

	public void add(IDomain d) {
		viewer.add(d);
		domains.add(d);
		Designer.getListenerService().registerListener(viewer.getControl(), d, this);
	}
	public void remove(IDomain d) {
		viewer.remove(d);
		domains.remove(d);
		Designer.getListenerService().unregisterListener(d, this);
	}
}
