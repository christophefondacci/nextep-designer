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
import com.nextep.designer.dbgm.mergers.SynonymMerger;
import com.nextep.designer.dbgm.oracle.impl.OracleSynonym;
import com.nextep.designer.dbgm.oracle.model.IOracleSynonym;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * A merger to compare two versions of a {@link OracleSynonym} and generate a
 * merged version of those synonyms.
 * 
 * @author Bruno Gautier
 */
public class OracleSynonymMerger extends SynonymMerger<IOracleSynonym> {

	private static final String ATTR_PUBLIC = "Public";
	private static final String ATTR_REF_OBJ_DBLINK = "Referred object DBLink name";

	@Override
	protected void fillSpecificComparison(IComparisonItem result, IOracleSynonym srcSynonym,
			IOracleSynonym tgtSynonym) {
		result.addSubItem(new ComparisonAttribute(ATTR_PUBLIC, null == srcSynonym ? null
				: strVal(srcSynonym.isPublic()), null == tgtSynonym ? null : strVal(tgtSynonym
				.isPublic())));
		result.addSubItem(new ComparisonAttribute(ATTR_REF_OBJ_DBLINK, null == srcSynonym ? null
				: srcSynonym.getRefDbObjDbLinkName(), null == tgtSynonym ? null : tgtSynonym
				.getRefDbObjDbLinkName()));
	}

	@Override
	protected Class<? extends IReferenceable> getSpecificClass() {
		return IOracleSynonym.class;
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		// Filling synonym common attributes.
		IOracleSynonym synonym = (IOracleSynonym) super.fillObject(target, result, activity);

		// Handling Oracle synonym specific attributes.
		if (synonym != null) {
			String accessible = getStringProposal(ATTR_PUBLIC, result);
			String refDbObjDbLinkName = getStringProposal(ATTR_REF_OBJ_DBLINK, result);

			if (accessible != null)
				synonym.setPublic(Boolean.valueOf(accessible));
			if (refDbObjDbLinkName != null && !refDbObjDbLinkName.equals(""))
				synonym.setRefDbObjDbLinkName(refDbObjDbLinkName);
		}

		return synonym;
	}

}
