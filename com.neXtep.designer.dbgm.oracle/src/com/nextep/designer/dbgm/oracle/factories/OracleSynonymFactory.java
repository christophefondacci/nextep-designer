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
package com.nextep.designer.dbgm.oracle.factories;

import com.nextep.designer.dbgm.factories.SynonymFactory;
import com.nextep.designer.dbgm.oracle.impl.OracleSynonym;
import com.nextep.designer.dbgm.oracle.model.IOracleSynonym;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * A factory to create new instances of a {@link OracleSynonym} and copy properties from a
 * <code>OracleSynonym</code> to another.
 * 
 * @author Bruno Gautier
 */
public class OracleSynonymFactory extends SynonymFactory {

    @Override
    public IVersionable<?> createVersionable() {
        return new OracleSynonym();
    }

    @Override
    public void rawCopy(IVersionable<?> source, IVersionable<?> destination) {
        // Copying common content
        super.rawCopy(source, destination);

        // Handling Oracle specific attributes
        IOracleSynonym srcSynonym = (IOracleSynonym)source.getVersionnedObject().getModel();
        IOracleSynonym dstSynonym = (IOracleSynonym)destination.getVersionnedObject().getModel();

        dstSynonym.setPublic(srcSynonym.isPublic());
        dstSynonym.setRefDbObjDbLinkName(srcSynonym.getRefDbObjDbLinkName());
    }

}
