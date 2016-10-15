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
package com.nextep.designer.beng.ui.commands;

import java.util.ArrayList;
import java.util.List;
import com.nextep.datadesigner.beng.gui.VersionViewDeliveriesEditor;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.impl.RepositoryUser;
import com.nextep.datadesigner.vcs.impl.VersionBranch;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.ui.jface.DeliveryContainerInfoContentProvider;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * TODO: refactor the way the "delivery-based" workspace creation is connected to the workspace
 * creation wizard. The cleanest way would be to properly define an extension point for workspace
 * creators that would be dynamically plugged in the main wizard.
 * 
 * @author Christophe Fondacci
 * @deprecated refactor this through dynamic extension points
 */
public class FinishViewDeliveryWizardCommand implements ICommand {

	@Override
	public Object execute(Object... parameters) {
		VersionViewDeliveriesEditor editor = (VersionViewDeliveriesEditor) parameters[0];
		final IWorkspace view = (IWorkspace) parameters[1];
		final IDeliveryInfo dlv = editor.getSelectedDelivery();
		final DeliveryContainerInfoContentProvider p = new DeliveryContainerInfoContentProvider();
		Object[] objs = p.getElements(dlv);

		List<ICommand> cmds = new ArrayList<ICommand>();
		cmds.add(new ICommand() {

			@Override
			public String getName() {
				return "Flushing current view";
			}

			@Override
			public Object execute(Object... parameters) {
				final IWorkspaceService viewService = VCSPlugin.getViewService();
				// We clear our hibernate session and reload our view
				HibernateUtil.getInstance().clearAllSessions(); // getSession().clear();
				CorePlugin.getService(IReferenceManager.class).flush();
				VersionBranch.reset();

				// Loading root branch
				CorePlugin.getIdentifiableDao().load(VersionBranch.class, new UID(1));
				IRepositoryUser user = (IRepositoryUser) CorePlugin.getIdentifiableDao().load(
						RepositoryUser.class, viewService.getCurrentUser().getUID());
				viewService.setCurrentUser(user);
				return null;
			}
		});
		for (final Object o : objs) {
			cmds.add(new ICommand() {

				@Override
				public Object execute(Object... parameters) {
					ContainerInfo i = (ContainerInfo) o;
					IVersionable<?> c = (IVersionable<?>) CorePlugin.getIdentifiableDao().load(
							IVersionable.class, i.getUID());
					view.addVersionable(c, new ImportPolicyAddOnly());
					// Forcing a save (DES-656)
					CorePlugin.getPersistenceAccessor().save(view);
					return null;
				}

				@Override
				public String getName() {
					return "Importing module " + ((ContainerInfo) o).getName() + " release "
							+ ((ContainerInfo) o).getRelease().getLabel() + " into view...";
				}
			});
		}
		CommandProgress.runWithProgress(false, "View creation", editor.getShell(),
				cmds.toArray(new ICommand[cmds.size()]));
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

}
