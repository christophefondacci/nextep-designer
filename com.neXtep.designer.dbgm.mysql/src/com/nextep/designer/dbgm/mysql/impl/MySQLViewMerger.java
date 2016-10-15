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
package com.nextep.designer.dbgm.mysql.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.mergers.ViewMerger;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;

public class MySQLViewMerger extends ViewMerger {

	// private static final Log log = LogFactory.getLog(MySQLViewMerger.class);
	@Override
	protected String cleanSourceCode(String originalSource) {
		if (originalSource == null)
			return null;
		if (getMergeStrategy().getComparisonScope() == ComparisonScope.REPOSITORY) {
			return originalSource;
		}
		String s = originalSource.replace("\r", "");
		s = s.replaceAll("(\\s)+\n", "\n");
		s = s.replaceAll("\n(\\s)+", "\n");
		// s = s.replace("\n", " ");
		s = s.replaceAll(",(\\s)+", ",");

		// Replacing dml by lowercase
		ISQLParser parser = GeneratorFactory.getSQLParser(DBVendor.MYSQL);
		Map<String, List<String>> typedTokens = parser.getTypedTokens();
		List<String> dmlWords = new ArrayList<String>(typedTokens.get(ISQLParser.DML));
		dmlWords.addAll(typedTokens.get(ISQLParser.FUNC));
		final String upperStr = s.toUpperCase();
		for (String w : dmlWords) {
			int index = upperStr.indexOf(w);
			final String lower = w.toLowerCase();
			while (index != -1) {
				s = s.substring(0, index) + lower + s.substring(index + w.length());
				index = upperStr.indexOf(w, index + 1);
			}
		}
		s = s.replace("(\\s)+(as)(\\s)+", " AS ");
		s = s.replace("(\\s)+(As)(\\s)+", " AS ");
		s = s.replace("(\\s)+(aS)(\\s)+", " AS ");
		s = s.replace("count(*)", "count(0)");
		return super.cleanSourceCode(s.trim());
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IView src = (IView) source;
		IView tgt = (IView) target;

		ComparisonResult result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		compareName(result, src, tgt);
		String srcCode = src != null ? cleanSourceCode(src.getSQLDefinition()) : null;
		String tgtCode = tgt != null ? cleanSourceCode(tgt.getSQLDefinition()) : null;

		if (srcCode != null && tgtCode != null) {
			if (srcCode.contains("\n")) {
				tgtCode = cleanSourceCode(adaptLines(srcCode, tgtCode));
			} else {
				srcCode = cleanSourceCode(adaptLines(tgtCode, srcCode));
			}
		}

		result.addSubItem(new ComparisonAttribute(ATTR_SQL, srcCode, tgtCode));
		if (src != null && tgt != null) {
			try {
				addLineByLineSubItems(result, null, source, target);
			} catch (IOException e) {
				throw new ErrorException(e);
			}
		}
		return result;
	}

	@Override
	public void addLineByLineSubItems(IComparisonItem result, IReferenceable ancestor,
			IReferenceable source, IReferenceable target) throws IOException {
		String srcCode = source != null ? cleanSourceCode(((IView) source).getSQLDefinition())
				: null;
		String tgtCode = target != null ? cleanSourceCode(((IView) target).getSQLDefinition())
				: null;

		if (srcCode != null && tgtCode != null) {
			if (srcCode.contains("\n")) {
				tgtCode = cleanSourceCode(adaptLines(srcCode, tgtCode));
			} else {
				srcCode = cleanSourceCode(adaptLines(tgtCode, srcCode));
			}
		}
		List<IComparisonItem> items = MergeUtils.mergeCompare(srcCode, tgtCode,
				ancestor == null ? null : cleanSourceCode(((IView) ancestor).getSQLDefinition()));
		addMergedSubItems(result, ATTR_SQL, items);
	}

	private String adaptLines(String from, String to) {
		if (getMergeStrategy().getComparisonScope() == ComparisonScope.REPOSITORY) {
			return to;
		}
		int offset = 0;
		int count = 0;
		int index = from.indexOf('\n');
		StringBuffer out = new StringBuffer(from.length());
		while (index != -1) {
			if (index > to.length())
				break;
			out.append(to.substring(offset, index) + '\n');
			count++;
			offset = index;
			if (index < from.length() - 1) {
				index = from.indexOf('\n', index + 1);
			} else {
				break;
			}
		}
		if (offset < to.length()) {
			out.append(to.substring(offset));
		}
		return out.toString();
	}
	// if(getMergeStrategy().getComparisonScope().isCompatible(ComparisonScope.REPOSITORY)) {
	// return originalSource;
	// }
	// // TODO retrieve the current synchronization connection when it will not always be DEV
	// IConnection conn = DBGMHelper.getTargetSet().getTarget(TargetType.DEVELOPMENT);
	// if(conn==null) return originalSource;
	// final String schema = conn.getSID();
	// // Removing the schema prefix
	// String s = originalSource.replace("`" + schema + "`.","");
	// // Removing all name encapsulation
	// s = s.replace("`", "");
	// s = s.replaceAll("_(latin1|latin2|ascii|cp125.|utf8|macce)'", "'");
	// // Removing multiline comments (regexp generate stack overflow
	// int index = s.indexOf("/*");
	// while(index!=-1) {
	// int end = s.indexOf("*/",index+2);
	// s = s.substring(0,index) + ((end == -1) ? "" : s.substring(end+2));
	//
	// index = s.indexOf("/*");
	// }
	// return s;
	// }
}
