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
package com.nextep.datadesigner.ctrl;

import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;

/**
 * @author Christophe Fondacci
 */
public class UnknownController extends AbstractUIController implements ITypedObjectUIController {

	IElementType type;
	Exception exception;

	public UnknownController(IElementType type, Exception e) {
		this.type = type;
		this.exception = e;
	}

	public class UnknownNavigator extends TypedNavigator {

		public UnknownNavigator() {
			super(null, null);
		}

		/**
		 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
		 */
		@Override
		public Image getConnectorIcon() {
			return ImageFactory.ICON_ERROR;
		}

		/**
		 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getType()
		 */
		@Override
		public IElementType getType() {
			return type;
		}

		/**
		 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
		 */
		@Override
		public String getTitle() {
			return "Unknown element type <" + type.getId() + ">";
		}

		/**
		 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
		 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
		 */
		@Override
		public void handleEvent(ChangeEvent event, IObservable source, Object data) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeGraphical(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new UnknownNavigator();
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	@Override
	public Object newInstance(Object parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
