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
package com.nextep.designer.sqlgen.ui.impl;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;

public class SQLSearchResult extends AbstractTextSearchResult {

	private SQLSearchQuery parentQuery;
	public SQLSearchResult(SQLSearchQuery query) {
		this.parentQuery = query;
	}
	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return new SQLEditorMatchAdapter();
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return SearchPluginImages.DESC_OBJ_TSEARCH_DPDN;
	}

	@Override
	public String getLabel() {
		return parentQuery.getSearchedText() + " - SQL occurrences";
	}

	@Override
	public ISearchQuery getQuery() {
		return parentQuery;
	}

	@Override
	public String getTooltip() {
		return getLabel();
	}

	@Override
	public int getMatchCount(Object element) {
		if(element instanceof Match) {
			return 1;
		}
		return super.getMatchCount();
	}
	@Override
	public Match[] getMatches(Object element) {
		if(element instanceof Match) {
			return new Match[] {(Match)element};
		}
		return super.getMatches(element);
	}
}
