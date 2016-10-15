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
package com.nextep.designer.vcs.ui.decorators;

import java.util.Collection;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.model.ITypedNode;

/**
 * An example showing how to control when an element is decorated. This example decorates only
 * elements that are instances of IResource and whose attribute is 'Read-only'.
 * 
 * @see ILightweightLabelDecorator
 */
public class MarkersDecorator implements ILightweightLabelDecorator {

	/**
	 * The image description used in <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor errorDescriptor;
	private ImageDescriptor warningDescriptor;
	private final static int quadrant = IDecoration.BOTTOM_LEFT;

	public MarkersDecorator() {
		errorDescriptor = ImageDescriptor.createFromImage(ImageFactory.ICON_ERROR_DECO_TINY);
		warningDescriptor = ImageDescriptor.createFromImage(ImageFactory.ICON_WARNING_DECO_TINY);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 * org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof ITypedNode) {
			final ITypedNode node = (ITypedNode) element;
			final Collection<ITypedObject> nodeChildren = node.getChildren();
			decorateIfAnyMarker(decoration, nodeChildren);
		} else if (element instanceof IVersionContainer) {
			final Collection<IVersionable<?>> versionables = ((IVersionContainer) element)
					.getContents();
			decorateIfAnyMarker(decoration, versionables);
		} else if (element instanceof ITypedObject) {
			Collection<IMarker> markers = Designer.getMarkerProvider().getMarkersFor(
					(ITypedObject) element);
			addDecoration(markers, decoration);
		}
	}

	private void addDecoration(Collection<IMarker> markers, IDecoration decoration) {
		MarkerType markerType = null;
		// looking for the highest level of markers
		for (IMarker m : markers) {
			markerType = m.getMarkerType();
			if (markerType == MarkerType.ERROR) {
				break;
			}
		}
		// Decorating
		if (markerType != null) {
			switch (markerType) {
			case ERROR:
				decoration.addOverlay(errorDescriptor, quadrant);
				break;
			case WARNING:
				decoration.addOverlay(warningDescriptor, quadrant);
				break;
			}
		}
	}

	/**
	 * Adds the error decoration as soon as one element of the specified collection has a marker
	 * 
	 * @param decoration decoration to alter
	 * @param elements elements to check for markers
	 */
	private void decorateIfAnyMarker(IDecoration decoration, Collection<?> elements) {
		final IMarkerService markerService = CorePlugin.getService(IMarkerService.class);
		for (Object o : elements) {
			final Collection<IMarker> markers = markerService.getMarkersFor(o);
			if (markers.size() > 0) {
				addDecoration(markers, decoration);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.
	 * ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 * java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.
	 * ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}
