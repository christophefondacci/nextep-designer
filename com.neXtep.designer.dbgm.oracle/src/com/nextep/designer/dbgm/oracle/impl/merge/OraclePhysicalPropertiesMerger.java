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

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonAttributeWithDefault;
import com.nextep.designer.dbgm.mergers.base.AbstractPartitionableMerger;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Bruno Gautier
 */
public class OraclePhysicalPropertiesMerger extends AbstractPartitionableMerger {

	public static final String ATTR_LOGGING = "Logging"; //$NON-NLS-1$

	public static final String ATTR_PCTFREE = "PCT Free"; //$NON-NLS-1$
	public static final String ATTR_PCTUSED = "PCT Used"; //$NON-NLS-1$
	public static final String ATTR_INITTRANS = "Init trans"; //$NON-NLS-1$
	public static final String ATTR_MAXTRANS = "Max trans"; //$NON-NLS-1$

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IPhysicalProperties src = (IPhysicalProperties) source;
		IPhysicalProperties tgt = (IPhysicalProperties) target;

		IComparisonItem result = super.doCompare(source, target);
		if (result == null)
			return null;

		// Comparing logging property
		result.addSubItem(new ComparisonAttribute(ATTR_LOGGING, src == null ? null : strVal(src
				.isLogging()), tgt == null ? null : strVal(tgt.isLogging())));

		// Comparing attributes
		result.addSubItem(new ComparisonAttributeWithDefault(ATTR_PCTFREE, src == null ? null
				: strVal(src.getAttribute(PhysicalAttribute.PCT_FREE)), tgt == null ? null
				: strVal(tgt.getAttribute(PhysicalAttribute.PCT_FREE)), "10", true)); //$NON-NLS-1$
		result.addSubItem(new ComparisonAttributeWithDefault(ATTR_PCTUSED, src == null ? null
				: strVal(src.getAttribute(PhysicalAttribute.PCT_USED)), tgt == null ? null
				: strVal(tgt.getAttribute(PhysicalAttribute.PCT_USED)), "40", true)); //$NON-NLS-1$
		result.addSubItem(new ComparisonAttribute(ATTR_INITTRANS, src == null ? null : strVal(src
				.getAttribute(PhysicalAttribute.INIT_TRANS)), tgt == null ? null : strVal(tgt
				.getAttribute(PhysicalAttribute.INIT_TRANS)), true));
		result.addSubItem(new ComparisonAttribute(ATTR_MAXTRANS, src == null ? null : strVal(src
				.getAttribute(PhysicalAttribute.MAX_TRANS)), tgt == null ? null : strVal(tgt
				.getAttribute(PhysicalAttribute.MAX_TRANS)), true));

		return result;
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IPhysicalProperties props = (IPhysicalProperties) super
				.fillObject(target, result, activity);
		if (props == null)
			return null;

		String pctFree = getStringProposal(ATTR_PCTFREE, result);
		String pctUsed = getStringProposal(ATTR_PCTUSED, result);
		String initTrans = getStringProposal(ATTR_INITTRANS, result);
		String maxTrans = getStringProposal(ATTR_MAXTRANS, result);

		if (pctFree == null && pctUsed == null && initTrans == null && maxTrans == null) {
			return null;
		}

		// Filling logging property
		props.setLogging(Boolean.valueOf(getStringProposal(ATTR_LOGGING, result)));

		// Filling attributes
		props.setAttribute(PhysicalAttribute.PCT_FREE, pctFree == null || "".equals(pctFree) ? null //$NON-NLS-1$
				: Integer.valueOf(pctFree));
		props.setAttribute(PhysicalAttribute.PCT_USED, pctUsed == null || "".equals(pctUsed) ? null //$NON-NLS-1$
				: Integer.valueOf(pctUsed));
		props.setAttribute(PhysicalAttribute.INIT_TRANS,
				initTrans == null || "".equals(initTrans) ? null : Integer.valueOf(initTrans)); //$NON-NLS-1$
		props.setAttribute(PhysicalAttribute.MAX_TRANS,
				maxTrans == null || "".equals(maxTrans) ? null : Integer.valueOf(maxTrans)); //$NON-NLS-1$

		return props;
	}

}
