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

import java.io.IOException;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerWithMultilineAttributes;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class ViewMerger extends MergerWithMultilineAttributes {

	public static final String ATTR_SQL = "View source";
	public static final String ATTR_ALIAS_PREFIX = "Column ";
	public static final String CATEG_ALIASES = "Column aliases";

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IView view = (IView) target;
		fillName(result, view);
		if (view.getName() == null) {
			return null;
		}
		view.setSQLDefinition(mergeAttribute(result, ATTR_SQL));
		// List<IComparisonItem> aliases = result.getSubItems(CATEG_ALIASES);
		// int i = 1;
		// for(IComparisonItem item : aliases) {
		// view.addColumnAlias(getStringProposal(ATTR_ALIAS_PREFIX + i, item));
		// i++;
		// }
		// Returning filled object
		return view;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IView src = (IView) source;
		IView tgt = (IView) target;

		ComparisonResult result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		compareName(result, src, tgt);
		result.addSubItem(new ComparisonAttribute(ATTR_SQL, src != null ? cleanSourceCode(src
				.getSQLDefinition()) : null, tgt != null ? cleanSourceCode(tgt.getSQLDefinition())
				: null));
		// // Comparing column aliases
		// int i = 1;
		// Iterator<String> tgtIt = tgt != null ? tgt.getColumnAliases().iterator() :
		// Collections.EMPTY_LIST.iterator();
		// for(String s : src != null ? src.getColumnAliases() :
		// (List<String>)Collections.EMPTY_LIST) {
		// String targetVal = tgtIt.hasNext() ? tgtIt.next() : "$$NULL VALUE$$";
		// targetVal = (targetVal==null ? "" : "$$NULL VALUE$$".equals(targetVal) ? null : targetVal
		// );
		// result.addSubItem(CATEG_ALIASES, new ComparisonAttribute(ATTR_ALIAS_PREFIX +
		// i,s==null?"":s,targetVal));
		// i++;
		// }
		// while(tgtIt.hasNext()) {
		// String targetVal = tgtIt.next();
		// result.addSubItem(CATEG_ALIASES, new ComparisonAttribute(ATTR_ALIAS_PREFIX +
		// i,null,targetVal == null ? "" : targetVal));
		// i++;
		// }
		if (src != null && tgt != null) {
			try {
				addLineByLineSubItems(result, null, source, target);
			} catch (IOException e) {
				throw new ErrorException(e);
			}
		}
		// Returning comparison result
		return result;
	}

	protected String cleanSourceCode(String originalSource) {
		if (getMergeStrategy().getComparisonScope() != ComparisonScope.REPOSITORY) {
			return cleanSQL(originalSource);
		}
		if(originalSource != null) {
			return originalSource;
		} else {
			return "";
		}
	}

	private String cleanSQL(String sql) {
		if (sql == null)
			return null;
		String cleaned = sql.trim();
		if (cleaned.length() > 0) {
			char lastCar = cleaned.charAt(cleaned.length() - 1);
			if (lastCar == ';') {
				cleaned = cleaned.substring(0, cleaned.length() - 1);
			}

			return cleaned.replace("\r", "").replaceAll("( )+\n", "\n").trim();
		} else {
			return "";
		}
	}

	@Override
	public void addLineByLineSubItems(IComparisonItem result, IReferenceable ancestor,
			IReferenceable source, IReferenceable target) throws IOException {
		final IView src = (IView)source;
		final IView tgt = (IView)target;
		final IView anc = (IView)ancestor;
		
		final String srcText = src == null ? "" : cleanSourceCode(src.getSql());
		final String tgtText = tgt == null ? "" : cleanSourceCode(tgt.getSql());
		final String ancText = anc== null ? null : cleanSourceCode(anc.getSql());
		
		List<IComparisonItem> items = MergeUtils.mergeCompare(srcText, tgtText,ancText);
		addMergedSubItems(result, ATTR_SQL, items);
	}

	@Override
	protected void cleanLineByLineSubItems(IComparisonItem result) {
		removeMergedSubItems(result, ATTR_SQL);
	}

}
