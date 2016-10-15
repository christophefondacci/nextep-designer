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
package com.nextep.datadesigner.beng.gui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class DeliveryItemNavigator extends UntypedNavigator {

	private INavigatorConnector contentNavigator;
	public DeliveryItemNavigator(IDeliveryItem<?> item) {
		super(item,null);
		initializeChildConnectors();
	}
	@Override
	public void initializeChildConnectors() {
		IDeliveryItem<?> item = (IDeliveryItem<?>)getModel();
		contentNavigator = UIControllerFactory.getController(item.getContent()).initializeNavigator(item.getContent());	
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return contentNavigator.getConnectorIcon();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getTitle()
	 */
	@Override
	public String getTitle() {
		return contentNavigator.getTitle();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#create(org.eclipse.swt.widgets.TreeItem, int)
	 */
	@Override
	public TreeItem create(TreeItem parent, int treeIndex) {
		TreeItem i = contentNavigator.create(parent, treeIndex);
		i.setData(this);
		return i;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getSWTConnector()
	 */
	@Override
	public TreeItem getSWTConnector() {
		return contentNavigator.getSWTConnector();
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getType()
	 */
	@Override
	public IElementType getType() {
		return contentNavigator.getType();
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#defaultAction()
	 */
	@Override
	public void defaultAction() {
		contentNavigator.defaultAction();
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#initialize()
	 */
	@Override
	public void initialize() {
		contentNavigator.initialize();
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		contentNavigator.refreshConnector();
	}
	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#releaseConnector()
	 */
	@Override
	public void releaseConnector() {
		contentNavigator.releaseConnector();
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();
	}

}
