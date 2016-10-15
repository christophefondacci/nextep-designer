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
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.ui.BENGImages;

/**
 * @author Christophe Fondacci
 *
 */
public class DeliveryTypeNavigator extends UntypedNavigator {

	private DeliveryType type;
	public DeliveryTypeNavigator(DeliveryType type) {
		super(null,null);
		this.type = type;
	}
	public DeliveryType getDeliveryType() {
		return type;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return BENGImages.ICON_CONTAINER;
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#getModel()
	 */
	@Override
	public Object getModel() {
		return type;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getTitle()
	 */
	@Override
	public String getTitle() {
		return type.getLabel();
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#compareTo(com.nextep.datadesigner.gui.model.INavigatorConnector)
	 */
	@Override
	public int compareTo(INavigatorConnector o) {
		if(o instanceof DeliveryTypeNavigator) {
			DeliveryType comparedType = ((DeliveryTypeNavigator)o).getDeliveryType();
			for(int i = 0 ; i < DeliveryType.values().length ; i++) {
				if(DeliveryType.values()[i] == type) {
					return -1;
				}
				if(DeliveryType.values()[i] == comparedType) {
					return 1;
				}
			}
		}
		// Default
		return super.compareTo(o);
	}
}
