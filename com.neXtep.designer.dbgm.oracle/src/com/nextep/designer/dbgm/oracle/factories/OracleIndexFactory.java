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

import com.nextep.designer.dbgm.factories.IndexFactory;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.OracleIndex;
import com.nextep.designer.dbgm.oracle.impl.external.OracleIndexPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleIndex;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class OracleIndexFactory extends IndexFactory {

	/**
	 * @see com.nextep.designer.dbgm.factories.IndexFactory#createVersionable()
	 */
	@Override
	public IVersionable<?> createVersionable() {
		return new OracleIndex();
	}

	/**
	 * @see com.nextep.designer.dbgm.factories.TableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		// Copying common content
		super.rawCopy(source, destination);
		// Handling Oracle specific content
		IOracleIndex src = (IOracleIndex) source.getVersionnedObject().getModel();
		IOracleIndex tgt = (IOracleIndex) destination.getVersionnedObject().getModel();
		if (src.getPhysicalProperties() != null) {
			// Setting copied properties
			IIndexPhysicalProperties tgtProps = new OracleIndexPhysicalProperties();
			(tgt).setPhysicalProperties(tgtProps);
			OracleFactoryHelper.copyPhysicalProperties(src, tgt);
			tgtProps.setParent(tgt);
		}
	}
}
