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
package com.nextep.designer.core.factories;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.model.ITypedObjectController;
import com.nextep.designer.core.model.impl.DefaultController;

public class ControllerFactory {

	private static final Log log = LogFactory.getLog(ControllerFactory.class);
	private static final String EXTENSION_ID = "com.neXtep.designer.core.elementType"; //$NON-NLS-1$
	private static final String CONTRIB_CONTROLLER = "controller"; //$NON-NLS-1$
	private static Map<ControllerKey, ITypedObjectController> controllers = new HashMap<ControllerKey, ITypedObjectController>();

	/**
	 * Key for controller cached instances (singletons).
	 */
	private static class ControllerKey extends MultiKey {

		/** Generated serial ID */
		private static final long serialVersionUID = -1934403071727252584L;

		public ControllerKey(IElementType type, String context) {
			super(type, context);
		}
	}

	public static ITypedObjectController getController(Object model) {
		if (model instanceof IReference) {
			return getController(IElementType.getInstance(IReference.TYPE_ID));
		} else if (model instanceof ITypedObject) {
			return getController(((ITypedObject) model).getType());
		}

		// Iterating on class entries
		for (IElementType type : IElementType.values()) {
			if (type.getInterface() != null && type.getInterface().isInstance(model)) {
				return getController(type);
			}
		}

		throw new ErrorException(MessageFormat.format(CoreMessages
				.getString("controller.factory.notFound"), (model == null ? "null" : model //$NON-NLS-1$ //$NON-NLS-2$
				.getClass().getName())));
	}

	/**
	 * Retrieves the controller of a given element type. A context is used to select the most
	 * appropriate controller. For now, the strategy is to select controller with a non null context
	 * first, and to downgrade to empty-context controllers if no contextual controllers are
	 * defined.<br>
	 * TODO: Add a setContext method called by DBGM with the current vendor and filter controllers
	 * with this context
	 * 
	 * @param type element type for which we want a controller
	 * @return the corresponding controller. An Unknown controller will be returned if no controller
	 *         has been found for the specified element type.
	 */
	public static ITypedObjectController getController(IElementType type) {
		// log.debug("ControllerFactory: Looking for type <" + type.getName() + ">");
		String currentContext = Designer.getInstance().getContext();
		ControllerKey key = new ControllerKey(type, currentContext);
		ITypedObjectController ctrl = controllers.get(key);
		if (ctrl == null) {
			Collection<IConfigurationElement> confs = Designer.getInstance().getExtensions(
					EXTENSION_ID, CONTRIB_CONTROLLER, "typeId", type.getId()); //$NON-NLS-1$ 
			try {
				for (IConfigurationElement conf : confs) {

					// The first controller defined with a non null context is chosen.
					String contextName = conf.getAttribute("context"); //$NON-NLS-1$
					if (contextName != null && !"".equals(contextName) //$NON-NLS-1$
							&& contextName.equals(currentContext)) {
						ctrl = (ITypedObjectController) conf.createExecutableExtension("class"); // controllers.get(type); //$NON-NLS-1$
						break;
					} else if (contextName == null || "".equals(contextName)) { //$NON-NLS-1$
						ctrl = (ITypedObjectController) conf.createExecutableExtension("class"); // controllers.get(type); //$NON-NLS-1$
					}
				}
				log.debug(MessageFormat.format(
						CoreMessages.getString("controller.factory.loaded"), ctrl.getClass() //$NON-NLS-1$
								.getName(), type.getId()));
			} catch (Exception e) {
				ctrl = getDefaultController(type);
			}
			if (ctrl == null) {
				ctrl = getDefaultController(type);
			}
			ctrl.setType(type);
			controllers.put(key, ctrl);
		}
		return ctrl;
	}

	private static ITypedObjectController getDefaultController(IElementType type) {
		ITypedObjectController ctrl;
		// Loading VERSIONABLE controller by default
		// TODO: change this as this imply a dependency from core to vcs which should not be
		// possible
		if (!"VERSIONABLE".equals(type.getId())) { //$NON-NLS-1$
			ctrl = getController(IElementType.getInstance("VERSIONABLE")); //$NON-NLS-1$
		} else {
			log.error(MessageFormat.format(
					CoreMessages.getString("controller.factory.notFound"), type.getId())); //$NON-NLS-1$
			ctrl = new DefaultController();
		}
		return ctrl;
	}

}
