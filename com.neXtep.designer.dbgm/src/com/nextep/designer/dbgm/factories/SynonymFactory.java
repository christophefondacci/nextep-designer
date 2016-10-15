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
package com.nextep.designer.dbgm.factories;

import com.nextep.datadesigner.dbgm.impl.Synonym;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * A factory to create new instances of a {@link Synonym} and copy properties from a
 * <code>Synonym</code> to another.
 * 
 * @author Bruno Gautier
 */
public class SynonymFactory extends VersionableFactory {

    @Override
    public IVersionable<?> createVersionable() {
        return new Synonym();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void rawCopy(IVersionable<?> source, IVersionable<?> destination) {
        ISynonym srcSynonym = (ISynonym)source.getVersionnedObject().getModel();
        ISynonym dstSynonym = (ISynonym)destination.getVersionnedObject().getModel();

        // Copy the name, description and version attributes.
        versionCopy((IVersionable<ISynonym>)source, (IVersionable<ISynonym>)destination);

        // Copy synonym specific attributes.
        dstSynonym.setRefDbObjName(srcSynonym.getRefDbObjName());
        dstSynonym.setRefDbObjSchemaName(srcSynonym.getRefDbObjSchemaName());
    }

}
