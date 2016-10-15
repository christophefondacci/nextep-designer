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
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.ui.impl.ExceptionHandler;

/**
 * This workbench advisor creates the window advisor, and specifies the perspective id for the
 * initial window.
 * 
 * @author Christophe Fondacci
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static Log log = LogFactory.getLog(ApplicationWorkbenchAdvisor.class);
	private static final String PERSPECTIVE_ID = "com.neXtep.Designer.perspective"; //$NON-NLS-1$

	private IWorkbenchConfigurer configurer;

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	/**
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#postShutdown()
	 */
	@Override
	public void postShutdown() {
		super.postShutdown();
		FontFactory.dispose();
		ImageFactory.dispose();
	}

	/**
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#eventLoopException(java.lang.Throwable)
	 */
	@Override
	public void eventLoopException(Throwable exception) {
		log.debug("Exception from the event loop: " + exception.getMessage(), exception); //$NON-NLS-1$
		ExceptionHandler.handle(exception);
		super.eventLoopException(exception);
	}

	/**
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
	 */
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		log.debug("Initializing configurer"); //$NON-NLS-1$
		super.initialize(configurer);
		this.configurer = configurer;
		configurer.setSaveAndRestore(false);
	}

	@Override
	public void postStartup() {
		log.debug("Entering postStartup"); //$NON-NLS-1$
		super.postStartup();
		configurer.setSaveAndRestore(true);
	}
}
