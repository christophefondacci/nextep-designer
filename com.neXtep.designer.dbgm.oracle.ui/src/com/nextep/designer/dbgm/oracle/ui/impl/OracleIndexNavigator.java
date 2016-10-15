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

import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.gui.navigators.IndexNavigator;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.designer.core.preferences.DesignerCoreConstants;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.oracle.model.IOracleIndex;
import com.nextep.designer.dbgm.oracle.ui.DBOMImages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public class OracleIndexNavigator extends IndexNavigator {

	public OracleIndexNavigator(IOracleIndex index, ITypedObjectUIController controller) {
		super(index, controller);
	}

	@Override
	public void initializeChildConnectors() {
		super.initializeChildConnectors();
		IOracleIndex index = (IOracleIndex) getModel();
		if (index.getPhysicalProperties() != null) {
			addConnector(UIControllerFactory.getController(index.getPhysicalProperties().getType())
					.initializeNavigator(index.getPhysicalProperties()));
			// Forcing consistency
			if (index.getPhysicalProperties().getParent() != index) {
				try {
					Observable.deactivateListeners();
					index.getPhysicalProperties().setParent(index);
				} finally {
					Observable.activateListeners();
				}

			}
		}
	}

	public Image getConnectorIcon() {
		final IOracleIndex i = (IOracleIndex) getModel();
		final IPhysicalProperties props = (IPhysicalProperties) i.getPhysicalProperties();
		if (props instanceof IPartitionable) {
			final IPartitionable partitionable = (IPartitionable) props;
			if (partitionable != null
					&& partitionable.getPartitioningMethod() != PartitioningMethod.NONE) {
				if (Designer.getInstance().getPropertyBool(DesignerCoreConstants.ICON_TINY)) {
					return DBOMImages.ICON_IDX_PARTITION_TINY;
				} else {
					return DBOMImages.ICON_IDX_PARTITION;
				}
			}
		}
		return super.getConnectorIcon();
	}
	// /**
	// * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
	// */
	// @Override
	// public void setModel(Object model) {
	// // Only for initialized connectors
	// if( getSWTConnector() != null ){
	// // Handling the case when the new table has physical properties
	// // while old table model doesn't.
	// IOracleIndex oldIndex = (IOracleIndex)getModel();
	// IOracleIndex newIndex = (IOracleIndex)model;
	// if( newIndex != null && newIndex.getPhysicalProperties()!=null && (oldIndex == null ||
	// (oldIndex != null && oldIndex.getPhysicalProperties()==null ))) {
	// // We add the navigator for physicals.
	// addConnector(ControllerFactory.getController(newIndex.getPhysicalProperties().getType()).initializeNavigator(newIndex.getPhysicalProperties()));
	// refreshConnector();
	// }
	// }
	// // Setting standard model
	// super.setModel(model);
	// }
}
