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
package com.nextep.designer.vcs;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.vcs.model.IComparisonManager;
import com.nextep.designer.vcs.services.IWorkspaceService;

public class VCSPlugin extends Plugin {

	private static VCSPlugin plugin;
	public static final String PLUGIN_ID = "com.neXtep.designer.vcs"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/**
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static VCSPlugin getDefault() {
		return plugin;
	}

	/**
	 * Retrieves the comparison manager implementation
	 * 
	 * @return the {@link IComparisonManager}
	 */
	public static IComparisonManager getComparisonManager() {
		return getService(IComparisonManager.class);
	}

	/**
	 * Retrieves the {@link IWorkspaceService} singleton implementation
	 * 
	 * @return the {@link IWorkspaceService} implementation
	 */
	public static IWorkspaceService getViewService() {
		return getService(IWorkspaceService.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> serviceInterface) {
		final BundleContext context = getDefault().getBundle().getBundleContext();
		final ServiceReference ref = context.getServiceReference(serviceInterface.getName());
		if (ref != null) {
			Object o = context.getService(ref);
			if (o != null && serviceInterface.isAssignableFrom(o.getClass())) {
				return (T) o;
			}
		}
		throw new ErrorException("Unable to locate requested service " + serviceInterface.getName()); //$NON-NLS-1$
	}
}
