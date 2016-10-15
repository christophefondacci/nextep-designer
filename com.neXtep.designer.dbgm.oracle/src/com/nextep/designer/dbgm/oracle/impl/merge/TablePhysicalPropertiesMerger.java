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
package com.nextep.designer.dbgm.oracle.impl.merge;

import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.dbgm.oracle.impl.external.OracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.PhysicalOrganisation;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class TablePhysicalPropertiesMerger extends OraclePhysicalPropertiesMerger {

	public static final String ATTR_ORGANIZATION = "Organization"; //$NON-NLS-1$
	public static final String CATEGORY_PART_COLS = "Partitioning columns"; //$NON-NLS-1$

	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IOracleTablePhysicalProperties src = (IOracleTablePhysicalProperties) source;
		IOracleTablePhysicalProperties tgt = (IOracleTablePhysicalProperties) target;

		IComparisonItem result = super.doCompare(source, target);
		if (result == null)
			return null;

		// Comparing organization
		result.addSubItem(new ComparisonAttribute(ATTR_ORGANIZATION, src == null ? null : src
				.getPhysicalOrganisation().name(), tgt == null ? null : tgt
				.getPhysicalOrganisation().name()));

		// Comparing partitioned columns
		listCompare(
				CATEGORY_PART_COLS,
				result,
				src == null ? (List<IReference>) Collections.EMPTY_LIST : src
						.getPartitionedColumnsRef(),
				tgt == null ? (List<IReference>) Collections.EMPTY_LIST : tgt
						.getPartitionedColumnsRef(), true);

		return result;
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IOracleTablePhysicalProperties props = (IOracleTablePhysicalProperties) super.fillObject(target,
				result, activity);
		if (props == null)
			return null;

		// Filling organization
		props.setPhysicalOrganisation(PhysicalOrganisation.valueOf(getStringProposal(
				ATTR_ORGANIZATION, result)));

		// Filling partitioned columns
		List<?> partCols = getMergedList(CATEGORY_PART_COLS, result, activity);
		for (Object o : partCols) {
			IReference r = (IReference) o;
			props.addPartitionedColumnRef(r);
		}
		save(props);

		return props;
	}

	@Override
	protected Object createTargetObject(IComparisonItem result, IActivity mergeActivity) {
		return new OracleTablePhysicalProperties();
	}

}
