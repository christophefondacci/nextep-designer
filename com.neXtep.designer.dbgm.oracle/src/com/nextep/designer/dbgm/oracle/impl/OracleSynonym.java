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
package com.nextep.designer.dbgm.oracle.impl;

import com.nextep.datadesigner.dbgm.impl.Synonym;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.dbgm.oracle.model.IOracleSynonym;

/**
 * This object represents an Oracle database synonym.
 * 
 * @author Bruno Gautier
 */
public class OracleSynonym extends Synonym implements IOracleSynonym {

    private boolean accessible = false;
    private String refDbObjDbLinkName;

    @Override
    public boolean isPublic() {
        return accessible;
    }

    @Override
    public void setPublic(boolean accessible) {
        final boolean oldAccessible = this.accessible;
        this.accessible = accessible;
        notifyIfChanged(oldAccessible, accessible, ChangeEvent.MODEL_CHANGED);
    }

    @Override
    public String getRefDbObjDbLinkName() {
        return refDbObjDbLinkName;
    }

    @Override
    public void setRefDbObjDbLinkName(String name) {
        final String oldName = this.refDbObjDbLinkName;

        // Since the referred object dblink name is a database name, we use the current database
        // vendor formatter to format the specified name.
        this.refDbObjDbLinkName = DBGMHelper.getCurrentVendor().getNameFormatter().format(name);

        notifyIfChanged(oldName, refDbObjDbLinkName, ChangeEvent.MODEL_CHANGED);
    }

}
