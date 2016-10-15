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
package com.nextep.designer.dbgm.oracle.ui.controllers;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.external.MaterializedViewLogPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLogPhysicalProperties;
import com.nextep.designer.dbgm.oracle.ui.impl.MaterializedViewLogPhysicalPropertiesEditor;
import com.nextep.designer.dbgm.oracle.ui.impl.PhysicalPropertiesNavigator;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

public class MaterializedViewLogPhysicalPropertiesUIController extends AbstractUIController {

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new MaterializedViewLogPhysicalPropertiesEditor(
				(IMaterializedViewLogPhysicalProperties) content, this);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new PhysicalPropertiesNavigator((IPhysicalProperties) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object newInstance(Object parent) {
		IPhysicalObject i = (IPhysicalObject) parent;
		// Ensuring modifiable parent index
		i = VCSUIPlugin.getVersioningUIService().ensureModifiable(i);
		// Checking pre-existence of physical properties
		if (i.getPhysicalProperties() != null) {
			throw new ErrorException(
					"Physical properties are already defined for this materialized view log and you can only have one definition. Please edit existing definition instead.");
		}
		// We're clear, creating new instance
		IPhysicalProperties props = new MaterializedViewLogPhysicalProperties();
		props.setParent((IMaterializedViewLog) i);
		newWizardEdition("Creating new physical properties...", initializeEditor(props));
		i.setPhysicalProperties(props);
		// Saving
		save(props);
		// Returning new created instance
		return i;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object emptyInstance(String name, Object parent) {
		IPhysicalObject i = (IPhysicalObject) parent;
		// Ensuring modifiable parent index
		i = VCSUIPlugin.getVersioningUIService().ensureModifiable(i);
		// Checking pre-existence of physical properties
		if (i.getPhysicalProperties() != null) {
			throw new ErrorException(
					"Physical properties are already defined for this materialized view log and you can only have one definition. Please edit existing definition instead.");
		}
		// We're clear, creating new instance
		IPhysicalProperties props = new MaterializedViewLogPhysicalProperties();
		props.setParent((IMaterializedViewLog) i);
		save(props);
		return props;
	}

}
