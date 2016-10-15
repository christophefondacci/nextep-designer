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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.ui.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.services.IUIService;

/**
 * Default UI services implementation.
 * 
 * @author Christophe Fondacci
 */
public class UIService implements IUIService {

	private static final String EXTENSION_ID_COMPONENT = "com.neXtep.designer.ui.typeUIComponent"; //$NON-NLS-1$
	private static final String EXTENSION_ID_PAGE = "com.neXtep.designer.ui.typeEditorPage"; //$NON-NLS-1$
	private static final String EXTENSION_ID_EDITOR_PAGE = "com.neXtep.designer.ui.editorTypedPageContribution"; //$NON-NLS-1$

	private static final String ATTR_CONTENT_PROVIDER = "contentProvider"; //$NON-NLS-1$
	private static final String ATTR_LABEL_PROVIDER = "labelProvider"; //$NON-NLS-1$
	private static final String ATTR_ACTION_PROVIDER = "actionProvider"; //$NON-NLS-1$
	private static final String ATTR_TITLE = "title"; //$NON-NLS-1$
	private static final String ATTR_ICON = "icon"; //$NON-NLS-1$
	private static final String ATTR_EDITOR_TYPE_ID = "editorTypeId"; //$NON-NLS-1$
	private static final String ATTR_PAGE_TYPE_ID = "pageTypeId"; //$NON-NLS-1$
	private static final String ATTR_VENDOR = "dbVendor"; //$NON-NLS-1$
	private static final String ATTR_SINGLE_EDITION = "singleEdition"; //$NON-NLS-1$
	private static final String ATTR_FORM_PART = "editorPageClass"; //$NON-NLS-1$

	private static final Log LOGGER = LogFactory.getLog(UIService.class);

	private Map<String, Image> pageImagesMap = new HashMap<String, Image>();

	@Override
	public List<IUIComponent> getEditorComponentsFor(IElementType type, DBVendor vendor) {
		final Collection<IConfigurationElement> confs = Designer.getInstance().getExtensions(
				EXTENSION_ID_COMPONENT, "typeId", type.getId()); //$NON-NLS-1$
		final List<IUIComponent> components = new ArrayList<IUIComponent>();
		final List<IUIComponent> vendorComponents = new ArrayList<IUIComponent>();
		for (IConfigurationElement elt : confs) {
			final String vendorStr = elt.getAttribute("vendor"); //$NON-NLS-1$
			boolean isVendorSpecific = false;
			// Is there a vendor defined in the contribution ?
			if (vendorStr != null && !"".equals(vendorStr.trim())) { //$NON-NLS-1$
				// If yes, does it match the requested vendor ?
				if (vendorStr.equals(vendor.name())) {
					// If yes
					isVendorSpecific = true;
				} else {
					continue;
				}
			}
			try {
				IUIComponent component = (IUIComponent) elt.createExecutableExtension("class"); //$NON-NLS-1$
				if (isVendorSpecific) {
					vendorComponents.add(component);
				} else {
					components.add(component);
				}
			} catch (CoreException e) {
				LOGGER.error(
						"Unable to instantiate a UIComponent contribution : " + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		components.addAll(vendorComponents);
		return components;
	}

	@Override
	public List<IUIComponent> getEditorComponentsFor(ITypedObject object, DBVendor vendor) {
		if (object == null) {
			return Collections.emptyList();
		} else {
			return getEditorComponentsFor(object.getType(), vendor);
		}
	}

	/**
	 * The OSGI de-activation method, allowing to dispose created image resources
	 */
	public void dispose() {
		LOGGER.info("Disposing core UI editor resources..."); //$NON-NLS-1$
		for (Image img : pageImagesMap.values()) {
			img.dispose();
		}
	}

	@Override
	public void bindController(Object model) {
		if (model instanceof IObservable) {
			final IListenerService listenerService = Designer.getListenerService();
			// Registering the controller as a listener to the model
			if (model instanceof ITypedObject) {
				final ITypedObjectUIController controller = UIControllerFactory
						.getController(model);
				if (controller != null) {
					listenerService.registerListener(this, (IObservable) model, controller);
				}
			}
		}
	}
}
