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
package com.nextep.datadesigner.gui.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * Root GUI services.
 * 
 * @author Christophe Fondacci
 */
public class GUIService {

	private static Map<MultiKey, INavigatorConnector> modelNavigatorsMap = new HashMap<MultiKey, INavigatorConnector>();
	private static final Log log = LogFactory.getLog(GUIService.class);

	/**
	 * Registers a navigator model. This method attaches the navigator to its model for the UI layer
	 * to be able to retrieve the navigator given the model object.
	 * 
	 * @param navigator
	 */
	public static synchronized void registerNavigator(INavigatorConnector navigator, Tree t) {
		// TODO Beurk: this to avoid registering reference navigators
		if (!("com.nextep.datadesigner.vcs.gui.navigators.ReferenceNavigator".equals(navigator
				.getClass().getName()))) {
			modelNavigatorsMap.put(new MultiKey(navigator.getModel(), t), navigator);
			modelNavigatorsMap.put(new MultiKey(navigator.getModel(), null), navigator);
		}
	}

	/**
	 * Unregisters this navigator.
	 * 
	 * @param navigator
	 */
	public static synchronized void unregisterNavigator(INavigatorConnector navigator, Tree t) {
		modelNavigatorsMap.remove(new MultiKey(navigator.getModel(), t));
	}

	/**
	 * Retrieves the navigator of the given object model.
	 * 
	 * @param model model object to retrieve the navigator of
	 * @return the corresponding navigator, or <code>null</code> if none
	 */
	public static INavigatorConnector getNavigator(Object model, Tree t) {
		return modelNavigatorsMap.get(new MultiKey(model, t));
	}

	public static INavigatorConnector getNavigator(Object model) {
		return getNavigator(model, null);
	}

	public static List<IEditorReference> getDependentEditors(Object o) {
		// We retrieve all editors pointing to a reference of a removed element to close them
		final List<IEditorReference> editorsToClose = new ArrayList<IEditorReference>();
		final List<Object> elts = new ArrayList<Object>();
		if (o instanceof IReferenceContainer) {
			elts.addAll(((IReferenceContainer) o).getReferenceMap().values());
		}
		elts.add(o);
		for (Object elt : elts) {
			if (elt instanceof ITypedObject) {
				ITypedObject model = (ITypedObject) elt;
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.getEditorReferences() != null) {
					for (IEditorReference r : PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().getEditorReferences()) {
						if (!editorsToClose.contains(r)) {
							try {
								IEditorInput input = r.getEditorInput();
								if (input instanceof IModelOriented<?>) {
									Object obj = ((IModelOriented<?>) input).getModel();
									if (obj == model) {
										editorsToClose.add(r);
									}
								}
							} catch (PartInitException e) {
								log.warn("Unable to read editor state.");
							}
						}
					}
				}
			}
		}
		return editorsToClose;
	}
}
