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
package com.nextep.designer.dbgm.oracle.model;

import com.nextep.datadesigner.dbgm.model.ISynonym;

/**
 * This interface represents an Oracle database synonym. In Oracle database, a synonym can be an
 * alias for any table, view, materialized view, sequence, procedure, function, package, type, Java
 * class schema object, user-defined object type, or another synonym.
 * 
 * @see <a
 *      href="http://download.oracle.com/docs/cd/B19306_01/server.102/b14220/schema.htm#i5669">Oracle
 *      10gR2 Database Concepts: Overview of synonyms</a>
 * @author Bruno Gautier
 */
public interface IOracleSynonym extends ISynonym {

    /**
     * Defines the accessibility of this synonym.
     * 
     * @param accessible <code>true</code> if this synonym is public, <code>false</code> otherwise.
     */
    public void setPublic(boolean accessible);

    /**
     * Is this synonym public?
     * 
     * @return <code>true</code> if this synonym is public, <code>false</code> otherwise.
     */
    public boolean isPublic();

    /**
     * Defines the name of the database link if this synonym refers to a database object located on
     * a remote database. If the database link is specified but not the schema of the referenced
     * object, then this synonyms refers to an object in the schema specified by the database link.
     * 
     * @param name a <code>String</code> representing the database link name.
     */
    public void setRefDbObjDbLinkName(String name);

    /**
     * Returns the name of the database link if this synonym refers to a database object located on
     * a remote database.
     * 
     * @return a <code>String</code> representing the database link name.
     */
    public String getRefDbObjDbLinkName();

}
