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
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttributeIgnoreFirstLine;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerWithMultilineAttributes;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class ProcedureMerger extends MergerWithMultilineAttributes {

	public static final String ATTR_SOURCE = "Source";
	private static final String ATTR_LANG = "Language";

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IProcedure proc = (IProcedure) target;

		// Filling name properties
		fillName(result, proc);
		// Filling source code
		if (result.getSource() != null && result.getTarget() != null
				&& result.getDifferenceType() != DifferenceType.EQUALS) {
			proc.setSQLSource(mergeAttribute(result, ATTR_SOURCE)); // , ancestor==null ? "" :
			// ancestor.getSpecSourceCode(),pkg.getName()));
		} else {
			proc.setSQLSource(getStringProposal(ATTR_SOURCE, result));
		}
		// Filling language
		LanguageType langType = null;
		try {
			LanguageType.valueOf(getStringProposal(ATTR_LANG, result));
		} catch (RuntimeException e) {
			langType = LanguageType.STANDARD;
		}
		proc.setLanguageType(langType);
		// Returning filled package
		return proc;
	}

	/**
	 * Cleans the source code from characters that would break the comparison.<br>
	 * Be careful when extending, you should always call this super method or you may corrupt
	 * comparison.
	 * 
	 * @param source initial string
	 * @return the cleaned string
	 */
	protected String cleanSource(String source) {
		return source.replace("\r", "").replaceAll("\n( )+\n", "\n\n").trim();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IProcedure src = (IProcedure) source;
		IProcedure tgt = (IProcedure) target;

		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		compareName(result, src, tgt);

		// TODO Comparing on a line to line basis
		// Triming each line
		String sqlSource = null;
		String sqlTarget = null;
		if (getMergeStrategy().getComparisonScope() == ComparisonScope.REPOSITORY) {
			sqlSource = (src != null ? src.getSQLSource() : null);
			sqlTarget = (tgt != null ? tgt.getSQLSource() : null);
		} else {
			sqlSource = (src != null && src.getSQLSource() != null ? cleanSource(src.getSQLSource())
					: null);
			sqlTarget = (tgt != null && tgt.getSQLSource() != null ? cleanSource(tgt.getSQLSource())
					: null);
		}
		// Comparing body and specs
		result.addSubItem(new ComparisonAttributeIgnoreFirstLine(ATTR_SOURCE,
				src != null ? sqlSource : null, tgt != null ? sqlTarget : null));
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
		final IProcedure srcProc = (IProcedure) source;
		final IProcedure tgtProc = (IProcedure) target;
		final IProcedure ancProc = (IProcedure) ancestor;
		List<IComparisonItem> srcItems = null;
		if (getMergeStrategy().getComparisonScope() == ComparisonScope.REPOSITORY) {
			srcItems = MergeUtils.mergeCompare(srcProc.getSQLSource(), tgtProc.getSQLSource(),
					ancProc == null ? null : ancProc.getSQLSource());
		} else {
			srcItems = MergeUtils.mergeCompare(cleanSource(srcProc.getSQLSource()).toLowerCase(),
					cleanSource(tgtProc.getSQLSource()).toLowerCase(), ancProc == null ? null
							: cleanSource(ancProc.getSQLSource()).toLowerCase());
		}
		addMergedSubItems(result, ATTR_SOURCE, srcItems);
	}

	@Override
	protected void cleanLineByLineSubItems(IComparisonItem result) {
		removeMergedSubItems(result, ATTR_SOURCE);
	}

}
