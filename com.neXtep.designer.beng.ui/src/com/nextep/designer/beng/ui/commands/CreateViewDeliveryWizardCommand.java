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
package com.nextep.designer.beng.ui.commands;

import com.nextep.datadesigner.beng.gui.VersionViewDeliveriesEditor;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * A command which creates and return the view delivery wizard page to
 * integrate with the view creation wizard.
 * TODO: A proper implementation would have been to define an extension point
 * and to extend it from the beng.ui plugin.
 * Not enough time to do this yet...using the generic command extension instead.
 * @author Christophe
 *
 */
public class CreateViewDeliveryWizardCommand implements ICommand {

	@Override
	public Object execute(Object... parameters) {
		IWorkspace view = (IWorkspace)parameters[0];
		return new VersionViewDeliveriesEditor(view);
	}

	@Override
	public String getName() {
		return "Creating view delivery wizard page...";
	}

}
