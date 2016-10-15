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
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;

public class UserLockDecorator implements ILightweightLabelDecorator {

	/** The integer value representing the placement options */
	private int quadrant;

	/**
	 * The image description used in <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor descriptor;

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IVersionable<?>) {
			final IVersionable<?> versionable = (IVersionable<?>) element;
			final IVersionInfo version = versionable.getVersion();
			if (version.getStatus() != IVersionStatus.CHECKED_IN
					&& versionable.getType() != IElementType.getInstance(IVersionContainer.TYPE_ID)) {
				if (version.getUser() != VCSPlugin.getViewService().getCurrentUser()) {
					if (descriptor == null) {
						descriptor = ImageDescriptor
								.createFromImage(ImageFactory.ICON_USER_LOCK_TINY);
					}
					quadrant = IDecoration.BOTTOM_RIGHT;
					decoration.addOverlay(descriptor, quadrant);
				}

			}
		}
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
