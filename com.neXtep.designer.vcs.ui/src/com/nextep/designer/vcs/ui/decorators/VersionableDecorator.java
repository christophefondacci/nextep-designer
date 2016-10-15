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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * An example showing how to control when an element is decorated. This example decorates only
 * elements that are instances of IResource and whose attribute is 'Read-only'.
 * 
 * @see ILightweightLabelDecorator
 */
public class VersionableDecorator implements ILightweightLabelDecorator {

	/** The integer value representing the placement options */
	private int quadrant;

	/**
	 * The image description used in <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor descriptor;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 * org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {

		if (element instanceof IVersionable<?>) {
			if (((IVersionable<?>) element).getVersion().getStatus() == IVersionStatus.CHECKED_IN) {
				if (descriptor == null) {
					descriptor = ImageDescriptor.createFromImage(ImageFactory.ICON_LOCK_TINY);
				}
				quadrant = IDecoration.BOTTOM_RIGHT;
				decoration.addOverlay(descriptor, quadrant);

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
