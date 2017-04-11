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
package com.nextep.designer.vcs.ui.controllers;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.hibernate.classic.Session;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.gui.impl.GUIWrapper;
import com.nextep.datadesigner.gui.model.InvokableController;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.gui.dialog.MergeInitGUI;
import com.nextep.datadesigner.vcs.gui.dialog.MergePreviewWizard;
import com.nextep.datadesigner.vcs.gui.dialog.MergeResultGUI;
import com.nextep.datadesigner.vcs.gui.dialog.MergeWizard;
import com.nextep.datadesigner.vcs.impl.MergeStrategy;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.dao.IIdentifiableDAO;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.services.IConnectionService;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.MergeStatus;
import com.nextep.designer.vcs.model.impl.Workspace;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.VersionUIHelper;

/**
 * @author Christophe Fondacci
 */
public class MergeController extends InvokableController {

	private static final Log LOGGER = LogFactory.getLog(MergeController.class);

	private static MergeController instance;

	public MergeController() {
	}

	public static MergeController getInstance() {
		if (instance == null) {
			instance = new MergeController();
		}
		return instance;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.InvokableController#invoke(java.lang.Object)
	 */
	@Override
	public Object invoke(Object... model) {
		if (model.length == 0) {
			throw new ErrorException(
					"The merge controller need at least 1 non-null argument to proceed.");
		} else {
			final IVersionable<?> versionable = (IVersionable<?>) model[0];
			final MergeWizard wiz = new MergeWizard(new MergeInitGUI(versionable),
					new MergePreviewWizard(versionable));
			wiz.setToVersionable(versionable);
			WizardDialog w = new WizardDialog(VCSUIPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getShell(), wiz);
			w.setBlockOnOpen(true);
			w.open();
			if (w.getReturnCode() != Window.CANCEL) {
				// FIXME QUICKFIX calling this static method => TO remove !!!
				MergeStrategy.setIsGenerating(false);
				// try {
				final IMerger m = MergerFactory.getMerger(wiz.getToRelease());
				// Building compare command
				ICommand compareCommand = new ICommand() {

					@Override
					public Object execute(Object... parameters) {
						return m.compare(wiz.getReference(), wiz.getFromRelease(),
								wiz.getToRelease(), true);
					}

					@Override
					public String getName() {
						return "Computing differences between " + wiz.getToRelease().getLabel()
								+ " and " + wiz.getFromRelease().getLabel();
					}
				};
				final IComparisonItem comp = (IComparisonItem) CommandProgress
						.runWithProgress(compareCommand).iterator().next();

				ProgressMonitorDialog pd = new ProgressMonitorDialog(VCSUIPlugin.getDefault()
						.getWorkbench().getActiveWorkbenchWindow().getShell());
				try {
					pd.run(true, false, new MergeProgress(comp, m));
				} catch (Exception e) {
					throw new ErrorException(e);
				}
				// } finally {
				MergeStrategy.setIsGenerating(true);
				// }
				MergeResultGUI mergeGUI = new MergeResultGUI(true, comp);
				mergeGUI.setIsRepositoryMerge(true);
				mergeGUI.setRootText("Source release " + wiz.getFromRelease().getLabel() + " [" //$NON-NLS-2$
						+ wiz.getFromRelease().getBranch().getName() + "]", //$NON-NLS-1$
						"Current target release " + wiz.getToRelease().getLabel() + " [" //$NON-NLS-2$
								+ wiz.getToRelease().getBranch().getName() + "]", "Merge result"); //$NON-NLS-1$
				boolean doItAgain = true;
				while (doItAgain) {
					invokeGUI(new GUIWrapper(mergeGUI, "Merge results", 800, 600));
					// Checking that everything is resolved
					if (comp.getMergeInfo().getStatus() != MergeStatus.MERGE_RESOLVED) {
						// IF not we ask if the user would like to edit again
						doItAgain = MessageDialog.openQuestion(VCSUIPlugin.getDefault()
								.getWorkbench().getActiveWorkbenchWindow().getShell(),
								VCSUIMessages.getString("mergeUnresolvedTitle"), //$NON-NLS-1$
								VCSUIMessages.getString("mergeUnresolved")); //$NON-NLS-1$
						if (!doItAgain) {
							throw new CancelException(
									"Merge operation aborted due to unresolved conflicts.");
						}
					} else {
						doItAgain = false;
					}
				}
				// We fall here if no cancel exception
				// HibernateUtil.getInstance().getSession().clear();
				// IdentifiableDAO.getInstance().loadAll(VersionReference.class);
				HibernateUtil.getInstance().clearAllSessions();
				pd = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell());
				try {
					pd.run(false, false, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							monitor.beginTask("Merge in progress...", 5);
							// Global listeners switch off
							Observable.deactivateListeners();
							// Refreshing progress
							monitor.worked(1);
							IVersionable<?> mergedObject = null;
							try {
								// Building the merge result
								final String activityText = "Merged "
										+ wiz.getFromRelease().getLabel() + " -> " //$NON-NLS-1$
										+ wiz.getFromRelease().getBranch().getName();
								final IVersioningService versioningService = VCSPlugin
										.getService(IVersioningService.class);
								final IActivity mergeActivity = versioningService
										.createActivity(activityText);
								mergedObject = (IVersionable<?>) m.buildMergedObject(comp,
										mergeActivity);
								// Checking if the merge did anything
								if (mergedObject instanceof IVersionable<?>) {
									if (((IVersionable<?>) mergedObject).getVersion().getStatus() == IVersionStatus.CHECKED_IN) {
										final boolean forceMerge = MessageDialog.openQuestion(
												Display.getCurrent().getActiveShell(),
												VCSUIMessages.getString("mergeDidNothingTitle"), //$NON-NLS-1$
												VCSUIMessages.getString("mergeDidNothing")); //$NON-NLS-1$
										if (forceMerge) {
											comp.getMergeInfo().setMergeProposal(null);
											mergedObject = (IVersionable<?>) m.buildMergedObject(
													comp, mergeActivity);
										} else {
											return;
										}
									}
								}
							} finally {
								Observable.activateListeners();
							}
							LOGGER.info("Merged successfully!");
							// Refreshing progress
							monitor.worked(1);
							monitor.setTaskName("Updating view contents...");

							// Temporary bugfix for DES-710
							// TODO refactor EVERYTHING as a service
							final Session session = HibernateUtil.getInstance().getSandBoxSession();

							// FIXME draft of view removal / addition of the merged object
							// Switching versionables (for database only)
							IVersionContainer parent = versionable.getContainer();
							// First removing current versionable in current session
							parent.getContents().remove(versionable);
							versionable.setContainer(null);

							// Forcing save of module without parent
							final IIdentifiableDAO identifiableDao = CorePlugin
									.getIdentifiableDao();
							identifiableDao.save(parent);
							// Refreshing progress
							monitor.worked(1);
							// Reloading parent container in sandbox (because merged object is in
							// sandbox)

							if (parent instanceof IWorkspace) {
								parent = (IVersionContainer) identifiableDao.load(Workspace.class,
										parent.getUID(), session, false);
							} else {
								parent = (IVersionContainer) identifiableDao.load(
										IVersionable.class, parent.getUID(), session, false);
							}
							// Refreshing progress
							monitor.worked(1);
							monitor.setTaskName("Committing to repository...");
							// Adding sandbox merged object
							mergedObject.setContainer(parent);
							parent.getContents().add(mergedObject);
							// Saving to sandbox before reloading view

							identifiableDao.save(mergedObject);
							session.flush();
							// **************
							// DES-710 urgent workaround
							// Hibernate does not properly send the INSERT statements to the db
							// But logs it properly in the logs !
							final IRepositoryService repositoryService = CorePlugin
									.getRepositoryService();
							final IConnectionService connectionService = CorePlugin
									.getConnectionService();
							final IConnection repositoryConnection = repositoryService
									.getRepositoryConnection();
							Connection conn = null;
							PreparedStatement stmt = null;
							try {
								conn = connectionService.connect(repositoryConnection);
								String insertSql = null;
								if (parent instanceof IWorkspace) {
									insertSql = "INSERT INTO rep_view_contents ( " //$NON-NLS-1$
											+ "  view_id, version_id " //$NON-NLS-1$
											+ ") VALUES ( " //$NON-NLS-1$
											+ "  ?, ? " //$NON-NLS-1$
											+ ") "; //$NON-NLS-1$
								} else {
									insertSql = "INSERT INTO rep_module_contents ( " //$NON-NLS-1$
											+ "  module_id, version_id " //$NON-NLS-1$
											+ ") VALUES ( " //$NON-NLS-1$
											+ "  ?, ? " //$NON-NLS-1$
											+ ") "; //$NON-NLS-1$
								}
								stmt = conn.prepareStatement(insertSql);
								stmt.setLong(1, parent.getUID().rawId());
								stmt.setLong(2, mergedObject.getUID().rawId());
								stmt.execute();
								if (!conn.getAutoCommit()) {
									conn.commit();
								}
							} catch (SQLException e) {
								LOGGER.error(e);
							} finally {
								if (stmt != null) {
									try {
										stmt.close();
									} catch (SQLException e) {
										LOGGER.error(e);
									}
								}
								if (conn != null) {
									try {
										conn.close();
									} catch (SQLException e) {
										LOGGER.error(e);
									}
								}
							}
							// End of DES-710 workaround
							// **************

							// HibernateUtil.getInstance().reconnectAll();
							// Merger.save(mergedObject);
							// Merger.save(parent);
							monitor.worked(1);
							LOGGER.info("Please wait while view is reloading...");
							monitor.setTaskName("Finished: Reloading view...");
							monitor.done();
							// END OF the fixme part

						}

					});
				} catch (Exception e) {
					throw new ErrorException(e);
				}

				// Restoring a new view
				LOGGER.info("Refreshing current view");
				VersionUIHelper.changeView(VersionHelper.getCurrentView().getUID());

				// Designer.getInstance().invokeSelection("com.neXtep.designer.vcs.SelectionInvoker",
				// "version.compare", comp);
			}
		}
		return null;
	}

	private class MergeProgress implements IRunnableWithProgress, IEventListener {

		private IComparisonItem comp;
		private IMerger merger;
		private IProgressMonitor monitor;
		int count = 0;

		public MergeProgress(IComparisonItem comp, IMerger merger) {
			this.comp = comp;
			this.merger = merger;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
			this.monitor = monitor;
			Designer.setProgressMonitor(monitor);
			// Counting items
			countItems(comp);
			try {
				MergerFactory.registerMergeListener(this);
				monitor.beginTask("Merge in progress", count + 1);
				merger.merge(comp, null, null);
				monitor.done();
			} finally {
				MergerFactory.unregisterMergeListener(this);
				Designer.setProgressMonitor(null);
			}
		}

		private void countItems(IComparisonItem i) {
			for (IComparisonItem subItem : i.getSubItems()) {
				countItems(subItem);
			}
			count += i.getSubItems().size();
		}

		@Override
		public void handleEvent(ChangeEvent event, IObservable source, Object data) {
			monitor.setTaskName((String) data);
			monitor.worked(1);

		}

	}

	/**
	 * Checks whether everything in this comparison tree is resolved for a merge operation.
	 * 
	 * @param item a {@link IComparisonItem} to check
	 * @return <code>true</code> if resolved, else <code>false</code>
	 */
	private boolean isMergeResolved(IComparisonItem item) {
		if (item.getMergeInfo().getStatus() != MergeStatus.MERGE_RESOLVED) {
			return false;
		} else {
			for (IComparisonItem subItem : item.getSubItems()) {
				if (!isMergeResolved(subItem)) {
					return false;
				}
			}
			return true;
		}
	}

}
