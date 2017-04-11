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
/**
 *
 */
package com.nextep.designer.vcs.ui.services;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.hibernate.HibernateException;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.impl.DefaultWizard;
import com.nextep.datadesigner.gui.model.IConnector;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.gui.UserCreationWizard;
import com.nextep.datadesigner.vcs.gui.dialog.RepositoryUpgradeWizardPage;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.repository.RepositoryPlugin;
import com.nextep.designer.repository.RepositoryStatus;
import com.nextep.designer.repository.exception.NoRepositoryConnectionException;
import com.nextep.designer.repository.exception.NoRepositoryException;
import com.nextep.designer.repository.services.IRepositoryUpdaterService;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.editors.RepositoryConnectionEditor;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.ui.preferences.DesignerUIConstants;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.impl.Workspace;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.dialogs.RepositoryInstallerMonitorPage;

/**
 * @author Christophe Fondacci
 */
public final class VersionUIHelper {

	private static final Log LOGGER = LogFactory.getLog(VersionUIHelper.class);

	private VersionUIHelper() {
	}

	/**
	 * Retrieves the currently selected versionable or <code>null</code> if no IVersionable object
	 * is currently selected .
	 * 
	 * @param window active window or event window
	 * @return the IVErsionable object currently being selected
	 */
	@SuppressWarnings("unchecked")
	public static List<IVersionable<?>> getSelectedVersionable(IWorkbenchWindow window) {
		List<?> models = UIHelper.getSelectedModel(window);
		if (models != null) {
			for (Object o : models) {
				if (!(o instanceof IVersionable)) {
					return Collections.emptyList();
				}
			}
		}
		return (List<IVersionable<?>>) models;
	}

	/**
	 * Changes the current version view (workarea). This method handles the view switch at the UI
	 * layer and will therefore handle any opened editor / perspective. The raw view flush / load is
	 * delegated to the {@link VersionHelper#changeWorkspace(UID)} method.
	 * 
	 * @param viewID new view ID to load.
	 */
	public static void changeView(UID viewID) {
		VCSUIPlugin.getService(IWorkspaceUIService.class).changeWorkspace(viewID);
		// List<ICommand> cmds = new ArrayList<ICommand>();
		// boolean openPerspective = false;
		// // Handling perspective closure if a perspective is already opened
		// if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
		// final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		// .getActivePage();
		// if (page != null) {
		// openPerspective = true;
		// if (!PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
		// .closeAllEditors(true)) {
		// return;
		// }
		// }
		// }
		// // Delegating load to the VersionHelper
		// cmds.addAll(VersionHelper.getChangeViewCommands(viewID));
		//
		// // Opening the perspective if we have previously closed it
		// if (openPerspective) {
		// cmds.add(new ICommand() {
		//
		// @Override
		// public Object execute(Object... parameters) {
		// try {
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
		// .closeAllPerspectives(false, true);
		// PlatformUI.getWorkbench().showPerspective(
		// "com.neXtep.Designer.perspective",
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		// Designer.getMarkerProvider().invalidate();
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
		// .showView(ProblemsView.VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
		//
		// // Job j = new CommandJob("Synchronizing with database.",new
		// // RefreshSynchStatusCommand());
		// // j.schedule();
		// } catch (WorkbenchException e) {
		// log.error(e);
		// }
		// return null;
		// }
		//
		// @Override
		// public String getName() {
		// return VCSUIMessages.getString("openingNewView");
		// }
		// });
		// }
		//
		// if (openPerspective) {
		// CommandProgress.runWithProgress(false, cmds.toArray(new ICommand[cmds.size()]));
		// } else {
		// for (ICommand cmd : cmds) {
		// cmd.execute();
		// }
		// }
		// // Saving last opened view for proper restore of the workbench
		// Designer.getInstance().setProperty(DesignerCoreConstants.LAST_OPENED_VIEW,
		// viewID.toString());
	}

	/**
	 * Changes the password of this repository user
	 * 
	 * @param user repository user for password update
	 * @param newPassword new password to assign to this user
	 */
	public static void changeUserPassword(IRepositoryUser user, String newPassword) {
		boolean confirm = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(),
				"Change user password",
				MessageFormat.format(VCSUIMessages.getString("userConfirmChangePassword"), //$NON-NLS-1$
						user.getName(), newPassword));
		if (confirm) {
			try {
				/*
				 * Columns names in the SET clause cannot be qualified with an alias name because it
				 * would fail in Postgres.
				 */
				HibernateUtil.getInstance().getSession()
						.createSQLQuery("UPDATE {h-schema}rep_users ru " //$NON-NLS-1$
								+ "  SET password = ? " //$NON-NLS-1$
								+ "WHERE ru.user_id = ? ").setString(0, newPassword) //$NON-NLS-1$
						.setLong(1, user.getUID().rawId()).executeUpdate();
			} catch (HibernateException e) {
				LOGGER.error("Error while updating user password", e);
				throw e;
			}
			LOGGER.info("Password changed for user '" + user.getName() + "'");
		} else {
			LOGGER.info("Aborted password change.");
		}
	}

	/**
	 * Retrieves the repository database status.
	 * 
	 * @return a {@link RepositoryStatus} enumeration representing our ability to connect to a
	 *         proper repository database.
	 */
	private static RepositoryStatus getRepositoryStatus(IDatabaseConnector dbConnector) {
		/*
		 * FIXME [BGA] The DatabaseConnector that is used to log the repository to which we are
		 * connecting is not the same as the DatabaseConnector that is actually used to connect the
		 * repository, i.e the dbConnector parameter passed in is not used to connect. Either it
		 * should be removed, either the local variable repoConn should be removed.
		 */
		final IConnection repoConn = CorePlugin.getRepositoryService().getRepositoryConnection();
		final IProgressMonitor monitor = Designer.getProgressMonitor();
		if (monitor != null) {
			monitor.setTaskName("Connecting to repository ["
					+ dbConnector.getConnectionURL(repoConn) + "]...");
		}
		RepositoryStatus repositoryStatus;
		// Checking release
		try {
			repositoryStatus = getRepositoryUpdaterService().checkRepository(repoConn);
		} catch (NoRepositoryException e) {
			repositoryStatus = RepositoryStatus.NO_REPOSITORY;
		} catch (NoRepositoryConnectionException e) {
			repositoryStatus = RepositoryStatus.NO_CONNECTION;
		}
		return repositoryStatus;
	}

	private static IRepositoryUpdaterService getRepositoryUpdaterService() {
		return RepositoryPlugin.getService(IRepositoryUpdaterService.class);
	}

	/**
	 * This method handles the startup of the versioning features. It handles the check of the
	 * repository connection, its version, user setup and displays the appropriate wizards when
	 * needed.
	 * 
	 * @return <code>true</code> if startup requirements are ok, <code>false</code> otherwise. Users
	 *         should not go on with the startup of neXtep if this method returns false.
	 */
	public static boolean startup() {
		final List<IWizardPage> pages = new ArrayList<IWizardPage>();
		final IDatabaseConnector dbConnector = CoreUiPlugin.getRepositoryUIService()
				.getRepositoryConnector();
		final RepositoryStatus repositoryStatus = getRepositoryStatus(dbConnector);

		// Enabling controls
		switch (repositoryStatus) {
		case NO_CONNECTION:
			pages.add(new RepositoryUpgradeWizardPage(repositoryStatus));
			pages.add(new RepositoryConnectionEditor());
			break;
		case NO_REPOSITORY:
			pages.add(new RepositoryUpgradeWizardPage(repositoryStatus));
			pages.add(new RepositoryInstallerMonitorPage(dbConnector));
			pages.add(new UserCreationWizard());
			break;
		case REPOSITORY_TOO_OLD:
			pages.add(new RepositoryUpgradeWizardPage(repositoryStatus));
			pages.add(new RepositoryInstallerMonitorPage(dbConnector));
			break;
		case CLIENT_TOO_OLD:
			// update manager cannot work here because the platform is not yet running
			// UpdateManagerUI.openInstaller(statusLabel.getShell());
			IWorkspace view = new Workspace("Update view", ""); //$NON-NLS-2$
			view.setId(1);
			VersionHelper.setCurrentView(view);
			break;
		}

		// Wizarding only if there's something to do...
		if (pages.size() > 0) {
			final Shell shell = Display.getDefault().getActiveShell();
			try {
				final String title = VCSUIMessages.getString("repositoryUpgradeTitle"); //$NON-NLS-1$
				Wizard wiz = new DefaultWizard(title) {

					@Override
					public boolean canFinish() {
						for (IWizardPage page : getPages()) {
							if (!page.isPageComplete()) {
								return false;
							}
						}
						return true;
					}

					@Override
					public boolean performCancel() {
						if (getContainer().getCurrentPage() instanceof RepositoryInstallerMonitorPage) {
							return false;
						}
						return super.performCancel();
					}
				};// title,pages);

				WizardDialog d = new WizardDialog(shell, wiz) {

					@Override
					public void updateButtons() {
						super.updateButtons();
						if (getCurrentPage() instanceof RepositoryInstallerMonitorPage) {
							getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
						}
					}
				};
				d.setHelpAvailable(true);
				d.setTitle(title);
				d.setBlockOnOpen(true);
				for (IWizardPage page : pages) {
					wiz.addPage(page);
				}

				// This setting overrides the computed size which might not be enough to display
				// correctly all controls
				d.setMinimumPageSize(500, 300);

				d.open();
				if (d.getReturnCode() == Window.OK) {
					final IDatabaseConnector repoConnector = CoreUiPlugin.getRepositoryUIService()
							.getRepositoryConnector();
					final RepositoryStatus newStatus = getRepositoryStatus(repoConnector);
					if (repositoryStatus == RepositoryStatus.NO_CONNECTION
							&& newStatus != RepositoryStatus.NO_CONNECTION) {
						return startup();
					}
					return newStatus == RepositoryStatus.OK;
				} else {
					return false;
				}
			} catch (CancelException e) {
				LOGGER.info("Action cancelled", e);
				return false;
			} catch (RuntimeException e) {
				MessageDialog.openError(
						shell,
						"Unexpected exception",
						"An unexpected exception was raised while checking the repository status, "
								+ "please contact neXtep Softwares.\n" + "Exception was :\n"
								+ e.getMessage());
				LOGGER.error("Unexpected exception", e);
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Depending on preferences, user will be prompted that the object has been resynched and any
	 * listening display connector will be refreshed.
	 * 
	 * @param o object which had been synched.
	 */
	public static void promptObjectSynched(Object o) {
		if (!Designer.getInstance().getPropertyBool(DesignerUIConstants.PROP_PROMPT_WHEN_SYNCHED)) {
			MessageDialogWithToggle.openInformation(CoreUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getShell(), UIMessages
					.getString("objectSynchedTitle"), UIMessages.getString("objectSynched"), //$NON-NLS-1$ //$NON-NLS-2$
					UIMessages.getString("objectSynchedToggle"), false, VCSUIPlugin.getDefault() //$NON-NLS-1$
							.getPreferenceStore(), DesignerUIConstants.PROP_PROMPT_WHEN_SYNCHED);
		}
		// Ensuring proper refresh
		if (o instanceof IObservable) {
			final IObservable obs = (IObservable) o;
			for (IEventListener l : obs.getListeners()) {
				if (l instanceof IConnector) {
					((IConnector) l).refreshConnector();
				}
			}
		}
	}

}
