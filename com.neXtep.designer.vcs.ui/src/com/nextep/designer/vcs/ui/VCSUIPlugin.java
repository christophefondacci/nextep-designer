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
package com.nextep.designer.vcs.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.vcs.model.IComparisonManager;
import com.nextep.designer.vcs.ui.services.IComparisonUIManager;
import com.nextep.designer.vcs.ui.services.IVersioningUIService;

/**
 * The activator class controls the plug-in life cycle
 */
public class VCSUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.neXtep.designer.vcs.ui"; //$NON-NLS-1$

	// The shared instance
	private static VCSUIPlugin plugin;

	// private final static Log log = LogFactory.getLog(VCSUIPlugin.class);

	/**
	 * The constructor
	 */
	public VCSUIPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		VCSImages.dispose();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static VCSUIPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Retrieves the comparison UI manager
	 * 
	 * @return the {@link IComparisonManager} UI implementation
	 */
	public static IComparisonUIManager getComparisonUIManager() {
		return getService(IComparisonUIManager.class);
	}

	/**
	 * Retrieves the {@link IVersioningUIService} singleton
	 * 
	 * @return the {@link IVersioningUIService} implementation
	 */
	public static IVersioningUIService getVersioningUIService() {
		return getService(IVersioningUIService.class);
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
