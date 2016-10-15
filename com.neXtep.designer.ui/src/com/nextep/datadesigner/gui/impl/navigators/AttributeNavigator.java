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
package com.nextep.datadesigner.gui.impl.navigators;

import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IAttribute;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class AttributeNavigator extends UntypedNavigator implements
		INavigatorConnector {

	private IAttribute attribute;
	public AttributeNavigator(IAttribute attribute) {
		super(null,null);
		this.attribute=attribute;
	}
	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(Object model) {
		this.attribute = (IAttribute)model;
		if(getSWTConnector()!=null) {
			refreshConnector();
		}
	}



	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return ImageFactory.ICON_ATTRIBUTE;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance("ATTRIBUTE");
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getModel()
	 */
	@Override
	public Object getModel() {
		return attribute;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		if(attribute!=null) {
			final int ind = attribute.getValue() != null ? attribute.getValue().toString().indexOf('\n') : -1;
			if(ind>0) {
				return attribute.getName() + " = " + (attribute.getValue() !=null ? attribute.getValue().toString().substring(0, ind) : attribute.getValue());
			}
		}
		return attribute == null ? "" : attribute.getName() + " = " + attribute.getValue();
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// TODO Auto-generated method stub

	}

}
