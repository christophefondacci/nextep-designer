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
package com.nextep.designer.synch.ui.services.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.osgi.service.prefs.BackingStoreException;

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.dbgm.ui.editors.DomainEditorComponent;
import com.nextep.designer.sqlgen.ui.SQLScriptEditorInput;
import com.nextep.designer.sqlgen.ui.editors.SQLEditor;
import com.nextep.designer.sqlgen.ui.editors.SQLMultiEditor;
import com.nextep.designer.sqlgen.ui.model.SubmitionManager;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.services.ISynchronizationService;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.dialogs.TableSelectionDialog;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;
import com.nextep.designer.ui.dialogs.ComponentWizard;
import com.nextep.designer.ui.dialogs.TitleAreaDialogWrapper;
import com.nextep.designer.ui.helpers.BlockingJob;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.base.RunnableWithReturnedValue;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * Default implementation of the {@link ISynchronizationUIService}
 * 
 * @author Christophe Fondacci
 */
public class SynchronizationUIService implements ISynchronizationUIService,
		ISynchronizationListener {

	private final static String PREF_DATASYNC_TABLES_KEY = "com.neXtep.designer.synch.ui.synchedTables";
	private final static String DATASYNC_TABLES_ROOT = "synchedTables";
	private final static String DATASYNC_TABLE_REFID = "ref";
	private final Set<ISynchronizationListener> listeners;
	private ISynchronizationResult lastResult;
	private static final Log log = LogFactory.getLog(SynchronizationUIService.class);
	private ISynchronizationService synchronizationService;
	private IWorkspaceService workspaceService;

	// private final static Log LOGGER =
	// LogFactory.getLog(SynchronizationUIService.class);

	@Override
	public ISynchronizationResult buildSynchronizationResult(IVersionContainer container,
			IConnection connection, ComparisonScope scope, IProgressMonitor monitor) {
		return synchronizationService.buildSynchronizationResult(container, connection, scope,
				monitor);
	}

	public void setSynchronizationService(ISynchronizationService synchronizationService) {
		this.synchronizationService = synchronizationService;
		synchronizationService.addSynchronizationListener(this);
	}

	public void unsetSynchronizationService(ISynchronizationService synchronizationService) {
		synchronizationService.removeSynchronizationListener(this);
		this.synchronizationService = null;
	}

	public SynchronizationUIService() {
		listeners = new HashSet<ISynchronizationListener>();
	}

	@Override
	public void addSynchronizationListener(final ISynchronizationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeSynchronizationListener(ISynchronizationListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void synchronize(final IVersionContainer container, final IConnection connection,
			IProgressMonitor monitor) {
		// Checking whether we are generating for current vendor
		final DBVendor currentVendor = workspaceService.getCurrentWorkspace().getDBVendor();
		if (connection.getDBVendor() != currentVendor) {
			final IUIComponent domainDialog = new DomainEditorComponent();

			// Initializing dialog
			ComponentWizard wiz = new ComponentWizard(
					SynchUIMessages.getString("wizard.synch.crossvendor"), Arrays.asList(domainDialog)); //$NON-NLS-1$
			WizardDialog dlg = new WizardDialog(UIHelper.getShell(), wiz) {
				@Override
				protected Point getInitialSize() {
					return new Point(600, 540);
				}

			};
			dlg.setBlockOnOpen(true);
			dlg.open();
			// Checking cancel state
			if (wiz.isCanceled()) {
				return;
			}
		}
		Job j = new BlockingJob(SynchUIMessages.getString("synch.job.name")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					synchronizationService.synchronize(container, connection, monitor);
				} catch (final ErrorException e) {
					log.error(SynchUIMessages.getString("synch.job.error"), e); //$NON-NLS-1$
					this.addJobChangeListener(new JobChangeAdapter() {

						@Override
						public void done(IJobChangeEvent event) {
							PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

								@Override
								public void run() {
									MessageDialog.openWarning(null,
											SynchUIMessages.getString("synch.job.error"), e //$NON-NLS-1$
													.getMessage());
								}
							});

						}

					});
				} catch (CancelException e) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.setPriority(Job.BUILD);
		j.schedule();
	}

	@Override
	public void buildScript(final ISynchronizationResult synchResult) {
		Job j = new BlockingJob(SynchUIMessages.getString("synch.job.generateScript")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor parentMonitor) {
				buildScript(synchResult, parentMonitor);
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.schedule();
	}

	@Override
	public void buildScript(ISynchronizationResult synchResult, IProgressMonitor parentMonitor) {
		final SubMonitor monitor = SubMonitor.convert(parentMonitor,
				SynchUIMessages.getString("synch.job.generateScript"), 30); //$NON-NLS-1$
		synchronizationService.buildScript(synchResult, monitor.newChild(10));
		final IEditorPart part = showScript(synchResult);
		if (part != null) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					part.doSave(monitor.newChild(20));
				}
			});
		}
		monitor.done();
	}

	@Override
	public IEditorPart showScript(final ISynchronizationResult synchResult) {
		// Opening in the UI thread every generated script
		if (synchResult.getGeneratedScript() != null) {
			RunnableWithReturnedValue<IEditorPart> runnable = new RunnableWithReturnedValue<IEditorPart>() {

				@Override
				public void run() {
					try {
						final ISQLScript s = synchResult.getGeneratedScript();
						final String editorId = s instanceof SQLWrapperScript ? SQLMultiEditor.EDITOR_ID
								: SQLEditor.EDITOR_ID;
						returnedValue = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().openEditor(new SQLScriptEditorInput(s), editorId);

					} catch (PartInitException e) {
						throw new ErrorException(
								MessageFormat.format(SynchUIMessages
										.getString("synch.openEditorFailed"), e.getMessage()), e); //$NON-NLS-1$
					}
				}
			};
			Display.getDefault().syncExec(runnable);
			return runnable.returnedValue;
		}
		return null;
	}

	@Override
	public void adjustParents(IComparisonItem item) {
		if (item == null)
			return;
		boolean childChecked = false;
		boolean allChildChecked = item.getSubItems().size() > 0;
		for (IComparisonItem subItem : item.getSubItems()) {
			if (subItem.getMergeInfo().getMergeProposal() != subItem.getTarget()) {
				if (subItem.getDifferenceType() != DifferenceType.EQUALS) {
					if (!childChecked) {
						childChecked = true;
					}
				}
			} else {
				allChildChecked = false;
			}
		}
		if (allChildChecked) {
			// Avoiding recursive loops when nothing has changed
			if (item.getMergeInfo().getMergeProposal() == item.getSource()) {
				return;
			}
			// Applying new value
			item.getMergeInfo().setMergeProposal(item.getSource());
		} else if (childChecked) {
			// Avoiding recursive loops when nothing has changed
			if (item.getMergeInfo().getMergeProposal() == null) {
				return;
			}
			// Applying change
			item.getMergeInfo().setMergeProposal(null);
		} else {
			// Avoiding recursive loops when nothing has changed
			if (item.getMergeInfo().getMergeProposal() == item.getTarget()) {
				return;
			}
			// Applying new value
			item.getMergeInfo().setMergeProposal(item.getTarget());
		}
		adjustParents(item.getParent());
	}

	@Override
	public void selectProposal(IComparisonItem item, ComparedElement selection,
			boolean recurseDependentElements) {
		MultiValueMap invRefMap = null;
		if (recurseDependentElements) {
			invRefMap = CorePlugin.getService(IReferenceManager.class).getReverseDependenciesMap();
		} else {
			invRefMap = new MultiValueMap();
		}
		selectProposal(item, selection, recurseDependentElements, invRefMap);
	}

	@SuppressWarnings("unchecked")
	private void selectProposal(IComparisonItem item, ComparedElement selection,
			boolean recurseDependentElements, MultiValueMap reverseDependencyMap) {
		if (item == null || item.getDifferenceType() == DifferenceType.EQUALS)
			return;
		final IReferenceable toSelectItem = selection.get(item);
		if (item.getMergeInfo().getMergeProposal() == toSelectItem) {
			return;
		}
		item.getMergeInfo().setMergeProposal(toSelectItem);
		for (IComparisonItem subItem : item.getSubItems()) {
			if (subItem.getDifferenceType() != DifferenceType.EQUALS) {
				selectProposal(subItem, selection, false);
			}
		}

		if (recurseDependentElements) {
			// When we unselect an element, we recursively unselect any element
			// which depend on it
			if (selection.isTarget()) {
				Collection<IReferencer> dependentRefs = (Collection<IReferencer>) reverseDependencyMap
						.get(item.getReference());
				if (dependentRefs != null) {
					for (IReferencer r : dependentRefs) {
						if (r instanceof IReferenceable) {
							final IComparisonItem dependentItem = lastResult
									.getComparisonItemFor(((IReferenceable) r).getReference());
							selectProposal(dependentItem, selection, true);
						}
					}
				}
			}
			// When we select an element, we recursively select elements whose
			// current element is
			// depending
			if (selection.isSource()) {
				if (item.getSource() instanceof IReferencer) {
					final IReferencer referencer = (IReferencer) item.getSource();
					Collection<IReference> dependencies = referencer.getReferenceDependencies();
					for (IReference d : dependencies) {
						final IComparisonItem dependentItem = lastResult.getComparisonItemFor(d);
						if (dependentItem != null
								&& dependentItem.getMergeInfo().getMergeProposal() != dependentItem
										.getSource()) {
							selectProposal(dependentItem, selection, true);
						}
					}
				}
			}
		}
		// Adjusting the dirty flag
		if (lastResult != null) {
			lastResult.setDirty(true);
		}
	}

	@Override
	public void selectProposal(IComparisonItem item, ComparedElement selection) {
		MultiValueMap invRefMap = CorePlugin.getService(IReferenceManager.class)
				.getReverseDependenciesMap();
		selectProposal(item, selection, invRefMap);
	}

	@Override
	public void selectProposal(IComparisonItem item, ComparedElement selection,
			MultiValueMap reverseDependenciesMap) {
		boolean importDependencies = false;
		final ICommandService service = (ICommandService) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getService(ICommandService.class);
		final Command cmd = service.getCommand("com.neXtep.designer.synch.ui.toggleDependencies"); //$NON-NLS-1$
		final State state = cmd.getState(RegistryToggleState.STATE_ID);
		importDependencies = (Boolean) state.getValue();
		selectProposal(item, selection, importDependencies, reverseDependenciesMap);
	}

	@Override
	public void changeSynchronizationScope(final ComparisonScope scope,
			final ISynchronizationResult result, final IProgressMonitor monitor) {
		Job j = new BlockingJob("") { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				synchronizationService.changeSynchronizationScope(scope, result, monitor);
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.setPriority(Job.BUILD);
		j.schedule();
	}

	@Override
	public void clearSynchronization() {
		synchronizationService.clearSynchronization();
	}

	@Override
	public void submit(final ISynchronizationResult result) {
		// If result has not yet been generated we generate and submit without
		// notice
		if (result.getGenerationResult() == null) {
			synchronizationService.buildScript(result, null);
		}
		// If result is dirty we need to regenerate it so we prompt
		if (result.isDirty()) {
			final boolean confirmed = MessageDialog.openConfirm(Display.getCurrent()
					.getActiveShell(), SynchUIMessages
					.getString("service.synch.needsSqlGenerationTitle"), //$NON-NLS-1$
					SynchUIMessages.getString("service.synch.needsSqlGenerationMsg")); //$NON-NLS-1$
			if (confirmed) {
				buildScript(result);
			}
		} else {
			// Building UI scripts
			final String jobName = MessageFormat.format(SynchUIMessages
					.getString("synch.job.submitScript"), result.getConnection().getName()); //$NON-NLS-1$
			Job j = new Job(jobName) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						monitor.beginTask(jobName, 1);
						monitor.subTask(""); //$NON-NLS-1$
						final ISQLScript script = result.getGeneratedScript();
						SubmitionManager.submit(result.getConnection(), script,
								result.getGenerationResult(), monitor);
						SQLEditorUIServices.getInstance().annotateVisibleEditor();

						showScript(result);
						return Status.OK_STATUS;
					} catch (CancelException e) {
						return Status.CANCEL_STATUS;
					}
				}
			};
			j.setUser(true);
			j.schedule();
		}
	}

	@Override
	public void newSynchronization(final ISynchronizationResult synchronizationResult) {
		this.lastResult = synchronizationResult;
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				for (ISynchronizationListener listener : new ArrayList<ISynchronizationListener>(
						listeners)) {
					listener.newSynchronization(synchronizationResult);
				}
			}
		});
	}

	@Override
	public void scopeChanged(final ComparisonScope scope) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				for (ISynchronizationListener listener : listeners) {
					listener.scopeChanged(scope);
				}
			}
		});
	}

	@Override
	public void synchronizeData(final IConnection conn, IProgressMonitor monitor,
			IVersionable<?>... synchronizedItems) {
		// Retrieving every table from workspace
		final Collection<IVersionable<?>> vTables = VersionHelper.getAllVersionables(
				workspaceService.getCurrentWorkspace(),
				IElementType.getInstance(IBasicTable.TYPE_ID));
		// Initializing our editor
		final Collection<IVersionable<?>> preselectedTables = getDataSynchronizationTables();
		TableSelectionDialog dlg = new TableSelectionDialog(preselectedTables, vTables);
		TitleAreaDialogWrapper wrapper = new TitleAreaDialogWrapper(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), dlg, SWT.CLOSE | SWT.RESIZE | SWT.TITLE
				| SWT.BORDER);
		wrapper.setBlockOnOpen(true);
		int result = wrapper.open();
		if (result == Window.OK) {
			final Collection<IVersionable<?>> selectedTables = dlg.getSelection();
			saveDataSynchronizationTables(selectedTables);
			Job j = new Job("Synchronizing data...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					synchronizationService.synchronizeData(conn, monitor,
							selectedTables.toArray(new IVersionable<?>[selectedTables.size()]));
					return Status.OK_STATUS;
				}
			};
			j.setUser(true);
			j.schedule();
		}
	}

	@Override
	public Collection<IVersionable<?>> getDataSynchronizationTables() {
		Collection<IVersionable<?>> tables = new ArrayList<IVersionable<?>>();
		IPreferenceStore store = SynchUIPlugin.getDefault().getPreferenceStore();
		final String prefKey = buildPreferenceKey(PREF_DATASYNC_TABLES_KEY);
		String preference = store.getString(prefKey);
		boolean needsResave = false;
		if (preference != null && !"".equals(preference)) {
			StringReader reader = new StringReader(preference);
			try {
				IMemento memento = XMLMemento.createReadRoot(reader);
				for (IMemento m : memento.getChildren(DATASYNC_TABLE_REFID)) {
					final String refId = m.getID();
					if (refId != null) {
						IReference r = new Reference(IElementType.getInstance(IBasicTable.TYPE_ID),
								null, null);
						r.setUID(new UID(Long.valueOf(refId)));
						try {
							IReferenceable table = VersionHelper.getReferencedItem(r);
							tables.add(VersionHelper.getVersionable(table));
						} catch (UnresolvedItemException e) {
							log.warn(
									"Could not retrieve all tables to synchronized, removing element from data synchronization, reason is : "
											+ e.getMessage(), e);
							needsResave = true;
						}
					}
				}
			} catch (WorkbenchException e) {
				log.warn("Unable to retrieve predefined tables to synchronize", e); //$NON-NLS-1$
			}
		}
		// Resaving tables if we lost some references
		if (needsResave) {
			saveDataSynchronizationTables(tables);
		}
		return tables;
	}

	private void saveDataSynchronizationTables(Collection<IVersionable<?>> synchronizedTables) {
		XMLMemento memento = XMLMemento.createWriteRoot(DATASYNC_TABLES_ROOT);
		for (IVersionable<?> synchedTable : synchronizedTables) {
			memento.createChild(DATASYNC_TABLE_REFID, synchedTable.getReference().getUID()
					.toString());
		}
		final String prefKey = buildPreferenceKey(PREF_DATASYNC_TABLES_KEY);
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
		} catch (IOException e) {
			log.warn("Error occurred while saving synchronized tables: " + e.getMessage(), e);
		}
		IPreferenceStore store = SynchUIPlugin.getDefault().getPreferenceStore();
		store.putValue(prefKey, writer.toString());
		try {
			new InstanceScope().getNode(SynchUIPlugin.PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			log.warn("Error occurred while saving synchronized tables: " + e.getMessage(), e);
		}
	}

	private String buildPreferenceKey(String key) {
		StringBuilder buf = new StringBuilder();
		IConnection repositoryConnection = CorePlugin.getRepositoryService()
				.getRepositoryConnection();
		if (repositoryConnection != null) {
			buf.append(repositoryConnection.getDatabase()
					+ "." + repositoryConnection.getServerIP() //$NON-NLS-1$
					+ "." + repositoryConnection.getLogin() + "."); ////$NON-NLS-1$ //$NON-NLS-2$ 
		}
		final IWorkspace currentView = workspaceService.getCurrentWorkspace();
		if (currentView != null) {
			buf.append(currentView.getId());
			buf.append('.');
		}
		buf.append(key);
		return buf.toString();
	}

	public void setWorkspaceService(IWorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}
}
