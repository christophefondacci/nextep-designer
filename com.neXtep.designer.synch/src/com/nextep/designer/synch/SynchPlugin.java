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
package com.nextep.designer.synch;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import com.nextep.datadesigner.Designer;

/**
 * The activator class controls the plug-in life cycle
 */
public class SynchPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.neXtep.designer.synch"; //$NON-NLS-1$

	// The shared instance
	private static SynchPlugin plugin;

	/**
	 * The constructor
	 */
	public SynchPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static <T> T getService(Class<T> serviceInterface) {
		return Designer.getService(getDefault().getBundle().getBundleContext(), serviceInterface);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SynchPlugin getDefault() {
		return plugin;
	}

}
