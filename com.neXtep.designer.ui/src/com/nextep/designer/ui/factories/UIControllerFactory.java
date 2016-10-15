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
package com.nextep.designer.ui.factories;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.ctrl.UnknownController;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * A global controller factory. Users of this factory should register their own controllers user the
 * registerController method before any further use of this factory. By default, the factory will
 * have no controller registered, except the internal ones.
 * 
 * @author Christophe Fondacci
 */
public class UIControllerFactory {

	private static final Log log = LogFactory.getLog(UIControllerFactory.class);

	private static Map<ControllerKey, ITypedObjectUIController> controllers = new HashMap<ControllerKey, ITypedObjectUIController>();

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

	public static ITypedObjectUIController getController(Object model) {
		// /////// TO UNCOMMENT AND TEST
		if (model instanceof IReference) {
			return getController(IElementType.getInstance(IReference.TYPE_ID));
		} else if (model instanceof ITypedObject) {
			return getController(((ITypedObject) model).getType());
		}
		// /////// END TO UNCOMMENT

		// Iterating on class entries
		for (IElementType type : IElementType.values()) {
			if (type.getInterface() != null && type.getInterface().isInstance(model)) {
				return getController(type);
			}
		}

		throw new ErrorException("No controller has been found for instance <"
				+ (model == null ? "null" : model.getClass().getName()) + ">");
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
	public static ITypedObjectUIController getController(IElementType type) {
		// log.debug("ControllerFactory: Looking for type <" + type.getName() + ">");
		String currentContext = Designer.getInstance().getContext();
		ControllerKey key = new ControllerKey(type, currentContext);
		ITypedObjectUIController ctrl = controllers.get(key);
		if (ctrl == null) {
			Collection<IConfigurationElement> confs = Designer.getInstance().getExtensions(
					"com.neXtep.designer.ui.typeController", "typeId", type.getId());
			try {
				for (IConfigurationElement conf : confs) {

					// The first controller defined with a non null context is chosen.
					String contextName = conf.getAttribute("context");
					if (contextName != null && !"".equals(contextName)
							&& contextName.equals(currentContext)) {
						ctrl = (ITypedObjectUIController) conf.createExecutableExtension("class"); // controllers.get(type);
						break;
					} else if (contextName == null || "".equals(contextName)) {
						ctrl = (ITypedObjectUIController) conf.createExecutableExtension("class"); // controllers.get(type);
					}
				}
				log.debug("ControllerFactory: Loaded type controller '" + ctrl.getClass().getName()
						+ "' for <" + type.getId() + ">");
			} catch (Exception e) {
				log.error("No controller has been found for element type <" + type.getId() + ">");
				ctrl = new UnknownController(type, e);
			}
			ctrl.setType(type);
			// ctrl = new UIThreadSafeController(ctrl);
			controllers.put(key, ctrl);
		}
		return ctrl;
	}
}
