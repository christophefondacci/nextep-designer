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

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.datadesigner.vcs.impl.MergerWithMultilineAttributes;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.designer.dbgm.mergers.UserTypeMerger;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

public class OracleUserTypeMerger extends MergerWithMultilineAttributes<IOracleUserType> {

	public static final String ATTR_TYPE_BODY = "Type body";

	@Override
	public void addLineByLineSubItems(IComparisonItem result, IReferenceable ancestor,
			IReferenceable source, IReferenceable target) throws IOException {
		final IOracleUserType srcProc = (IOracleUserType) source;
		final IOracleUserType tgtProc = (IOracleUserType) target;
		final IOracleUserType ancProc = (IOracleUserType) ancestor;

		String srcBody = "";
		if (srcProc != null && srcProc.getTypeBody() != null) {
			srcBody = srcProc.getTypeBody();
		}
		String tgtBody = "";
		if (tgtProc != null && tgtProc.getTypeBody() != null) {
			tgtBody = tgtProc.getTypeBody();
		}
		String ancBody = "";
		if (ancProc != null && ancProc.getTypeBody() != null) {
			ancBody = ancProc.getTypeBody();
		}
		List<IComparisonItem> srcItems = MergeUtils.mergeCompare(srcBody, tgtBody, ancBody);
		addMergedSubItems(result, ATTR_TYPE_BODY, srcItems);
	}

	@Override
	protected void cleanLineByLineSubItems(IComparisonItem result) {
		removeMergedSubItems(result, ATTR_TYPE_BODY);
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		Merger<?> m = new UserTypeMerger();
		m.setMergeStrategy(getMergeStrategy());
		m.fill(target, result);
		IOracleUserType t = (IOracleUserType) target;

		if (result.getSource() != null && result.getTarget() != null
				&& result.getDifferenceType() != DifferenceType.EQUALS) {
			t.setTypeBody(mergeAttribute(result, ATTR_TYPE_BODY));
		} else {
			t.setTypeBody(getStringProposal(ATTR_TYPE_BODY, result));
		}

		return t;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IMerger m = new UserTypeMerger();
		m.setMergeStrategy(getMergeStrategy());
		return m.compare(source, target);

	}

	@Override
	protected void fillSpecificComparison(IComparisonItem result, IOracleUserType src,
			IOracleUserType tgt) {
		final String srcBody = src == null ? null : src.getTypeBody() == null ? null : src
				.getTypeBody().trim();
		final String tgtBody = tgt == null ? null : tgt.getTypeBody() == null ? null : tgt
				.getTypeBody().trim();
		result.addSubItem(new ComparisonAttribute(ATTR_TYPE_BODY, srcBody, tgtBody));
		if (src != null && tgt != null) {
			try {
				addLineByLineSubItems(result, null, src, tgt);
			} catch (IOException e) {
				throw new ErrorException(e);
			}
		}
	}

	@Override
	protected Class<? extends IReferenceable> getSpecificClass() {
		return IOracleUserType.class;
	}
}
