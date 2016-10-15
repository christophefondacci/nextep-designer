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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.helpers;

import java.util.HashMap;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PhysicalAttribute;

/**
 * @author Christophe Fondacci
 */
public final class FactoryHelper {

	private FactoryHelper() {
	}

	/**
	 * This method copies the physical properties of the source physical object to the target. The
	 * target <b>MUST</b> have been filled with an empty {@link IPhysicalProperties} implementation
	 * before calling this method.
	 * 
	 * @param src source physical object
	 * @param tgt target physical object
	 */
	public static void copyPhysicalProperties(IPhysicalObject src, IPhysicalObject tgt) {
		IPhysicalProperties srcProps = (IPhysicalProperties) src.getPhysicalProperties();
		if (srcProps != null) {
			IPhysicalProperties tgtProps = (IPhysicalProperties) tgt.getPhysicalProperties();

			// Copying base attributes
			tgtProps.setAttributes(new HashMap<PhysicalAttribute, Object>(srcProps.getAttributes()));
			tgtProps.setTablespaceName(srcProps.getTablespaceName());
			tgtProps.setCompressed(srcProps.isCompressed());
			tgtProps.setReference(srcProps.getReference());

			// Copying Oracle specific attributes
			tgtProps.setLogging(srcProps.isLogging());

		}
	}

}
