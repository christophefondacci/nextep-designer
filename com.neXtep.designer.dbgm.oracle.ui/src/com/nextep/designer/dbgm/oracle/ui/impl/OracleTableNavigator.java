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
import com.nextep.datadesigner.dbgm.gui.navigators.TableNavigator;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.designer.core.preferences.DesignerCoreConstants;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.oracle.impl.OracleUniqueConstraint;
import com.nextep.designer.dbgm.oracle.impl.OracleUniqueKeyIndexWrapper;
import com.nextep.designer.dbgm.oracle.model.IOracleTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.ui.DBOMImages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public class OracleTableNavigator extends TableNavigator {

	public OracleTableNavigator(IOracleTable table, ITypedObjectUIController controller) {
		super(table, controller);
	}

	@Override
	public void initializeChildConnectors() {
		super.initializeChildConnectors();
		IOracleTable table = (IOracleTable) getModel();
		final IOracleTablePhysicalProperties props = (IOracleTablePhysicalProperties) table
				.getPhysicalProperties();
		if (props != null) {
			addConnector(UIControllerFactory.getController(props.getType()).initializeNavigator(
					props));
			// Forcing consistency
			if (props.getParent() != table) {
				try {
					Observable.deactivateListeners();
					props.setParent(table);
				} finally {
					Observable.activateListeners();
				}
			}
		}
		// Initializing check constraints
		for (ICheckConstraint c : table.getCheckConstraints()) {
			addConnector(UIControllerFactory.getController(c.getType()).initializeNavigator(c));
			// Cannot remember why i am doing this, but i think there might be a bug if i do not
			// TODO : check why relinking parent
			if (c.getConstrainedTable() != table) {
				c.setConstrainedTable(table);
			}
		}
	}

	@Override
	protected IIndex wrapUniqueKey(UniqueKeyConstraint uk) {
		return new OracleUniqueKeyIndexWrapper((OracleUniqueConstraint) uk);
	}

	@Override
	public Image getConnectorIcon() {
		final IOracleTable t = (IOracleTable) getModel();
		final IOracleTablePhysicalProperties props = (IOracleTablePhysicalProperties) t.getPhysicalProperties();
		if (props != null && props.getPartitioningMethod() != PartitioningMethod.NONE) {
			if (Designer.getInstance().getPropertyBool(DesignerCoreConstants.ICON_TINY)) {
				return DBOMImages.ICON_TAB_PARTITION_TINY;
			} else {
				return DBOMImages.ICON_TAB_PARTITION;
			}
		}
		return super.getConnectorIcon();
	}

	@Override
	public void refreshConnector() {
		super.refreshConnector();
	}

}
