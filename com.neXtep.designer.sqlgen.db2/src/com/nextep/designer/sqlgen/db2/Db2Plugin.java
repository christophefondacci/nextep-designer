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
package com.nextep.designer.sqlgen.db2;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import com.nextep.datadesigner.Designer;

public class Db2Plugin extends Plugin {

	private static Db2Plugin PLUGIN;

	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		PLUGIN = this;
	}

	public static Db2Plugin getDefault() {
		return PLUGIN;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		PLUGIN = null;
		super.stop(bundleContext);
	}

	public static <T> T getService(Class<T> serviceInterface) {
		return Designer.getService(getDefault().getBundle().getBundleContext(), serviceInterface);
	}
}
