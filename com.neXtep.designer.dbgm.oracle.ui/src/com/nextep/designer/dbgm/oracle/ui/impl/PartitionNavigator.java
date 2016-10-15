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

import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.navigators.UnversionedNavigator;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PartitionNavigator extends UnversionedNavigator {

	public PartitionNavigator(IPartition p, ITypedObjectUIController controller) {
		super(VersionHelper.getVersionable(((IParentable<?>) p.getParent()).getParent()), p,
				controller);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();

	}

}
