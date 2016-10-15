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

import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * SQL generator for database generic model. Each element of the model should provide its
 * corresponding SQL generator which should be declined for all supported database vendors.
 * 
 * @author Christophe Fondacci
 */
public interface ISQLGenerator {

	/**
	 * Generates the SQL for the given object. The generated SQL script must contain all SQL
	 * instructions needed to create the database object corresponding to the Java representation of
	 * this object given in parameter.
	 * 
	 * @param model the database object for which the SQL will be generated
	 */
	IGenerationResult generateFullSQL(Object model);

	/**
	 * Generates the SQL from the given comparison information. The comparison information should
	 * contain valid and resolved merge information. This result can come from the comparison of the
	 * repository with a target or of 2 different versions of the repository. The returned script
	 * will contain all SQL instructions needed to upgrade the target object to the specifications
	 * of the source object.
	 * 
	 * @param result the comparison result
	 * @return the upgrade script (only source selection will be generated)
	 */
	IGenerationResult generateIncrementalSQL(IComparisonItem result);

	/**
	 * Generates the drop script of the given object. This method should check configured drop
	 * policies to determine what to do.
	 * 
	 * @param model model for which the SQL DROP statement should be generated
	 * @return the drop script
	 */
	IGenerationResult generateDrop(Object model);

	/**
	 * This method generates the drop script of the given object. The implementation should not
	 * depend on any drop strategy and should always DROP the specified object. For example if a
	 * column generator has a drop policy set to "Keep nullify" the
	 * {@link ISQLGenerator#generateDrop(Object)} will generate the following SQL:<br>
	 * ALTER TABLE parent_table MODIFY ( col {column_type} NULL );<br>
	 * while a call to {@link ISQLGenerator#doDrop(Object)} will generate:<br>
	 * ALTER TABLE parent_table DROP COLUMN col;<br>
	 * 
	 * @param model model to drop
	 * @return the DROP SQL script
	 */
	IGenerationResult doDrop(Object model);

	/**
	 * @return the {@link DBVendor} for which this generator generates SQL code.
	 */
	DBVendor getVendor();

	/**
	 * Defines the vendor for which the SQL should be generated
	 * 
	 * @param vendor the {@link DBVendor} of the target database
	 */
	void setVendor(DBVendor vendor);
}
