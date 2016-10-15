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
package com.nextep.designer.vcs.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.model.DependencyMode;
import com.nextep.designer.vcs.ui.model.IDependencySearchRequest;
import com.nextep.designer.vcs.ui.services.IDependencyUIService;

/**
 * This handler toggles the mode of computation of dependencies. Typically, it responds to a user
 * action who decided to change from "reverse" dependencies computation to "direct" dependencies
 * computation
 * 
 * @author Christophe Fondacci
 */
public class ToggleDependencyModeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.matchesRadioState(event)) {
			return null;
		}
		// Retrieving selected synchronization mode
		final String mode = event.getParameter(RadioState.PARAMETER_ID);

		// Retriveing current dependency request (not trusting HandlerUtil for active part : buggy)
		final IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActivePart();
		if (part instanceof CommonNavigator) {
			CommonViewer viewer = ((CommonNavigator) part).getCommonViewer();
			if (viewer.getInput() instanceof IDependencySearchRequest) {
				final IDependencySearchRequest request = (IDependencySearchRequest) viewer
						.getInput();

				// Computing new dependency mode
				DependencyMode type = DependencyMode.valueOf(mode);
				final IDependencyUIService service = getDependencyUIService();
				service.setDependencyMode(type);
				getDependencyUIService().computeDependencies(request.getElement());
				HandlerUtil.updateRadioState(event.getCommand(), mode);
			}
		}
		return null;
	}

	private IDependencyUIService getDependencyUIService() {
		return VCSUIPlugin.getService(IDependencyUIService.class);
	}
}
