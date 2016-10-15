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

import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.EditorAreaDropAdapter;
import org.eclipse.ui.intro.IIntroManager;
import com.nextep.datadesigner.Designer;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.p2.P2Plugin;
import com.nextep.designer.p2.exceptions.UnavailableLicenseServerException;
import com.nextep.designer.p2.services.ILicenseService;
import com.nextep.designer.repository.RepositoryPlugin;
import com.nextep.designer.repository.RepositoryStatus;
import com.nextep.designer.repository.exception.NoRepositoryConnectionException;
import com.nextep.designer.repository.exception.NoRepositoryException;
import com.nextep.designer.repository.services.IRepositoryUpdaterService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * @author Christophe Fondacci
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private static final Log log = LogFactory.getLog(ApplicationWorkbenchAdvisor.class);
	private static final List<String> EXCLUDED_PREFERENCE_NODES = Arrays
			.asList(new String[] { "org.eclipse.team.ui.TeamPreferences" }); //$NON-NLS-1$

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public void preWindowOpen() {
		logInfo(DesignerMessages.getString("startup")); //$NON-NLS-1$
		super.preWindowOpen();
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setShowPerspectiveBar(true);
		// Initializing
		getViewService().changeWorkspace(getViewService().getCurrentWorkspace().getUID(),
				Designer.getProgressMonitor());
		configurer
				.configureEditorAreaDropListener(new EditorAreaDropAdapter(configurer.getWindow()));

		logInfo(DesignerMessages.getString("openWorkbench")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowCreate()
	 */
	@Override
	public void postWindowCreate() {
		super.postWindowCreate();

		// Removing the "Team" section from the preferences.
		PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
		IPreferenceNode[] rootSubNodes = pm.getRootSubNodes();

		for (IPreferenceNode node : rootSubNodes) {
			if (EXCLUDED_PREFERENCE_NODES.contains(node.getId())) {
				pm.remove(node);
				log.debug("Removed node [" + node.getId() + "] from preferences"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#postWindowOpen()
	 */
	@Override
	public void postWindowOpen() {
		super.postWindowOpen();
		log.info("neXtep Designer is ready!"); //$NON-NLS-1$
		final IConnection conn = CorePlugin.getRepositoryService().getRepositoryConnection();
		RepositoryStatus status = RepositoryStatus.REPOSITORY_TOO_OLD;
		try {
			status = RepositoryPlugin.getService(IRepositoryUpdaterService.class).checkRepository(
					conn);
		} catch (NoRepositoryException e) {
			// Nothing, we're too old and we'll exit
		} catch (NoRepositoryConnectionException e) {
			// Nothing, we're too old and we'll exit
		}

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		switch (status) {
		case CLIENT_TOO_OLD:
			MessageDialog
					.openInformation(
							shell,
							DesignerMessages.getString("windowAdvisor.softwareUpdate.title"), DesignerMessages.getString("windowAdvisor.softwareUpdate.message")); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				P2Plugin.getService(ILicenseService.class).checkForUpdates(false);
			} catch (UnavailableLicenseServerException e) {
				MessageDialog
						.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), "Update server unavailable",
								"The neXtep softwares update server is unavailable at the moment, please try again later.");
			}
			break;
		case REPOSITORY_TOO_OLD:
			MessageDialog
					.openWarning(
							shell,
							DesignerMessages.getString("windowAdvisor.repositoryUpdate.title"), DesignerMessages.getString("windowAdvisor.repositoryUpdate.message")); //$NON-NLS-1$ //$NON-NLS-2$
			PlatformUI.getWorkbench().close();
		}

		// Setting the Welcome page in fully visible mode if available.
		IIntroManager mgr = getWindowConfigurer().getWindow().getWorkbench().getIntroManager();
		mgr.setIntroStandby(mgr.getIntro(), false);
	}

	/**
	 * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor(org.eclipse.ui.application.IActionBarConfigurer)
	 */
	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new TextEditorActionBarAdvisor(configurer);
	}

	private static void logInfo(String message) {
		log.info(message);
		IProgressMonitor m = Designer.getProgressMonitor();
		if (m != null) {
			m.setTaskName(message);
		}
	}

	private IWorkspaceService getViewService() {
		return VCSPlugin.getViewService();
	}
}
