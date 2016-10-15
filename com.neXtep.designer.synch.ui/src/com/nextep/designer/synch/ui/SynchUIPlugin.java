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
package com.nextep.designer.synch.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.nextep.datadesigner.exception.ErrorException;

/**
 * The activator class controls the plug-in life cycle
 */
public class SynchUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.neXtep.designer.synch.ui";
	public static final Log LOG = LogFactory.getLog(SynchUIPlugin.class);
	// The shared instance
	private static SynchUIPlugin plugin;

	/**
	 * The constructor
	 */
	public SynchUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		SynchUIImages.dispose();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SynchUIPlugin getDefault() {
		return plugin;
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
		throw new ErrorException("Unable to locate requested service " + serviceInterface.getName());
	}
}
