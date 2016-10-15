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

import java.util.Date;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.RepositoryUser;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;
import com.nextep.designer.vcs.ui.impl.ExceptionHandler;

/**
 * This workbench advisor creates the window advisor, and specifies the perspective id for the
 * initial window.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "com.neXtep.Designer.perspective"; //$NON-NLS-1$
	private static final String PROP_REPO_USER = "repo.user"; //$NON-NLS-1$
	private static final String PROP_REPO_PASSWORD = "repo.password"; //$NON-NLS-1$
	private static final String PROP_REPO_DATABASE = "repo.database"; //$NON-NLS-1$
	private static final String PROP_REPO_INSTANCE = "repo.instance"; //$NON-NLS-1$
	private static final String PROP_REPO_PORT = "repo.port"; //$NON-NLS-1$
	private static final String PROP_REPO_SERVER = "repo.server"; //$NON-NLS-1$
	private static final String PROP_REPO_VENDOR = "repo.vendor"; //$NON-NLS-1$

	private IWorkbenchConfigurer configurer;

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	@Override
	public void postShutdown() {
		super.postShutdown();
		FontFactory.dispose();
		ImageFactory.dispose();
	}

	@Override
	public void eventLoopException(Throwable exception) {
		ExceptionHandler.handle(exception);
		super.eventLoopException(exception);
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		this.configurer = configurer;
		configurer.setSaveAndRestore(false);
		PlatformUI.getPreferenceStore().setValue("SHOW_PROGRESS_ON_STARTUP", true);

		// Initializing repository connection
		final IConnection c = getRepositoryConnection();
		CorePlugin.getRepositoryService().setRepositoryConnection(c);

		final IWorkspaceService viewService = VCSPlugin.getViewService();
		viewService
				.setCurrentUser(new RepositoryUser("SYSTEM", "MANAGER", "System", "JUnit tests"));
		viewService.getCurrentUser().setUID(new UID(1));
		IWorkspace view = viewService.createWorkspace();
		view.setName("JUnit ");
		view.setDescription(new Date().toString());
		CorePlugin.getIdentifiableDao().save(view);
		viewService.setCurrentWorkspace(view);
	}

	private IConnection getRepositoryConnection() {
		final IConnection conn = CorePlugin.getTypedObjectFactory().create(IConnection.class);
		final IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();

		final String vendor = prefs.getString(PROP_REPO_VENDOR);
		final String user = prefs.getString(PROP_REPO_USER);
		final String password = prefs.getString(PROP_REPO_PASSWORD);
		final String server = prefs.getString(PROP_REPO_SERVER);
		final String port = prefs.getString(PROP_REPO_PORT);
		final String instance = prefs.getString(PROP_REPO_INSTANCE);
		final String database = prefs.getString(PROP_REPO_DATABASE);

		conn.setDBVendor(DBVendor.valueOf(vendor));
		conn.setLogin(user);
		conn.setPassword(password);
		conn.setPasswordSaved(true);
		conn.setServerIP(server);
		conn.setServerPort(port);
		conn.setInstance(instance);
		conn.setDatabase(database);
		conn.setSchema(""); //$NON-NLS-1$

		return conn;
	}

	@Override
	public void postStartup() {
		super.postStartup();
		configurer.setSaveAndRestore(true);
	}

}
