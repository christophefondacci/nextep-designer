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
package com.nextep.designer.dbgm.mergers.base;

import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.dbgm.mergers.PhysicalPropertiesMerger;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * Merger for partitionable elements.
 * 
 * @author Bruno Gautier
 * @author refactored by Christophe Fondacci There was a mix of partitioned and partitionable
 *         notions in first implementation, along with a mix of the corresponding physical
 *         properties, leading to a nightmare.
 */
public class AbstractPartitionableMerger extends PhysicalPropertiesMerger {

	public static final String ATTR_PARTITIONING = "Partitioning"; //$NON-NLS-1$
	public static final String CATEGORY_PARTITIONS = "Partitions"; //$NON-NLS-1$

	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		final IComparisonItem result = super.doCompare(source, target);
		if (result == null) {
			return null;
		}
		final IPartitionable src = (IPartitionable) source;
		final IPartitionable tgt = (IPartitionable) target;

		// Comparing partitioning method
		result.addSubItem(new ComparisonAttribute(ATTR_PARTITIONING,
				getPartitioningMethodAttribute(src), getPartitioningMethodAttribute(tgt)));

		// Comparing partitions
		listCompare(CATEGORY_PARTITIONS, result,
				src == null ? (List<IPartition>) Collections.EMPTY_LIST : src.getPartitions(),
				tgt == null ? (List<IPartition>) Collections.EMPTY_LIST : tgt.getPartitions());

		return result;
	}

	/**
	 * A null safe getter for the {@link PartitioningMethod} attribute string
	 * 
	 * @param partitionable the {@link IPartitionable} to extract attribute from
	 * @return the partitioning method attribute
	 */
	private String getPartitioningMethodAttribute(IPartitionable partitionable) {
		if (partitionable == null) {
			return null;
		} else {
			final PartitioningMethod method = partitionable.getPartitioningMethod();
			return method == null ? null : method.name();
		}
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IPartitionable props = (IPartitionable) super.fillObject(target, result, activity);

		// Filling partitioning method
		String partMethod = getStringProposal(ATTR_PARTITIONING, result);
		if (null == partMethod) {
			return null;
		}

		props.setPartitioningMethod(PartitioningMethod.valueOf(partMethod));

		// Filling merged partitions
		List<?> partitions = getMergedList(CATEGORY_PARTITIONS, result, activity);
		for (Object r : partitions) {
			IPartition p = (IPartition) r;
			p.setParent(props);
			save(p);
			props.addPartition(p);
		}

		return props;
	}

}
