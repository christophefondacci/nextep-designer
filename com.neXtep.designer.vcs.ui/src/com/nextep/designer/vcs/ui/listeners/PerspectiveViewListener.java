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
package com.nextep.designer.vcs.ui.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.base.AbstractWorkspaceListener;

/**
 * This view listener handles closure of all opened editors on view closure and perspective close /
 * reopen on view opening.
 * 
 * @author Christophe Fondacci
 */
public class PerspectiveViewListener extends AbstractWorkspaceListener {

	private final static Log log = LogFactory.getLog(PerspectiveViewListener.class);

	@Override
	public void workspaceChanged(IWorkspace oldView, IWorkspace newView, IProgressMonitor monitor) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchPage page = null;
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
					page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					if (page != null) {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.closeAllPerspectives(false, true);
					}
					try {
						PlatformUI.getWorkbench().showPerspective(
								"com.neXtep.Designer.perspective", //$NON-NLS-1$
								PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					} catch (WorkbenchException e) {
						log.error(e);
					}

				}
			}
		});
	}

	@Override
	public void workspaceClosed(IWorkspace view) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				// Handling perspective closure if a perspective is already opened
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
					final IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					if (page != null) {
						if (!PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.closeAllEditors(true)) {
							return;
						}
					}
				}
			}
		});
	}
}
