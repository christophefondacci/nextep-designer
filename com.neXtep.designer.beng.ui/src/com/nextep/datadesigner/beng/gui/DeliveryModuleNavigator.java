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
package com.nextep.datadesigner.beng.gui;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.beng.ctrl.DeliveryController;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * The navigator connector of a delivery module.
 *
 * @author Christophe Fondacci
 *
 */
public class DeliveryModuleNavigator extends UntypedNavigator {

	private Map<DeliveryType,INavigatorConnector> typeConnectors;

	public DeliveryModuleNavigator(IDeliveryModule module, DeliveryController controller) {
		super(module,controller);
		typeConnectors = new HashMap<DeliveryType, INavigatorConnector>();
	}

	@Override
	public void initializeChildConnectors() {
		IDeliveryModule module = (IDeliveryModule)getModel();
		// Adding child items
		for(IDeliveryItem<?> item : module.getDeliveryItems()) {
			this.addConnector(new DeliveryItemNavigator(item));
		}
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#createSWTConnector(org.eclipse.swt.widgets.TreeItem, int)
	 */
	@Override
	protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
		TreeItem i = super.createSWTConnector(parent, treeIndex);
		// Adding type connectors
		for(DeliveryType t : DeliveryType.values()) {
			INavigatorConnector c = new DeliveryTypeNavigator(t);
			c.create(i, -1);
			c.initialize();
			typeConnectors.put(t, c);
		}
		return i;
	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
//	 */
//	@Override
//	public Image getConnectorIcon() {
//		return BENGImages.ICON_DEPLOY_UNIT;
//	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		super.refreshConnector();
		// Refreshing type connectors
		for(INavigatorConnector c : typeConnectors.values()) {
			c.refreshConnector();
		}
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case VERSIONABLE_ADDED:
			this.addConnector(UIControllerFactory.getController(data).initializeNavigator(data));
			break;
		case VERSIONABLE_REMOVED:
			this.removeConnector(this.getConnector(data));
			break;
		case ITEM_ADDED:
			IDeliveryItem<?> item = (IDeliveryItem<?>)data;
			addConnector(new DeliveryItemNavigator(item));
			break;
		case ITEM_REMOVED:
			removeConnector(getConnector(data));
			break;
		}
		refreshConnector();
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator#createConnector(com.nextep.datadesigner.gui.model.INavigatorConnector, int)
	 */
	@Override
	protected void createConnector(INavigatorConnector c, int index) {
		if(c instanceof DeliveryItemNavigator) {
			IDeliveryItem<?> item = (IDeliveryItem<?>)c.getModel();
			INavigatorConnector typeConnector = typeConnectors.get(item.getDeliveryType());
			typeConnector.addConnector(c);
		} else {
			super.createConnector(c, index);
		}
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#removeConnector(com.nextep.datadesigner.gui.model.INavigatorConnector)
	 */
	@Override
	public void removeConnector(INavigatorConnector c) {
		if(c instanceof DeliveryItemNavigator) {
			IDeliveryItem<?> item = (IDeliveryItem<?>)c.getModel();
			INavigatorConnector typeConnector = typeConnectors.get(item.getDeliveryType());
			typeConnector.removeConnector(c);
			getConnectors().remove(c);
		} else {
			super.removeConnector(c);
		}
	}
}
