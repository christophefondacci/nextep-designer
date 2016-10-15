/*******************************************************************************
 * Copyright (c) 2011 neXtep Software and contributors.
 * All rights reserved.
 *
 * This file is part of neXtep designer.
 *
 * NeXtep designer is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General  
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 *
 * NeXtep designer is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General  License for more details.
 *
 * You should have received a copy of the GNU General  License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.beng.model;

import com.neXtep.shared.model.ArtefactType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.beng.model.impl.DeliveryModule;
import com.nextep.designer.core.model.IVendorOriented;

/**
 * This interface represents an item of a delivery. Any item which could be delivered must implement
 * this interface.
 * 
 * @author Christophe Fondacci
 */
public interface IDeliveryItem<T> extends IObservable, INamedObject, IdentifiedObject,
		ITypedObject, Comparable, IVendorOriented {

	/**
	 * @return the content of this delivery item
	 */
	T getContent();

	/**
	 * Generates the delivery to the specified filesystem directory.
	 * 
	 * @param generator the deliveyr module which initiated the generation
	 * @param directoryTarget directory where the artefact should be generated.
	 */
	void generateArtefact(String directoryTarget);

	/**
	 * @return the name of the generated artefact
	 */
	String getArtefactName();

	/**
	 * @return the type of generated artefact which will let the installer know how to deploy it.
	 */
	ArtefactType getArtefactType();

	/**
	 * @return the type of delivery item
	 */
	DeliveryType getDeliveryType();

	/**
	 * Defines the type of delivery for this item
	 * 
	 * @param type the {@link DeliveryType} of this item
	 */
	void setDeliveryType(DeliveryType type);

	long getId();

	void setId(long id);

	/**
	 * @return the {@link IDeliveryModule} containing this {@link IDeliveryItem}
	 */
	IDeliveryModule getParentModule();

	/**
	 * Defines the {@link IDeliveryModule} which contains this {@link IDeliveryItem}
	 * 
	 * @param module parent {@link DeliveryModule}
	 */
	void setParentModule(IDeliveryModule module);

}
