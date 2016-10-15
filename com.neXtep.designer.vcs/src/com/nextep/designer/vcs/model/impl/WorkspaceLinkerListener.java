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
package com.nextep.designer.vcs.model.impl;

import java.text.MessageFormat;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.vcs.services.IViewLinker;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.base.AbstractWorkspaceListener;

/**
 * This view listener handles the linkage of the view by consulting the registered extension points.
 * 
 * @author Christophe Fondacci
 */
public class WorkspaceLinkerListener extends AbstractWorkspaceListener {

	private final static Log log = LogFactory.getLog(WorkspaceLinkerListener.class);

	@Override
	public void workspaceChanged(IWorkspace oldView, IWorkspace newView, IProgressMonitor monitor) {
		if (oldView != null && oldView.getUID() != null
				&& oldView.getUID().equals(newView.getUID())) {
			newView.setImportOnOpenNeeded(oldView.isImportOnOpenNeeded());
		}
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
				IViewLinker.LINKER_EXTENSION_POINT_ID, "name", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		monitor.beginTask(VCSMessages.getString("linkView"), elts.size()); //$NON-NLS-1$
		for (IConfigurationElement conf : elts) {
			try {
				IViewLinker linker = (IViewLinker) conf.createExecutableExtension("class"); //$NON-NLS-1$
				monitor.subTask(MessageFormat.format(
						VCSMessages.getString("view.listener.linker.linkingMsg"), linker.getLabel())); //$NON-NLS-1$
				try {
					linker.link(newView);
				} catch (Exception e) {
					log.error(MessageFormat.format(VCSMessages.getString("linkException"), conf //$NON-NLS-1$
							.getAttribute("name")), e); //$NON-NLS-1$
				}
				monitor.worked(10);
			} catch (CoreException e) {
				log.error(
						MessageFormat.format(
								VCSMessages.getString("view.listener.linker.loadFailed"), conf.getAttribute("name")), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	@Override
	public int getPriority() {
		return PRIORITY_INTERNAL;
	}
}
