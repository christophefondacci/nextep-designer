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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;
import com.nextep.designer.vcs.model.ComparisonScope;

public class ChangeSynchronizationModeHandler extends AbstractHandler {

	private final static String SYNCH_MODE_CMD_ID = "com.neXtep.designer.synch.ui.synchMode"; //$NON-NLS-1$
	private final static Log LOGGER = LogFactory.getLog(ChangeSynchronizationModeHandler.class);

	public ChangeSynchronizationModeHandler() {
		getSynchronizationService().addSynchronizationListener(new ISynchronizationListener() {

			@Override
			public void scopeChanged(ComparisonScope scope) {
				toggleScope(scope);
			}

			@Override
			public void newSynchronization(ISynchronizationResult synchronizationResult) {
				toggleScope(synchronizationResult.getComparisonScope());
			}

			private void toggleScope(ComparisonScope scope) {
				final ICommandService cmdService = (ICommandService) PlatformUI.getWorkbench()
						.getService(ICommandService.class);
				final Command cmd = cmdService.getCommand(SYNCH_MODE_CMD_ID);
				try {
					HandlerUtil.updateRadioState(cmd, scope.name());
				} catch (ExecutionException e) {
					LOGGER.error(SynchUIMessages.getString("synch.handler.toggleScopeFailed"), e); //$NON-NLS-1$
				}
			}
		});
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.matchesRadioState(event)) {
			return null;
		}
		// Retrieving selected synchronization mode
		final String mode = event.getParameter(RadioState.PARAMETER_ID);
		// Retrieving current synchronization result
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		final ISynchronizationResult result = (ISynchronizationResult) part
				.getAdapter(ISynchronizationResult.class);
		if (result != null) {
			ComparisonScope newComparisonScope = null;
			if (ComparisonScope.DB_TO_REPOSITORY.name().equals(mode)) {
				newComparisonScope = ComparisonScope.DB_TO_REPOSITORY;
			} else if (ComparisonScope.DATABASE.name().equals(mode)) {
				newComparisonScope = ComparisonScope.DATABASE;
			}
			if (newComparisonScope != null) {
				getSynchronizationService().changeSynchronizationScope(newComparisonScope, result,
						null);
			}
		}
		// HandlerUtil.updateRadioState(event.getCommand(), mode);
		return null;
	}

	ISynchronizationUIService getSynchronizationService() {
		return SynchUIPlugin.getService(ISynchronizationUIService.class);
	}

}
