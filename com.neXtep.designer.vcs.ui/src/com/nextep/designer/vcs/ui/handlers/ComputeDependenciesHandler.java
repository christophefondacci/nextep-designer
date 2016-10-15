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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.navigators.DependenciesNavigator;
import com.nextep.designer.vcs.ui.services.IDependencyUIService;

public class ComputeDependenciesHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection && !s.isEmpty()) {
			Object o = ((IStructuredSelection) s).getFirstElement();
			if (o instanceof IReferenceable) {
				VCSUIPlugin.getService(IDependencyUIService.class).computeDependencies(
						(IReferenceable) o);
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
							DependenciesNavigator.VIEW_ID);
				} catch (PartInitException e) {
					throw new ErrorException(e);
				}
			}
		}
		return null;
	}

}
