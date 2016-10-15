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
package com.nextep.designer.sqlclient.ui.model.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.rcp.SQLFullClientEditor;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;

public class SQLClientEditorTracker implements IPartListener {

	private final static Log LOGGER = LogFactory.getLog(SQLClientEditorTracker.class);

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof SQLFullClientEditor) {
			final IEditorPart editor = (IEditorPart) part;
			final IEditorInput input = editor.getEditorInput();
			final IWorkbenchPart view = SQLClientPlugin.getService(ISQLClientService.class)
					.getSQLResultViewFor(input, false);
			final IViewSite site = (IViewSite) view.getSite();
			// Async exec here to avoid recursive activation problem
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.showView(site.getId(), site.getSecondaryId(),
										IWorkbenchPage.VIEW_VISIBLE);
					} catch (PartInitException e) {
						LOGGER.error(
								"Unable to activate corresponding SQL results view: "
										+ e.getMessage(), e);
					}
				}
			});
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

}
