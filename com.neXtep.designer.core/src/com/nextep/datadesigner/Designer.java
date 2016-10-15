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
package com.nextep.datadesigner;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import com.nextep.datadesigner.exception.CommandFinishException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IInvokable;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.IMarkerService;

/**
 * A global class providing core helpers, convenience methods, or storing global-scope elements.
 * This class also provides properties management of the core plugin.
 * 
 * @author Christophe Fondacci
 */
public class Designer {

	protected static final String EXTENSION_SELID = "com.neXtep.designer.core.SelectionInvoker";
	private static final Log log = LogFactory.getLog(Designer.class);
	// private static final String PROPERTY_ICON_SIZE = "nextep.designer.icon.size";
	private static Designer instance = null;
	// private Properties props = null;
	private Object dragObject;
	private String context;
	private static boolean debugging = false;
	private static boolean unitTest = false;
	private static boolean terminationSignal = false;
	private IListenerService listenerService = null;
	private static IProgressMonitor monitor;

	public Designer() {
		instance = this;
	}

	public void initialize() {

	}

	public static Designer getInstance() {
		if (instance == null) {
			instance = new Designer();
		}
		return instance;
	}

	/**
	 * This method indicates whether the specified object can be modified. A flag could be specified
	 * to indicate that you would like to raise an exception rather than returning a flag.
	 * 
	 * @param o object to check
	 * @param raise <code>true</code> to raise an exception when the object is not modifiable,
	 *        <code>false</code> to always return a flag
	 * @return <code>true</code> if the object is modifiable, else :<br>
	 *         &nbsp;&nbsp;* returning <code>false</code> when raise argument is <code>false</code><br>
	 *         &nbsp;&nbsp;* raising an {@link ErrorException} when raise argument is
	 *         <code>true</code><br>
	 */
	public static boolean checkIsModifiable(Object o, boolean raise) {
		ILockable<?> vc = null;
		// Have we got a lockable ?
		if (o instanceof ILockable<?>) {
			vc = (ILockable<?>) o;
		} else if (o instanceof IAdaptable) {
			// If not can we adapt to a lockable ?
			vc = (ILockable<?>) ((IAdaptable) o).getAdapter(ILockable.class);
			if (vc == null) {
				// Not adaptable, should be modifiable
				return true;
			}
		} else {
			// Not lockable nor adaptable to a lockable, it is modifiable
			return true;
		}
		if (vc.updatesLocked()) {
			if (raise) {
				throw new ErrorException(vc.getType().getName() + " <" + vc.getName()
						+ "> is not modifiable, please check out first and try again.");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Retrieves a property from the core neXtep preference store as a boolean value
	 * 
	 * @param key key of the property to retrieve
	 * @return the boolean value of this property
	 */
	public boolean getPropertyBool(String key) {
		final boolean value = Platform.getPreferencesService().getBoolean(CorePlugin.PLUGIN_ID,
				key, false, new IScopeContext[] { new InstanceScope(), new DefaultScope() });
		return value;
	}

	public void setProperty(String key, String value) {
		IEclipsePreferences prefs = new InstanceScope().getNode(CorePlugin.PLUGIN_ID);
		prefs.put(key, value);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			log.error("Unable to store preference : " + key + "=" + value);
		}
	}

	public void setDragObject(Object dragging) {
		this.dragObject = dragging;
	}

	public Object getDragObject() {
		return dragObject;
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	public String getFullPath(String relativePath) {

		try {
			CorePlugin plugin = CorePlugin.getDefault();
			URL url = plugin.getBundle().getEntry(relativePath); // getDescriptor().getInstallURL();
			url = FileLocator.resolve(url);
			return url.toExternalForm();
		} catch (Exception e) {
			throw new ErrorException(e);
		}
	}

	/**
	 * Invokes the specified action from the selection invoker extension.
	 * 
	 * @param actionName name of the action to invoke
	 * @param args arguments to pass to the invoker
	 * @return the object returned by the selection invocation
	 */
	public Object invokeSelection(String actionName, Object... args) {
		return invokeSelection(EXTENSION_SELID, actionName, args);
	}

	/**
	 * Invokes the specified action from the first matching extension. Extension should implement an
	 * IInvokable interface.
	 * 
	 * @param extensionID ID of the extension to look for (which must have a <code>name</code> and
	 *        <code>class</code> tag)
	 * @param actionName name of the action to invoke
	 * @param args arguments to pass to the selection
	 * @return the object returned by the invocation
	 */
	public Object invokeSelection(String extensionID, String actionName, Object... args) {
		log.debug("Looking for action <" + actionName + ">");
		IConfigurationElement extensionFactory = getExtension(extensionID, "name", actionName);
		// If we have found a matching factory
		if (extensionFactory != null) {
			try {
				IInvokable selectionInvoker = (IInvokable) extensionFactory
						.createExecutableExtension("class");

				return selectionInvoker.invoke(args);
			} catch (CoreException e) {
				throw new ErrorException("Unable to instantiate factory of <"
						+ extensionFactory.getAttribute("name") + ">, exception in child plugin.",
						e);
			}
		} else {
			throw new ErrorException("No matching plugin found, check your plugin configuration");
		}
	}

	/**
	 * Loads the specified command name and executes it with the provided command arguments,
	 * returning the result of the command execution.
	 * 
	 * @param commandName name of the command to run
	 * @param args arguments to pass to the command
	 * @return the result of the command
	 */
	public Object runCommand(String commandName, Object... args) {
		IConfigurationElement elt = Designer.getInstance().getExtension(
				ICommand.COMMAND_EXTENSION_ID, "name", commandName); //$NON-NLS-1$
		try {
			ICommand cmd = (ICommand) elt.createExecutableExtension("class"); //$NON-NLS-1$
			return cmd.execute(args);
		} catch (CoreException e) {
			throw new ErrorException(e);
		}

	}

	/**
	 * Retrieves the eclipse extension of the given id which matches a specific attribute.<br>
	 * Convenience method.
	 * 
	 * @param extensionID id of the extension to load
	 * @param keyAttribute key attribute to match
	 * @param matchingValue value to match
	 * @return the extension, if 1 and only one extension has been found
	 */
	public IConfigurationElement getExtension(String extensionID, String keyAttribute,
			String matchingValue) {
		Collection<IConfigurationElement> exts = getExtensions(extensionID, keyAttribute,
				matchingValue);
		if (exts.size() == 0) {
			return null;
		}

		return exts.iterator().next();
	}

	public Collection<IConfigurationElement> getExtensions(String extensionID, String keyAttribute,
			String matchingValue) {

		return getExtensions(extensionID, "*", keyAttribute, matchingValue); //$NON-NLS-1$
	}

	public Collection<IConfigurationElement> getExtensions(String extensionID,
			String contributionName, String keyAttribute, String matchingValue) {
		List<IConfigurationElement> extensions = new ArrayList<IConfigurationElement>();
		// Looking from plugin contributions
		IConfigurationElement[] contributions = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(extensionID);
		for (int i = 0; i < contributions.length; i++) {
			if ("*".equals(contributionName) || contributions[i].getName().equals(contributionName)) { //$NON-NLS-1$
				String attributeVal = contributions[i].getAttribute(keyAttribute);
				if ((attributeVal != null && attributeVal.equalsIgnoreCase(matchingValue))
						|| "*".equals(matchingValue)) { //$NON-NLS-1$
					extensions.add(contributions[i]);
				} else if (attributeVal == null
						&& ("".equals(matchingValue) || matchingValue == null)) {
					extensions.add(contributions[i]);
				}
			}
		}
		return extensions;
	}

	/**
	 * @return the currently connected repository user
	 */

	/**
	 * Are we in deep debug mode. Plugins should rely on this to perform some extra debug processes.
	 * 
	 * @return the current debug mode
	 */
	public static boolean isDebugging() {
		return debugging;
	}

	/**
	 * Global debug flag.
	 * 
	 * @param debugging
	 */
	public static void setDebugging(boolean debugging) {
		Designer.debugging = debugging;
	}

	/**
	 * Defines the listener service which should be used to register / unregister nextep listeners.
	 * 
	 * @param listenerService the global listener service
	 */
	public void setListenerService(IListenerService listenerService) {
		this.listenerService = listenerService;
	}

	/**
	 * @return the global listener service to be used for global listener registration.
	 */
	public static IListenerService getListenerService() {
		return getInstance().getInternalListenerService();
	}

	public IListenerService getInternalListenerService() {
		return listenerService;
	}

	/**
	 * Runs the specified list of commands and return all results in a list.
	 * 
	 * @param commands commands to execute.
	 * @return a list of command results
	 */
	public static List<?> runCommands(ICommand... commands) {
		List<Object> results = new ArrayList<Object>();
		try {
			Object lastResult = null;
			for (ICommand c : commands) {
				lastResult = c.execute(lastResult);
				results.add(lastResult);
			}
			return results;
		} catch (CommandFinishException e) {
			return results;
		}
	}

	/**
	 * Defines the global progress monitor (typically used during splash screen).
	 * 
	 * @param monitor current progress monitor
	 */
	public static void setProgressMonitor(IProgressMonitor monitor) {
		Designer.monitor = monitor;
	}

	/**
	 * @return the current global progress monitor or <code>null</code> if none.
	 */
	public static IProgressMonitor getProgressMonitor() {
		return Designer.monitor;
	}

	/**
	 * Retrieves the global context
	 * 
	 * @return current global context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Defines the global context (it will typically be the database vendor name, but we call it
	 * context here because core plugin cannot know what is a database vendor).
	 * 
	 * @param context global context
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * Defines that designer is running unit tests. Useful to deactivate some user-prompting
	 * features to automate tests.
	 * 
	 * @param unitTest
	 */
	public static void setUnitTest(boolean unitTest) {
		Designer.unitTest = unitTest;
	}

	/**
	 * @return <code>true</code> if neXtep is currently running a unit test, else <code>false</code>
	 */
	public static boolean isUnitTest() {
		return unitTest;
	}

	/**
	 * @return the root marker provider, aggregating all marker information
	 */
	public static IMarkerService getMarkerProvider() {
		return CorePlugin.getService(IMarkerService.class);
	}

	/**
	 * Sets the termination signal indicating that any running job should stop right now cause we
	 * may need to restart the workbench, to close the perspective or to perform an action which
	 * would otherwise cause deadlocks.
	 * 
	 * @param terminate the termination flag
	 */
	public static void setTerminationSignal(boolean terminate) {
		Designer.terminationSignal = terminate;
	}

	/**
	 * @return the termination flag
	 */
	public static boolean getTerminationSignal() {
		return terminationSignal;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getService(BundleContext context, Class<T> serviceInterface) {
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
