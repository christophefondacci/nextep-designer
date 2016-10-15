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
package com.nextep.designer.vcs.ui.jface;

import java.util.Collection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;

/**
 * A simple versionable content provider providing a collection of {@link IVersionable} elements
 * 
 * @author Christophe Fondacci
 */
public class VersionableTableContentProvider implements IStructuredContentProvider, IEventListener {

	private Collection<IVersionable<?>> versionables;
	private Viewer viewer;
	private IVersioningOperationContext context;

	public VersionableTableContentProvider() {
	}

	public VersionableTableContentProvider(IVersioningOperationContext context) {
		this.context = context;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (versionables != null) {
			return versionables.toArray();
		}
		return null;
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		Designer.getListenerService().unregisterListeners(this);
		if (newInput instanceof Collection<?>) {
			versionables = (Collection<IVersionable<?>>) newInput;
			this.viewer = viewer;
			for (IVersionable<?> v : versionables) {
				Designer.getListenerService().registerListener(this, v, this);
				if (context == null) {
					Designer.getListenerService().registerListener(this, v.getVersion(), this);
				} else {
					Designer.getListenerService().registerListener(this,
							context.getTargetVersionInfo(v), this);
				}
			}
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (!viewer.getControl().isDisposed()) {
			((StructuredViewer) viewer).refresh(source);
		}
	}
}
