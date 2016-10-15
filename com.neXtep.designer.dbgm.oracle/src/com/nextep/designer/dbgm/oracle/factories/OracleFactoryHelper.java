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

import com.nextep.designer.dbgm.helpers.FactoryHelper;
import com.nextep.designer.dbgm.model.IIndexPartition;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.oracle.impl.IndexPartition;
import com.nextep.designer.dbgm.oracle.impl.TablePartition;
import com.nextep.designer.dbgm.oracle.impl.external.PartitionPhysicalProperties;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleFactoryHelper {

	public static void copyPhysicalProperties(IPhysicalObject src, IPhysicalObject tgt) {
		FactoryHelper.copyPhysicalProperties(src, tgt);
		// Copying partitioning attributes if physical properties are partitionable
		if (src.getPhysicalProperties() instanceof IPartitionable
				&& tgt.getPhysicalProperties() instanceof IPartitionable) {
			IPartitionable srcPartProps = (IPartitionable) src.getPhysicalProperties();
			IPartitionable tgtPartProps = (IPartitionable) tgt.getPhysicalProperties();

			tgtPartProps.setPartitioningMethod(srcPartProps.getPartitioningMethod());
			for (IPartition srcPart : srcPartProps.getPartitions()) {
				IPartition tgtPart = copyPartition(srcPart);
				tgtPart.setParent(tgtPartProps);
				tgtPartProps.addPartition(tgtPart);
			}
		}
	}

	public static IPartition copyPartition(IPartition src) {
		IPartition tgt = null;
		if (src instanceof ITablePartition) {
			tgt = new TablePartition();
			((TablePartition) tgt).setHighValue(((TablePartition) src).getHighValue());
			tgt.setPhysicalProperties(new PartitionPhysicalProperties());
		} else if (src instanceof IIndexPartition) {
			tgt = new IndexPartition();
			tgt.setPhysicalProperties(new PartitionPhysicalProperties());
		}
		tgt.setName(src.getName());
		tgt.setPosition(src.getPosition());
		tgt.setReference(src.getReference());
		FactoryHelper.copyPhysicalProperties(src, tgt);
		return tgt;
	}

}
