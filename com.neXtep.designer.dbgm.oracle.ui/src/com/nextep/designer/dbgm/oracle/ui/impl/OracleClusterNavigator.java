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

import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class OracleClusterNavigator extends TypedNavigator {

	public OracleClusterNavigator(IOracleCluster cluster, ITypedObjectUIController controller) {
		super(cluster, controller);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case INDEX_REMOVED:
		case GENERIC_CHILD_REMOVED:
			removeConnector(getConnector(data));
			break;
		case INDEX_ADDED:
		case GENERIC_CHILD_ADDED:
			addConnector(UIControllerFactory.getController(((ITypedObject) data).getType())
					.initializeNavigator(data));
			break;
		}
		refreshConnector();
	}

	@Override
	public void initializeChildConnectors() {
		final IOracleCluster cluster = (IOracleCluster) getModel();
		for (IOracleClusteredTable t : cluster.getClusteredTables()) {
			addConnector(UIControllerFactory.getController(t.getType()).initializeNavigator(t));
		}
		for (IIndex i : cluster.getIndexes()) {
			addConnector(UIControllerFactory.getController(i.getType()).initializeNavigator(i));
		}
		final IPhysicalProperties p = cluster.getPhysicalProperties();
		if (p != null) {
			addConnector(UIControllerFactory.getController(p.getType()).initializeNavigator(p));
		}

	}

	@Override
	public void refreshConnector() {
		// TODO Auto-generated method stub
		super.refreshConnector();
	}
}
