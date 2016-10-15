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
package com.nextep.designer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 * 
 * @author Christophe Fondacci
 */
public class Application implements IApplication {

	private final static Log log = LogFactory.getLog(Application.class);

	public Object start(IApplicationContext context) {
		// Adding debug logs here since when we got a configuration problem we fail in these early
		// steps.
		log.debug("Creating display..."); //$NON-NLS-1$
		Display display = PlatformUI.createDisplay();
		log.debug("Display OK, checking java version..."); //$NON-NLS-1$
		String javaVersion = System.getProperty("java.version"); //$NON-NLS-1$
		Float d = Float.parseFloat(javaVersion.substring(0, 3));
		if (d.floatValue() < 1.6f) {
			MessageDialog
					.openError(
							null,
							DesignerMessages.getString("application.javaTooOldError.title"), DesignerMessages.getString("application.javaTooOldError.message") + javaVersion + "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return IApplication.EXIT_OK;
		}
		try {
			log.debug("Creating workbench..."); //$NON-NLS-1$
			int returnCode = PlatformUI.createAndRunWorkbench(display,
					new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {

			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
