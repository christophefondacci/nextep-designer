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
package com.nextep.designer.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.dao.IIdentifiableDAO;
import com.nextep.designer.core.model.IPersistenceAccessor;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.core.model.impl.PersistenceAccessorDelegate;
import com.nextep.designer.core.services.IConnectionService;
import com.nextep.designer.core.services.IRepositoryService;

public class CorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.neXtep.designer.core"; //$NON-NLS-1$
	// The shared instance
	private static CorePlugin plugin;

	/**
	 * The constructor
	 */
	public CorePlugin() {
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
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Provides the {@link IRepositoryService} implementation
	 * 
	 * @return a {@link IRepositoryService} implementation
	 */
	public static IRepositoryService getRepositoryService() {
		return getService(IRepositoryService.class);
	}

	/**
	 * Provides a {@link IPersistenceAccessor} which can access any persistable element.
	 * 
	 * @return a {@link IPersistenceAccessor} implementation
	 */
	public static IPersistenceAccessor<ITypedObject> getPersistenceAccessor() {
		return PersistenceAccessorDelegate.getInstance();
	}

	/**
	 * Provides a factory delegate which will be able to create {@link ITypedObject} of any kind as
	 * soon as the factory is declared in the extension points.
	 * 
	 * @return a {@link ITypedObjectFactory} root implementation
	 * @see ITypedObjectFactory
	 */
	public static ITypedObjectFactory getTypedObjectFactory() {
		return getService(ITypedObjectFactory.class);
	}

	/**
	 * Provides the {@link IConnectionService} singleton.
	 * 
	 * @return the {@link IConnectionService} singleton
	 */
	public static IConnectionService getConnectionService() {
		return getService(IConnectionService.class);
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

	/**
	 * @return
	 */
	public static IIdentifiableDAO getIdentifiableDao() {
		return getService(IIdentifiableDAO.class);
	}
}
