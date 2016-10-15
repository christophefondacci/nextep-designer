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
package com.nextep.designer.sqlgen.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.services.IGenerationListener;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public class ShowIncrementalDDLHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) selection;
			final Object o = sel.getFirstElement();
			if (o instanceof IVersionable) {
				final IVersionable<?> versionable = (IVersionable<?>) o;
				final IVersionInfo initialVersion = VCSUIPlugin.getVersioningUIService()
						.pickPreviousVersion(versionable, null);
				Job j = new Job(SQLGenMessages.getString("service.generation.incremental")) { //$NON-NLS-1$

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						final IGenerationService generationService = CorePlugin
								.getService(IGenerationService.class);
						generationService.generateIncrement(new IGenerationListener() {

							@Override
							public void generationSucceeded(final ISQLScript result) {
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										UIControllerFactory.getController(result).defaultOpen(
												result);
									}
								});
							}

							@Override
							public void generationFailed(Throwable t, String message) {

							}
						}, monitor, initialVersion, versionable.getVersion());
						return Status.OK_STATUS;
					}
				};
				j.setUser(true);
				j.schedule();
			}
		}
		return null;
	}

}
