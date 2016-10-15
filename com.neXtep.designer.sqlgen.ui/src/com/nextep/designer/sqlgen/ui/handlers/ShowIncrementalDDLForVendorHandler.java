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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.vcs.gui.dialog.VersionHistoryGUI;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.editors.DomainEditorComponent;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.services.IGenerationListener;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.ui.dialogs.ComponentWizard;
import com.nextep.designer.ui.dialogs.VendorSelectionDialog;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * @author Christophe Fondacci
 */
public class ShowIncrementalDDLForVendorHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) selection;
			final Object o = sel.getFirstElement();
			if (o instanceof IVersionable) {
				final IVersionable<?> versionable = (IVersionable<?>) o;

				// Prompting for database vendor
				final IWorkspaceService workspaceService = CorePlugin
						.getService(IWorkspaceService.class);
				final IWorkspace workspace = workspaceService.getCurrentWorkspace();
				final DBVendor currentVendor = workspace.getDBVendor();

				// Building our components and wrapping them to pages
				final List<IUIComponent> components = new ArrayList<IUIComponent>();
				final VendorSelectionDialog vendorDialog = new VendorSelectionDialog(currentVendor);
				components.add(vendorDialog);
				final VersionHistoryGUI versionHistoryDialog = new VersionHistoryGUI(versionable);
				versionHistoryDialog.setTitle(SQLMessages
						.getString("wizard.crossgeneration.version")); //$NON-NLS-1$
				versionHistoryDialog.setDescription(SQLMessages
						.getString("wizard.crossgeneration.version.desc")); //$NON-NLS-1$
				versionHistoryDialog.setImage(DBGMImages.WIZARD_TYPE);
				components.add(versionHistoryDialog);
				final IUIComponent domainDialog = new DomainEditorComponent();
				components.add(domainDialog);

				// Initializing dialog
				ComponentWizard wiz = new ComponentWizard(
						SQLMessages.getString("wizard.crossgeneration.title"), components); //$NON-NLS-1$
				WizardDialog dlg = new WizardDialog(UIHelper.getShell(), wiz) {
					@Override
					protected Point getInitialSize() {
						return new Point(600, 540);
					}
				};
				dlg.setBlockOnOpen(true);
				dlg.open();

				// If not cancelled
				if (!wiz.isCanceled()) {

					// Retrieving parameters
					final IVersionInfo initialVersion = versionHistoryDialog.getSelection();
					final DBVendor vendor = vendorDialog.getCurrentVendor();

					// Starting background job
					Job j = new Job(SQLGenMessages.getString("service.generation.incremental")) { //$NON-NLS-1$

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							final IGenerationService generationService = CorePlugin
									.getService(IGenerationService.class);
							generationService.generateIncrement(vendor, new IGenerationListener() {

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
		}
		return null;
	}
}
