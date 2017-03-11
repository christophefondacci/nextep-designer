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
package com.nextep.designer.sqlclient.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.rcp.PageTracker;
import com.nextep.designer.sqlclient.ui.model.impl.SQLClientEditorTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class SQLClientPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.neXtep.designer.sqlclient.ui"; //$NON-NLS-1$

	// The shared instance
	private static SQLClientPlugin plugin;

	/**
	 * The constructor
	 */
	public SQLClientPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		plugin.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				plugin.getWorkbench().getDisplay().disposeExec(new Runnable() {
					@Override
					public void run() {
						SQLClientImages.dispose();
					}
				});
			}
		});
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.addPartListener(new SQLClientEditorTracker());
		} else {
			PlatformUI.getWorkbench().addWindowListener(new PageTracker() {

				@Override
				public void pageOpened(IWorkbenchPage page) {
					page.getWorkbenchWindow().removePageListener(this);
					page.getWorkbenchWindow().getWorkbench().removeWindowListener(this);
					page.addPartListener(new SQLClientEditorTracker());
				}

				@Override
				public void pageClosed(IWorkbenchPage page) {
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SQLClientPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static <T> T getService(Class<T> serviceClass) {
		return Designer.getService(getDefault().getBundle().getBundleContext(), serviceClass);
	}
}
