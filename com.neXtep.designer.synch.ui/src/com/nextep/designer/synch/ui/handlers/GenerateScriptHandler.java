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
package com.nextep.designer.synch.ui.handlers;

import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.State;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;
import com.nextep.designer.vcs.model.ComparisonScope;

public class GenerateScriptHandler extends AbstractHandlerWithState implements
		ISynchronizationListener {

	private boolean enabled = true;
	private boolean stateRegistered = false;
	private ISynchronizationUIService synchService = null;

	public GenerateScriptHandler() {
		synchService = SynchUIPlugin.getService(ISynchronizationUIService.class);
		synchService.addSynchronizationListener(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		final ISynchronizationResult result = (ISynchronizationResult) part
				.getAdapter(ISynchronizationResult.class);
		if (result != null) {
			ISynchronizationUIService synchService = (ISynchronizationUIService) HandlerUtil
					.getActiveSiteChecked(event).getService(ISynchronizationUIService.class);
			// Building UI scripts
			synchService.buildScript(result);
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		if (!stateRegistered) {
			final ICommandService cmdService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);
			final String commandId = "com.neXtep.designer.synch.ui.synchMode"; //$NON-NLS-1$
			final Command cmd = cmdService.getCommand(commandId);
			final State state = cmd.getState(RadioState.STATE_ID);
			addState("synchMode", state); //$NON-NLS-1$
			stateRegistered = true;

		}
		return enabled;
	}

	@Override
	public void handleStateChange(State state, Object oldValue) {
		enabled = ComparisonScope.DATABASE.name().equals(state.getValue());
	}

	@Override
	public void dispose() {
		synchService.removeSynchronizationListener(this);
	}

	@Override
	public void newSynchronization(ISynchronizationResult synchronizationResult) {
	}

	@Override
	public void scopeChanged(ComparisonScope scope) {
		final HandlerEvent e = new HandlerEvent(this, true, false);
		fireHandlerChanged(e);
	}
}
