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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.services.IReverseSynchronizationUIService;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;

public class RunSynchronizationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		final ISynchronizationResult result = (ISynchronizationResult) part
				.getAdapter(ISynchronizationResult.class);
		if (result != null) {
			switch (result.getComparisonScope()) {
			case DB_TO_REPOSITORY:
				final IReverseSynchronizationUIService reverseService = getReverseSynchronizationService();
				reverseService.reverseSynchronize(result, null);
				break;
			case DATABASE:
				getSynchronizationService().submit(result);
				break;
			}
		}

		return null;
	}

	private IReverseSynchronizationUIService getReverseSynchronizationService() {
		return SynchUIPlugin.getService(IReverseSynchronizationUIService.class);
	}

	private ISynchronizationUIService getSynchronizationService() {
		return SynchUIPlugin.getService(ISynchronizationUIService.class);
	}
}
