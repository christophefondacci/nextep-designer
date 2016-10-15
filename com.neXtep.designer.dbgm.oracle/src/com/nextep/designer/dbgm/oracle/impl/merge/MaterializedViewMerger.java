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

import java.io.IOException;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.impl.MergerWithMultilineAttributes;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.designer.dbgm.oracle.model.BuildType;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.MaterializedViewType;
import com.nextep.designer.dbgm.oracle.model.RefreshMethod;
import com.nextep.designer.dbgm.oracle.model.RefreshTime;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MaterializedViewMerger extends MergerWithMultilineAttributes {

	public static final String ATTR_SQL_QUERY = "SQL Query"; //$NON-NLS-1$
	public static final String ATTR_BUILD_TYPE = "Build type"; //$NON-NLS-1$
	public static final String ATTR_REFRESH_TIME = "Refresh time"; //$NON-NLS-1$
	public static final String ATTR_REFRESH_METHOD = "Refresh method"; //$NON-NLS-1$
	public static final String ATTR_START = "Start expr"; //$NON-NLS-1$
	public static final String ATTR_NEXT = "Next expr"; //$NON-NLS-1$
	public static final String ATTR_VIEW_TYPE = "View type"; //$NON-NLS-1$
	public static final String ATTR_QUERY_REWRITE = "Query rewrite"; //$NON-NLS-1$

	@Override
	public void addLineByLineSubItems(IComparisonItem result, IReferenceable ancestor,
			IReferenceable source, IReferenceable target) throws IOException {
		final IMaterializedView srcView = (IMaterializedView) source;
		final IMaterializedView tgtView = (IMaterializedView) target;
		final IMaterializedView ancView = (IMaterializedView) ancestor;
		final String srcBody = srcView.getSql() == null ? "" : srcView.getSql(); //$NON-NLS-1$
		final String tgtBody = tgtView.getSql() == null ? "" : tgtView.getSql(); //$NON-NLS-1$
		List<IComparisonItem> srcItems = MergeUtils.mergeCompare(srcBody, tgtBody,
				ancView == null ? null : ancView.getSql());
		addMergedSubItems(result, ATTR_SQL_QUERY, srcItems);
	}

	@Override
	protected void cleanLineByLineSubItems(IComparisonItem result) {
		removeMergedSubItems(result, ATTR_SQL_QUERY);
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IMaterializedView view = (IMaterializedView) target;

		// Filling table contents
		Merger m = (Merger) MergerFactory.getMerger(IElementType.getInstance(IBasicTable.TYPE_ID),
				getMergeStrategy().getComparisonScope());
		m.setMergeStrategy(getMergeStrategy());
		m.fill(target, result);

		// Handling removal
		if (view.getName() == null) {
			return null;
		}
		// Filling materialized view properties
		view.setRefreshTime(RefreshTime.valueOf(getStringProposal(ATTR_REFRESH_TIME, result)));
		view.setRefreshMethod(RefreshMethod.valueOf(getStringProposal(ATTR_REFRESH_METHOD, result)));
		view.setViewType(MaterializedViewType.valueOf(getStringProposal(ATTR_VIEW_TYPE, result)));
		view.setStartExpr(getStringProposal(ATTR_START, result));
		view.setNextExpr(getStringProposal(ATTR_NEXT, result));
		view.setBuildType(BuildType.valueOf(getStringProposal(ATTR_BUILD_TYPE, result)));
		view.setQueryRewriteEnabled(Boolean.valueOf(getStringProposal(ATTR_QUERY_REWRITE, result)));
		if (result.getSource() != null && result.getTarget() != null
				&& result.getDifferenceType() != DifferenceType.EQUALS) {
			view.setSql(mergeAttribute(result, ATTR_SQL_QUERY));
		} else {
			view.setSql(getStringProposal(ATTR_SQL_QUERY, result));
		}
		return view;
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
		return source.replace("\r", "").replaceAll("\\s+\n", "\n").replaceAll("\n\n", "\n").trim(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IMaterializedView src = (IMaterializedView) source;
		IMaterializedView tgt = (IMaterializedView) target;
		// Table properties merge
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IBasicTable.TYPE_ID),
				getMergeStrategy().getComparisonScope());
		IComparisonItem result = m.compare(source, target);
		// Materialized view merge
		result.addSubItem(new ComparisonAttribute(ATTR_REFRESH_TIME, src == null ? null : src
				.getRefreshTime().name(), tgt == null ? null : tgt.getRefreshTime().name()));
		result.addSubItem(new ComparisonAttribute(ATTR_REFRESH_METHOD, src == null ? null : src
				.getRefreshMethod().name(), tgt == null ? null : tgt.getRefreshMethod().name()));
		result.addSubItem(new ComparisonAttribute(ATTR_VIEW_TYPE, src == null ? null : src
				.getViewType().name(), tgt == null ? null : tgt.getViewType().name()));
		// Start attribute has a repository scope since there is no way to retrieve it from database
		// after creation
		result.addSubItem(new ComparisonAttribute(ATTR_START, src == null ? null : trim(src
				.getStartExpr()), tgt == null ? null : trim(tgt.getStartExpr()),
				ComparisonScope.REPOSITORY));
		result.addSubItem(new ComparisonAttribute(ATTR_NEXT, src == null ? null : trim(src
				.getNextExpr()), tgt == null ? null : trim(tgt.getNextExpr())));
		result.addSubItem(new ComparisonAttribute(ATTR_BUILD_TYPE, src == null ? null : src
				.getBuildType().name(), tgt == null ? null : tgt.getBuildType().name()));
		result.addSubItem(new ComparisonAttribute(ATTR_SQL_QUERY, src == null ? null
				: clean(cleanSource(src.getSql())), tgt == null ? null : cleanSource(tgt.getSql())));
		result.addSubItem(new ComparisonAttribute(ATTR_QUERY_REWRITE, src == null ? null : String
				.valueOf(src.isQueryRewriteEnabled()), tgt == null ? null : String.valueOf(tgt
				.isQueryRewriteEnabled())));
		if (src != null && target != null) {
			try {
				addLineByLineSubItems(result, null, source, target);
			} catch (IOException e) {
				throw new ErrorException(e);
			}
		}
		return result;
	}

	private String trim(String s) {
		if (getMergeStrategy().getComparisonScope() != ComparisonScope.REPOSITORY) {
			if (s == null)
				return null;
			return s.trim();
		}
		return s;
	}

	private String clean(String sql) {
		if (getMergeStrategy().getComparisonScope() != ComparisonScope.REPOSITORY) {
			String newSQL = sql.trim();
			if (newSQL.charAt(newSQL.length() - 1) == ';') {
				return newSQL.substring(0, newSQL.length() - 1);
			}
			return newSQL;
		}
		return sql;
	}

}
