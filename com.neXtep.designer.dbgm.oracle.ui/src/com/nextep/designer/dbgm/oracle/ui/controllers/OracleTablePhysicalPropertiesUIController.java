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

import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.vcs.gui.external.VersionableDisplayDecorator;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.ui.impl.PhysicalPropertiesNavigator;
import com.nextep.designer.dbgm.oracle.ui.impl.TablePhysicalPropertiesEditor;
import com.nextep.designer.dbgm.ui.controllers.TablePhysicalPropertiesUIController;

/**
 * @author Christophe Fondacci
 */
public class OracleTablePhysicalPropertiesUIController extends TablePhysicalPropertiesUIController {

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		IOracleTablePhysicalProperties props = (IOracleTablePhysicalProperties) content;
		return new VersionableDisplayDecorator(new TablePhysicalPropertiesEditor(props, this),
				VersionHelper.getVersionable(props.getParent()));
	}

	/**
	 * @see com.nextep.datadesigner.ctrl.IGe nericController#initializeNavigator(java.lang.Object)
	 */
	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new PhysicalPropertiesNavigator((IPhysicalProperties) model, this);
	}

}
