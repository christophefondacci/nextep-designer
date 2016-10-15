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
package com.nextep.designer.dbgm.mergers;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerWithChildCollections;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.preferences.PreferenceConstants;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * An abstract merger for physical properties which merges all the base attributes of the
 * IPhysicalProperties interface. Mergers of IOracleTablePhysicalProperties or
 * IOracleIndexPhysicalProperties should extend this merger and implement only the merge of specific
 * features.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PhysicalPropertiesMerger extends MergerWithChildCollections {

	public static final String ATTR_TABLESPACE = "Tablespace"; //$NON-NLS-1$
	public static final String ATTR_COMPRESSED = "Compressed"; //$NON-NLS-1$

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IPhysicalProperties props = (IPhysicalProperties) target;

		String tsName = getStringProposal(ATTR_TABLESPACE, result);

		if (tsName == null) {
			return null;
		}

		props.setTablespaceName(tsName);
		props.setCompressed(Boolean.valueOf(getStringProposal(ATTR_COMPRESSED, result)));
		save(props);

		return props;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		// Retrieving our casted model
		IPhysicalProperties src = (IPhysicalProperties) source;
		IPhysicalProperties tgt = (IPhysicalProperties) target;

		// Handling specific database scope behaviour
		// We consider EQUALITY if we have the following conditions:
		// 1- Database scope
		// 2- Unexisting (null) repository physical properties definition
		// 3- Existing database physical properties (always true)
		if (src == null && getMergeStrategy().getComparisonScope() == ComparisonScope.DATABASE) {
			// result.setDifferenceType(DifferenceType.EQUALS);
			return null;
		}

		// Checking whether physical comparison should be performed
		final IEclipsePreferences prefs = new InstanceScope().getNode(DbgmPlugin.PLUGIN_ID);
		final boolean comparePhysicals = prefs.getBoolean(PreferenceConstants.COMPARE_PHYSICALS,
				true);
		ComparisonResult result = null;
		if (comparePhysicals) {
			// Building comparison result
			result = new ComparisonResult(source, target, getMergeStrategy().getComparisonScope());
			result.addSubItem(new ComparisonAttribute(ATTR_TABLESPACE, src == null ? null
					: strVal(src.getTablespaceName()), tgt == null ? null : strVal(tgt
					.getTablespaceName())));
			result.addSubItem(new ComparisonAttribute(ATTR_COMPRESSED, src == null ? null
					: strVal(src.isCompressed()), tgt == null ? null : strVal(tgt.isCompressed())));
		}
		return result;
	}

	@Override
	public boolean isVersionable() {
		return false;
	}

	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}

}
