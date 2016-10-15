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

import com.nextep.datadesigner.dbgm.impl.Synonym;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * A merger to compare two versions of a {@link Synonym} and generate a merged
 * version of those synonyms.
 * 
 * @author Bruno Gautier
 */
public class SynonymMerger<T> extends Merger<T> {

	private static final String ATTR_REF_OBJ_NAME = "Referred object name";
	private static final String ATTR_REF_OBJ_SCHEMA = "Referred object schema name";

	@Override
	protected IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		ISynonym srcSynonym = (ISynonym) source;
		ISynonym tgtSynonym = (ISynonym) target;

		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());

		// Comparing name and description attributes.
		compareName(result, srcSynonym, tgtSynonym);

		// Comparing synonym specific attributes.
		result.addSubItem(new ComparisonAttribute(ATTR_REF_OBJ_NAME, null == srcSynonym ? null
				: srcSynonym.getRefDbObjName(), null == tgtSynonym ? null : tgtSynonym
				.getRefDbObjName()));
		result.addSubItem(new ComparisonAttribute(ATTR_REF_OBJ_SCHEMA, null == srcSynonym ? null
				: srcSynonym.getRefDbObjSchemaName(), null == tgtSynonym ? null : tgtSynonym
				.getRefDbObjSchemaName()));

		return result;
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		ISynonym synonym = (ISynonym) target;

		// Filling name and description attributes.
		fillName(result, synonym);
		if (null == synonym.getName())
			return null;

		// Filling synonym specific attributes.
		String refDbObjName = getStringProposal(ATTR_REF_OBJ_NAME, result);
		String refDbObjSchemaName = getStringProposal(ATTR_REF_OBJ_SCHEMA, result);

		if (refDbObjName != null && !refDbObjName.equals(""))
			synonym.setRefDbObjName(refDbObjName);
		if (refDbObjSchemaName != null && !refDbObjSchemaName.equals(""))
			synonym.setRefDbObjSchemaName(refDbObjSchemaName);

		return synonym;
	}

}
