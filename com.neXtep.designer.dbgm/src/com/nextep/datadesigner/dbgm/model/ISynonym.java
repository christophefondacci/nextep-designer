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
package com.nextep.datadesigner.dbgm.model;

/**
 * This interface represents a database synonym. A synonym is an alias for another database object.
 * The types of objects that can be used with a synonym depend on database vendor.
 * 
 * @author Bruno Gautier
 */
public interface ISynonym extends IDatabaseObject<ISynonym> {

    public static final String TYPE_ID = "SYNONYM";

    /**
     * Defines the name of the database object referred by this synonym.
     * 
     * @param name a <code>String</code> representing the name of the database object referred by
     *        this synonym.
     */
    public void setRefDbObjName(String name);

    /**
     * Returns the name of the database object referred by this synonym.
     * 
     * @return a <code>String</code> representing the name of the database object referred by this
     *         synonym.
     */
    public String getRefDbObjName();

    /**
     * Defines the name of the schema in which resides the database object referred by this synonym.
     * 
     * @param name a <code>String</code> representing the name of the schema in which resides the
     *        database object referred by this synonym.
     */
    public void setRefDbObjSchemaName(String name);

    /**
     * Returns the name of the schema in which resides the database object referred by this synonym.
     * 
     * @return a <code>String</code> representing the name of the schema in which resides the
     *         database object referred by this synonym.
     */
    public String getRefDbObjSchemaName();

}
