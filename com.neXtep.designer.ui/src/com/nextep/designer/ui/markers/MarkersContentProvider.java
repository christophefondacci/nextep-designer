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
package com.nextep.designer.ui.markers;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerListener;
import com.nextep.designer.core.services.IMarkerService;

public class MarkersContentProvider implements IStructuredContentProvider, IMarkerListener {

	private Collection<IMarker> markers;
	private TableViewer viewer;

	public MarkersContentProvider() {
		CorePlugin.getService(IMarkerService.class).addMarkerListener(this);
		markers = new ArrayList<IMarker>();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (markers != null) {
			return markers.toArray();
		} else {
			return null;
		}
	}

	@Override
	public void dispose() {
		CorePlugin.getService(IMarkerService.class).removeMarkerListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
		// Asking for full recomputation as this method is called during problems view first
		// initialization
		this.markers = CorePlugin.getService(IMarkerService.class).getCachedMarkers();
	}

	@Override
	public void markersChanged(final Object o, final Collection<IMarker> oldMarkers,
			final Collection<IMarker> newMarkers) {
		final IMarkerService markerService = CorePlugin.getService(IMarkerService.class);
		this.markers = markerService.getCachedMarkers();
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}

	@Override
	public void markersReset(Collection<IMarker> allMarkers) {
		this.markers = new ArrayList<IMarker>(allMarkers);
		if (viewer != null && !viewer.getTable().isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					viewer.refresh(true);
				}
			});
		}
	}
}
