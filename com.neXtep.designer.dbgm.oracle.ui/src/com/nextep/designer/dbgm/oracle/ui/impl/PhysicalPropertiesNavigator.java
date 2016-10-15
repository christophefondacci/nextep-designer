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
package com.nextep.designer.dbgm.oracle.ui.impl;

import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.navigators.UnversionedNavigator;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PhysicalPropertiesNavigator extends UnversionedNavigator {

	public PhysicalPropertiesNavigator(IPhysicalProperties props,
			ITypedObjectUIController controller) {
		super(VersionHelper.getVersionable(props.getParent()), props, controller);
	}

	@Override
	public void initializeChildConnectors() {
		IPhysicalProperties props = (IPhysicalProperties) getModel();

		if (props instanceof IPartitionable) {
			for (IPartition p : ((IPartitionable) props).getPartitions()) {
				addConnector(UIControllerFactory.getController(p.getType()).initializeNavigator(p));
				// Forcing consistency
				if (p.getParent() != props) {
					try {
						Observable.deactivateListeners();
						p.setParent((IPartitionable) props);
					} finally {
						Observable.activateListeners();
					}
				}
			}
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case PARTITION_ADDED:
			this.addConnector(UIControllerFactory.getController(data).initializeNavigator(data));
			break;
		case PARTITION_REMOVED:
			this.removeConnector(this.getConnector(data));
			break;
		}
		refreshConnector();
	}

}
