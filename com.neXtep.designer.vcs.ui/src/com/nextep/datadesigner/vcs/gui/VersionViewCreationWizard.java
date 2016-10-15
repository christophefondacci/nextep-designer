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
package com.nextep.datadesigner.vcs.gui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.Designer;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * @author Christophe Fondacci
 */
public class VersionViewCreationWizard extends Wizard {

	// private static final Log log = LogFactory.getLog(VersionViewCreationWizard.class);
	private VersionViewWizardPage viewPage1;
	private IWorkspace view;
	private ViewConnectionWizardPage connectionPage;
	private IWizardPage rulesPage;
	private WizardPage deliveriesPage;
	private IConnection connection;

	public VersionViewCreationWizard(String title, IWorkspace view) {
		this.view = view;
		VersionViewEditorGUI gui = new VersionViewEditorGUI(view);
		viewPage1 = new VersionViewWizardPage(gui);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(VCSUIMessages.getString("wizard.workspace.title")); //$NON-NLS-1$
		addPage(viewPage1);
	}

	@Override
	public boolean performCancel() {
		// Removing any residual persisted view on cancel
		if (view != null && view.getId() > 0) {
			// Removing view on cancel
			ControllerFactory.getController(view).modelDeleted(view);
		}
		return true;
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// Setting current view if not null
		if (view != null) {
			VCSPlugin.getViewService().setCurrentWorkspace(view);
		}
		if (viewPage1.getCreationType() == 1 || viewPage1.getCreationType() == 3) {
			CorePlugin.getIdentifiableDao().save(view);
			return true;
		}
		if (viewPage1.getCreationType() == 4) {
			Designer.getInstance().runCommand("finishViewDeliveryWizard", deliveriesPage, view); //$NON-NLS-1$
			return true;
		}
		if (viewPage1.getCreationType() == 2) {
			// Registering connection to the view
			try {
				registerConnection(connection, view);
				view.setImportOnOpenNeeded(true);
			} catch (CoreException e) {
				MessageDialog
						.openError(Display.getCurrent().getActiveShell(), VCSUIMessages
								.getString("wizard.workspace.connectionError"), e.getMessage()); //$NON-NLS-1$
				return false;
			}
		}
		return true;
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		if (viewPage1.getCreationType() == 1) {
			return viewPage1.isPageComplete();
		} else if (viewPage1.getCreationType() == 2) {
			return getContainer().getCurrentPage() == connectionPage;
		} else if (viewPage1.getCreationType() == 3) {
			if (getContainer().getCurrentPage() == rulesPage) {
				return true;
			}
		} else if (viewPage1.getCreationType() == 4) {
			if (getContainer().getCurrentPage() == deliveriesPage) {
				return deliveriesPage.isPageComplete();
			}
		}
		return false;
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == viewPage1) {
			CorePlugin.getIdentifiableDao().save(view);
			VCSPlugin.getViewService().setCurrentWorkspace(view);
			Designer.getInstance().setContext(view.getDBVendor().name());
			if (viewPage1.getCreationType() == 2) {
				if (connectionPage == null) {
					connection = CorePlugin.getTypedObjectFactory().create(IConnection.class);
					connectionPage = new ViewConnectionWizardPage(connection);
				}
				connectionPage.setWizard(this);
				return connectionPage;
			} else if (viewPage1.getCreationType() == 3) {
				if (rulesPage == null) {
					rulesPage = new VersionViewRulesNewEditor(view); // new
					// ViewRulesWizardPage(view);
				}
				rulesPage.setWizard(this);
				return rulesPage;
			} else if (viewPage1.getCreationType() == 4) {
				if (deliveriesPage == null) {
					deliveriesPage = (WizardPage) Designer.getInstance().runCommand(
							"createViewDeliveryWizardPage", view); //$NON-NLS-1$
				}
				deliveriesPage.setWizard(this);
				return deliveriesPage;
			}
		} else if (page == connectionPage) {
			return null;
		}
		return super.getNextPage(page);
	}

	/**
	 * Registers the connection to the view's target sets.
	 * 
	 * @param connection connection to register
	 * @throws CoreException when extension cannot be loaded
	 */
	private void registerConnection(IConnection connection, IWorkspace view) throws CoreException {
		ITargetSet targetSet = CorePlugin.getTypedObjectFactory().create(ITargetSet.class);
		targetSet.addConnection(connection);
		CorePlugin.getPersistenceAccessor().save(targetSet);
	}
}
