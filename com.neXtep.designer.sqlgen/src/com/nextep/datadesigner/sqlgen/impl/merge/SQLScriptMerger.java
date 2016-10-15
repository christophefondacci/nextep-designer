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
package com.nextep.datadesigner.sqlgen.impl.merge;

import java.io.IOException;
import java.util.List;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerWithMultilineAttributes;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class SQLScriptMerger extends MergerWithMultilineAttributes {

	public static final String ATTR_SQL = "SQL"; //$NON-NLS-1$

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		ISQLScript script = (ISQLScript) target;

		// Filling name properties
		fillName(result, script);
		if (script.getName() == null) {
			return null;
		}
		// Filling source code
		if (result.getSource() != null && result.getTarget() != null
				&& result.getDifferenceType() != DifferenceType.EQUALS) {
			script.setSql(mergeAttribute(result, ATTR_SQL)); // , ancestor==null ? "" :
																// ancestor.getSpecSourceCode(),pkg.getName()));
		} else {
			script.setSql(getStringProposal(ATTR_SQL, result));
		}

		// Returning filled package
		return script;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		ISQLScript src = (ISQLScript) source;
		ISQLScript tgt = (ISQLScript) target;

		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		compareName(result, src, tgt);

		// TODO Comparing on a line to line basis
		// Comparing SQL
		result.addSubItem(new ComparisonAttribute(ATTR_SQL, src != null ? src.getSql() : null,
				tgt != null ? tgt.getSql() : null));
		if (src != null && target != null) {
			try {
				addLineByLineSubItems(result, null, source, target);
			} catch (IOException e) {
				throw new ErrorException(e);
			}
		}
		// Returning
		return result;
	}

	@Override
	public void addLineByLineSubItems(IComparisonItem result, IReferenceable ancestor,
			IReferenceable source, IReferenceable target) throws IOException {
		final ISQLScript src = (ISQLScript) source;
		final ISQLScript tgt = (ISQLScript) target;
		final ISQLScript anc = (ISQLScript) ancestor;
		List<IComparisonItem> srcItems = MergeUtils.mergeCompare(src == null ? "" : src.getSql(), //$NON-NLS-1$
				tgt == null ? "" : tgt.getSql(), anc == null ? null : anc.getSql()); //$NON-NLS-1$
		addMergedSubItems(result, ATTR_SQL, srcItems);
	}

	@Override
	protected void cleanLineByLineSubItems(IComparisonItem result) {
		removeMergedSubItems(result, ATTR_SQL);
	}

}
