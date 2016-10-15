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
package com.nextep.designer.beng.model;

import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This class represents an external file of a delivery
 * module. External files are simply copied in the exported
 * module and added to the delivery descriptor. 
 * 
 * @author Christophe Fondacci
 */
public interface IExternalFile extends INamedObject, IdentifiedObject {

	/**
	 * @return the directory path of the file
	 */
	public String getDirectory();
	/**
	 * Defines the directory path of the file
	 * @param directory directory of this file
	 */
	public void setDirectory(String directory);
	/**
	 * @return the delivery module in which this external file
	 * 		   is defined.
	 */
	public IDeliveryModule getDelivery();
	/**
	 * Defines the delivery module for which this external file
	 * is defined.
	 * 
	 * @param delivery module owning this external file.
	 */
	public void setDelivery(IDeliveryModule delivery);
	/**
	 * @return the position of this external file among all defined
	 * 		   external files.
	 */
	public int getPosition();
	/**
	 * Defines the position of this external file.
	 * @param position new position
	 */
	public void setPosition(int position);
	
}
