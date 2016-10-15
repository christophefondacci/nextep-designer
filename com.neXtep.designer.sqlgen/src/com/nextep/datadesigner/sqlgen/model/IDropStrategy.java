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
package com.nextep.datadesigner.sqlgen.model;

import com.nextep.datadesigner.model.INamedObject;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * A drop strategy provides delegates method to handle the removal of objects during database
 * generation or synchronization.<br>
 * Strategies are registered through dedicated extension points and can be defined by the user
 * through properties. For example, a user might want to keep database tables which have been
 * removed from the repository or he might want to drop them. A user might want to nullify a removed
 * column OR might want to drop it.<br>
 * The drop strategies can handle those different behaviours.
 * 
 * @author Christophe Fondacci
 */
public interface IDropStrategy extends INamedObject {

	/**
	 * The drop delegate method. This method is a delegate from the method
	 * {@link ISQLGenerator#generateDrop(Object)}.
	 * 
	 * @param generator initial generator for this model
	 * @param modelToDrop the model for which we want to generate the SQL drop statement.
	 * @param vendor the {@link DBVendor} for which we generate the SQL drop statement.
	 * @return the {@link ISQLScript} containing the SQL drop statements
	 */
	IGenerationResult generateDrop(ISQLGenerator generator, Object modelToDrop, DBVendor vendor);

	/**
	 * @return the ID of this strategy. It must be unique in the scope of the whole application and
	 *         extensions. The ID is used to define appropriate properties of the chosen user drop
	 *         strategies.
	 */
	String getId();

	/**
	 * Defines if this strategy should be the default
	 * 
	 * @param isDefault
	 */
	void setDefault(boolean isDefault);

	/**
	 * @return if this strategy should be the default
	 */
	boolean isDefault();

	/**
	 * Indicates if this strategy performs some kind of drop or not.
	 * 
	 * @return whether the object is dropped or not
	 */
	boolean isDropping();

	/**
	 * Retrieves the vendor for which this strategy is defined.
	 * 
	 * @return the {@link DBVendor}
	 */
	DBVendor getVendor();

	/**
	 * Defines the vendor for which this strategy is defined
	 * 
	 * @param vendor the {@link DBVendor}
	 */
	void setVendor(DBVendor vendor);

}
