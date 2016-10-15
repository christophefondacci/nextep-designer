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

import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 *
 */
public class IndexNavigator extends UntypedNavigator {

	public IndexNavigator(IIndex index, ITypedObjectUIController controller) {
		super(index,controller);
	}
	@Override
	public void initializeChildConnectors() {
		IIndex index = (IIndex)getModel();
		for(IReference r : index.getIndexedColumnsRef()) {
			addConnector(UIControllerFactory.getController(IElementType.getInstance(IReference.TYPE_ID)).initializeNavigator(r));
		}		
	}
	/**
	 * Overriding default behaviour to avoid columns sort
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#addConnector(com.nextep.datadesigner.gui.model.INavigatorConnector)
	 */
	public void addConnector(INavigatorConnector c) {
		getConnectors().add(c);
		c.setParent(this);
		if(initialized) {
			createConnector(c, getConnectors().indexOf(c));
		}
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case COLUMN_ADDED:
			addConnector(UIControllerFactory.getController(IElementType.getInstance("REFERENCE")).initializeNavigator(data));
			break;
		case COLUMN_REMOVED:
			removeConnector(getConnector(data));
			break;
		case GENERIC_CHILD_ADDED:
			if(data != null) {
				this.addConnector(UIControllerFactory.getController(data).initializeNavigator(data));
			}
			break;
		case GENERIC_CHILD_REMOVED:
			this.removeConnector(this.getConnector(data));
			break;			
		}
		refreshConnector();

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#getTitle()
	 */
	@Override
	public String getTitle() {
		IIndex index = (IIndex)getModel();
		try {
			return "[" + index.getIndexedTable().getName() + "] " + index.getIndexName();
		} catch( ErrorException e) {
			return "[Unresolved] " + index.getName();
		}
	}
}
