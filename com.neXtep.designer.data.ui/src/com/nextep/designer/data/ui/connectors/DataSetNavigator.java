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
package com.nextep.designer.data.ui.connectors;

import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.data.ui.controllers.DataSetUIController;
import com.nextep.designer.dbgm.model.IDataSet;


/**
 * @author Christophe Fondacci
 *
 */
public class DataSetNavigator extends UntypedNavigator  {

	public DataSetNavigator(IDataSet model, DataSetUIController controller) {
		super(model, controller);
//		for(IDataLine l : model.getDataLines()) {
//			if(l!=null)
//				addConnector(ControllerFactory.getController(l).initializeNavigator(l));
//		}
	}

//	/**
//	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
//	 */
//	@Override
//	public Image getConnectorIcon() {
//		return DBGMImages.ICON_DATASET;
//	}


	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case DATALINE_ADDED:
//			addConnector(ControllerFactory.getController(data).initializeNavigator(data));
			break;
		case DATALINE_REMOVED:
//			removeConnector(this.getConnector(data));
			break;
		}
		refreshConnector();

	}

}
