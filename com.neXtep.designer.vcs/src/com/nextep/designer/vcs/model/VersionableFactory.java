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
/**
 *
 */
package com.nextep.designer.vcs.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ISynchronizable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.impl.Activity;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * Factory of typed versionables.<br>
 * i.e. If you have a factory VersionableFactory<IBasicTable>, then a call to
 * <code>createVersionable</code> would return a <code>IVersionable</code>
 * object that would return an IVersionControlled structure when calling the
 * method <code>getVersionedObject</code>. The <code>IVersionControlled</code>
 * object will actually be your object. But to avoid casting objects and risking
 * a ClassCastException, the <code>IVersionControlled</code> interface provides
 * the method <code>getModel</code> which will return you the original
 * <code>IBasicTable</code> object.<br>
 * 
 * So this means that VersionabeFactory, IVersionable and IVersionControlled
 * interfaces are all typed and linked for consistent usage. Though the user
 * will be able to manipulate either the object model (getModel), the version
 * controlled object (for generic locking / unlocking facilities) or the
 * versionable object (for version management, checkin / check out).
 * 
 * @param T
 *            type of versionable to create
 * @author Christophe Fondacci
 * 
 */
public abstract class VersionableFactory {

	// private static final String FACTORY_PREFIX_PROPERTY = "factory.";
	private static final Log log = LogFactory.getLog(VersionableFactory.class);
	private static final String EXTENSION_ID = "com.neXtep.designer.vcs.VersionableFactory";

	/**
	 * Creates a versionable object from the specified class. A property lookup
	 * is performed to determine which concrete factory will provide the
	 * implementation.<br>
	 * The specified class instance could then be retrieved by calling
	 * <code>getVersionnedObject().getModel()</code> on the returned
	 * versionable.
	 * 
	 * @param clazz
	 *            class for which we want a versionable
	 * @return an IVersionable object of the specified class
	 */
	public static <T> IVersionable<T> createVersionable(Class<T> clazz) {
		final IWorkspaceService workspaceService = CorePlugin.getService(IWorkspaceService.class);
		final DBVendor vendor = workspaceService.getCurrentWorkspace().getDBVendor();
		return createVersionable(clazz, vendor);
	}

	/**
	 * Creates a versionable object from the specified class for the given
	 * vendor. A property lookup is performed to determine which concrete
	 * factory will provide the implementation.<br>
	 * The specified class instance could then be retrieved by calling
	 * <code>getVersionnedObject().getModel()</code> on the returned
	 * versionable.
	 * 
	 * @param clazz
	 *            class for which we want a versionable
	 * @return an IVersionable object of the specified class
	 */
	public static <T> IVersionable<T> createVersionable(Class<T> clazz, DBVendor vendor) {
		IVersionable<T> v = (IVersionable<T>) getFactory(
				getFactoryExtension(clazz.getName(), vendor)).createVersionable();
		// Registering versions
		if (v.getVersion() == null) {
			v.setVersion(VersionFactory.getUnversionedInfo(new Reference(v.getType(), v.getName(),
					v), Activity.getDefaultActivity()));
		}
		return v;
	}

	public static <T> IVersionable<T> copy(IVersionable<T> source) {
		IConfigurationElement conf = getFactoryExtension(source.getType().getInterface().getName());
		if (conf == null) {
			// If no factory has been found, we try with all implemented
			// interface
			Class<?>[] interfaces = source.getClass().getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				conf = getFactoryExtension(interfaces[i].getName());
				if (conf != null) {
					break;
				}
			}
		}
		return getFactory(conf).copyOf(source);
	}

	public static void copy(IVersionable<?> source, IVersionable<?> destination) {
		IConfigurationElement conf = getFactoryExtension(source.getClass().getName());
		if (conf == null) {
			// If no factory has been found, we try with all implemented
			// interface
			Class<?>[] interfaces = source.getClass().getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				conf = getFactoryExtension(interfaces[i].getName());
				if (conf != null) {
					break;
				}
			}
		}
		getFactory(conf).rawCopy(source, destination);
	}

	private static IConfigurationElement getFactoryExtension(String className) {
		final IWorkspaceService workspaceService = CorePlugin.getService(IWorkspaceService.class);
		return getFactoryExtension(className, workspaceService.getCurrentWorkspace().getDBVendor());
	}

	/**
	 * Retrieves the plugin extension from a given class name. The class name
	 * will checked against the <code>versionableInterfaceName</code> attribute
	 * of the extension point.
	 * 
	 * @param className
	 *            class name of the objects managed by the factory
	 * @param vendor
	 *            the {@link DBVendor} to get a factory for
	 * @return the eclipse configuration element
	 */
	private static IConfigurationElement getFactoryExtension(String className, DBVendor vendor) {
		log.debug("Looking for factory of <" + className + ">");
		// Looking from plugin contributions
		IConfigurationElement[] contributions = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_ID);
		IConfigurationElement extensionFactory = null;
		for (int i = 0; i < contributions.length; i++) {
			if (contributions[i].getAttribute("versionableInterfaceName").equals(className)) {

				String vendorName = contributions[i].getAttribute("vendor");
				if (vendorName != null && !"".equals(vendorName)
						&& vendor.name().equals(vendorName)) {
					extensionFactory = contributions[i];
					break;
				} else if (vendorName == null
						|| (vendorName != null && "".equals(vendorName.trim()))) {
					extensionFactory = contributions[i];
				}
			}
		}
		if (extensionFactory != null) {
			log.debug("Loaded factory '" + extensionFactory.getAttribute("class") + "' for <"
					+ className + ">");
		}
		return extensionFactory;
	}

	/**
	 * Creates the factory object from the plugin configuration element. This
	 * method may raise an exception if there is a problem while initializing
	 * the remote class or if no configuration element is provided.
	 * 
	 * @param extensionFactory
	 *            configuration element corresponding to the factory
	 * @return an instantiated factory
	 */
	private static VersionableFactory getFactory(IConfigurationElement extensionFactory) {
		// If we have found a matching factory
		if (extensionFactory != null) {
			try {
				VersionableFactory factory = (VersionableFactory) extensionFactory
						.createExecutableExtension("class");
				return factory;
			} catch (CoreException e) {
				throw new ErrorException("Unable to instanciate factory of <"
						+ extensionFactory.getAttribute("name") + ">, exception in child plugin.",
						e);
			}
		} else {
			throw new ErrorException("No matching plugin found, check your plugin configuration");
		}
	}

	/**
	 * Creates a new versionable of type T. This class T will be retrieved by a
	 * call to <code>getVersionedObject().getModel()</code> on the returned
	 * versionable.
	 * 
	 * @return a versionable typed by this factory type.
	 */
	public abstract IVersionable<?> createVersionable();

	/**
	 * Copies the specified versionable and returns the new version copy.
	 * Typically this method should be called on checkout.
	 * 
	 * @param source
	 *            the IVersionable object to copy
	 * @return an exact copy of the source versionable
	 */
	public <T> IVersionable<T> copyOf(IVersionable<T> source) {
		IVersionable<T> copy = (IVersionable<T>) createVersionable();
		rawCopy(source, copy);
		return copy;
	}

	public abstract void rawCopy(IVersionable<?> source, IVersionable<?> destination);

	/**
	 * Copy the basic <code>IVersionable</code> attributes from an object to
	 * another. The copied object (destination parameter) will have a new
	 * Unversioned state.
	 * 
	 * @param source
	 *            source object to copy
	 * @param destination
	 *            destination "copied" object
	 */
	protected <T> void versionCopy(IVersionable<T> source, IVersionable<T> destination) {
		destination.setName(source.getName());
		destination.setContainer(source.getContainer());
		destination.setDescription(source.getDescription());
		destination.setVersion(source.getVersion());
		if (destination instanceof ISynchronizable && source instanceof ISynchronizable) {
			((ISynchronizable) destination).setSynched(((ISynchronizable) source).getSynchStatus());
		}
		// Registering listeners
		// Designer.getListenerService().switchListeners(source, destination);

	}
}
