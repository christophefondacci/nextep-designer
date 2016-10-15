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
package com.nextep.datadesigner.dbgm.gui.navigators;

import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.helper.DatatypeHelper;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 *
 */
public class TypeColumnNavigator extends UntypedNavigator {

	public TypeColumnNavigator(ITypeColumn column,ITypedObjectUIController controller) {
		super(column,controller);
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getType()
	 */
	@Override
	public IElementType getType() {
		return null;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		ITypeColumn col = (ITypeColumn)getModel();
		// Returning datatype icon
		if(col != null) {
			return DatatypeHelper.getDatatypeIcon(col.getDatatype());
		} else {
			return ImageFactory.ICON_BLANK;
		}
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();
	}

}
