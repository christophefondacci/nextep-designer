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
package com.nextep.designer.dbgm.model;

import java.util.Map;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.model.IParentable;

/**
 * This interface describes the basic information which constitutes the physical properties of an
 * element.<br>
 * Physical properties can belong to tables, indexes, materialized views, or partitions.
 * Specificities of these elements are defined by extending this interface.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface IPhysicalProperties extends IObservable, IdentifiedObject, ITypedObject,
		IReferenceable, IReferencer, IParentable<IPhysicalObject> {

	/**
	 * @return the name of the tablespace on which the database element should be generated. This is
	 *         not a ITablespace object because the tablespace definition might not be present in
	 *         the current container / view since they could be defined at the application level
	 */
	String getTablespaceName();

	/**
	 * Defines the name of the tablespace which should own the database element.
	 * 
	 * @param tablespaceName name of the tablespace which will own the database element
	 */
	void setTablespaceName(String tablespaceName);

	/**
	 * Defines an attribute value for these physical properties.
	 * 
	 * @param attr physical attribute to define
	 * @param value value of the attribute
	 */
	void setAttribute(PhysicalAttribute attr, Object value);

	/**
	 * A helper method to allow full replacement of physical attributes in 1 call.
	 * 
	 * @param attrs attributes map
	 */
	void setAttributes(Map<PhysicalAttribute, Object> attrs);

	/**
	 * Retrieves the value of a physical attribute, if defined.
	 * 
	 * @param attr attribute to retrieve
	 * @return the value of the specified physical attribute or <code>null</code> if this attribute
	 *         has not been defined for these physical properties
	 */
	Object getAttribute(PhysicalAttribute attr);

	/**
	 * @return a map of all defined attributes value, hashed by their corresponding physical
	 *         attribute
	 */
	Map<PhysicalAttribute, Object> getAttributes();

	/**
	 * @return whether or not the segments of the database element should be compressed
	 */
	boolean isCompressed();

	/**
	 * Defines the compression of the underlying database element's segments.
	 * 
	 * @param compressed <code>true</code> to compress, else <code>false</code>
	 */
	void setCompressed(boolean compressed);

	long getId();

	void setId(long id);

	/**
	 * @return <code>true</code> if the database element is in logging mode, <code>false</code>
	 *         otherwise
	 */
	boolean isLogging();

	/**
	 * Defines the logging mode.
	 * 
	 * @param logging <code>true</code> to activate logging, <code>false</code> otherwise
	 */
	void setLogging(boolean logging);

}
