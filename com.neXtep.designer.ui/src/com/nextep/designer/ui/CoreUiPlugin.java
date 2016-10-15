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
package com.nextep.designer.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IResourceLocator;
import com.nextep.designer.core.model.ResourceConstants;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.services.impl.RepositoryUIService;

/**
 * The activator class controls the plug-in life cycle
 */
public class CoreUiPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.neXtep.designer.ui"; //$NON-NLS-1$

	// The shared instance
	private static CoreUiPlugin plugin;

	/**
	 * The constructor
	 */
	public CoreUiPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Registering core resources
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);
		final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
		final IResourceLocator lockLocator = coreFactory.createImageLocator(PLUGIN_ID,
				ImageFactory.RESOURCE_LOCK_LARGE);
		coreService.registerResource(ResourceConstants.ICON_LOCK, lockLocator);

		final IResourceLocator dropLocator = coreFactory.createImageLocator(PLUGIN_ID,
				UIImages.RESOURCE_DROP_LARGE);
		coreService.registerResource(ResourceConstants.ICON_DROP, dropLocator);

		final IResourceLocator errorLocator = coreFactory.createImageLocator(PLUGIN_ID,
				ImageFactory.RESOURCE_ERROR);
		coreService.registerResource(ResourceConstants.ICON_ERROR, errorLocator);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		UIImages.dispose();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CoreUiPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Retreives the repository service with the UI layer. This service should be preferred to
	 * {@link CorePlugin#getRepositoryService()} when called by UI plugins
	 * 
	 * @return the {@link IRepositoryService}
	 */
	public static IRepositoryService getRepositoryUIService() {
		return RepositoryUIService.getInstance();
	}
}
