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
package com.nextep.designer.synch.ui.decorators;

import java.util.Collection;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;
import com.nextep.designer.vcs.model.IVersionable;

public class DataSynchronizationDecorator implements ILightweightLabelDecorator {

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

		if (element instanceof IBasicTable) {
			if (descriptor == null) {
				descriptor = SynchUIPlugin.imageDescriptorFromPlugin(SynchUIPlugin.PLUGIN_ID,
						"/resource/datasync-deco.png"); //$NON-NLS-1$
			}
			quadrant = IDecoration.TOP_RIGHT;
			Collection<IVersionable<?>> tables = SynchUIPlugin.getService(
					ISynchronizationUIService.class).getDataSynchronizationTables();
			if (tables.contains(element)) {
				decoration.addOverlay(descriptor, quadrant);
			}

		}
	}

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

}
