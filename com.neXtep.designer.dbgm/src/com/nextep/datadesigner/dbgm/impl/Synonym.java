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
package com.nextep.datadesigner.dbgm.impl;

import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;

/**
 * This object represents a database synonym.
 * 
 * @author Bruno Gautier
 */
public class Synonym extends SynchedVersionable<ISynonym> implements ISynonym {

    private String refDbObjName;
    private String refDbObjSchemaName;

    /**
     * Hibernate / Versionable constructor
     */
    public Synonym() {
        nameHelper.setFormatter(DBGMHelper.getCurrentVendor().getNameFormatter());
    }

    @Override
    public IElementType getType() {
        return IElementType.getInstance(ISynonym.TYPE_ID);
    }

    @Override
    public String getRefDbObjName() {
        return refDbObjName;
    }

    @Override
    public void setRefDbObjName(String name) {
        final String oldName = this.refDbObjName;

        // Since the referred object name is a database name, we use the current database vendor
        // formatter to format the specified name.
        this.refDbObjName = DBGMHelper.getCurrentVendor().getNameFormatter().format(name);

        notifyIfChanged(oldName, refDbObjName, ChangeEvent.MODEL_CHANGED);
    }

    @Override
    public String getRefDbObjSchemaName() {
        return refDbObjSchemaName;
    }

    @Override
    public void setRefDbObjSchemaName(String name) {
        final String oldName = this.refDbObjSchemaName;

        // Since the referred object schema name is a database name, we use the current database
        // vendor formatter to format the specified name.
        this.refDbObjSchemaName = DBGMHelper.getCurrentVendor().getNameFormatter().format(name);

        notifyIfChanged(oldName, refDbObjSchemaName, ChangeEvent.MODEL_CHANGED);
    }

    @Override
    public void checkConsistency() throws InconsistentObjectException {
        // We first check name consistency before checking for more specific characteristics.
        super.checkConsistency();

        if (null == getRefDbObjName() || getRefDbObjName().trim().equals("")) {
            throw new InconsistentObjectException(
                    DBGMMessages.getString("synonym.consistency.refDbObjNameMustExist"));
        }
    }

}
