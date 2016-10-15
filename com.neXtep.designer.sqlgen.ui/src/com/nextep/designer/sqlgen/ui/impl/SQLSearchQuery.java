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

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import com.nextep.datadesigner.dbgm.model.ISqlBased;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * A query for searching a pattern within the SQL code of repository elements.
 * 
 * @author Christophe Fondacci
 *
 */
public class SQLSearchQuery implements ISearchQuery {
	/** the text to search for */
	private String searchedText;
	/** Defines whether the searched text is a regular expression or not */
	private boolean isRegexp;
	/** Are we looking for a whole word ? */
	private boolean wholeWord;
	private SQLSearchResult result;
	public SQLSearchQuery(String searchedText, boolean isRegexp, boolean wholeWord) {
		this.searchedText = searchedText;
		this.isRegexp = isRegexp;
		this.wholeWord = wholeWord;
		result = new SQLSearchResult(this);
	}
	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public String getLabel() {
		return "Looking for '" + searchedText + "' SQL references...";
	}
	public String getSearchedText() {
		return searchedText;
	}

	@Override
	public ISearchResult getSearchResult() {
		return result;
	}

	@Override
	public IStatus run(IProgressMonitor monitor)
			throws OperationCanceledException {
		Pattern p = null;
		String patternString = searchedText;
		if(!isRegexp) {
			patternString = FindReplaceDocumentAdapter.escapeForRegExPattern(searchedText);
		}
		if(wholeWord) {
			p = Pattern.compile("(\\W|\\s|^)" + patternString.toUpperCase() + "(\\W|\\s|$)");
		} else {
			p = Pattern.compile(patternString.toUpperCase());
		}
		// Fetching all objects from current view
		final Collection<IVersionable<?>> versionables = VersionHelper.getAllVersionables(VersionHelper.getCurrentView(), null);
		for(IVersionable<?> v : versionables) {
			if(v instanceof ISqlBased) {
				// Preparing regexp search
				String source = ((ISqlBased) v).getSql().toUpperCase();
				final Matcher m = p.matcher(source);
				while(m.find()) {
					int offset = m.group().indexOf(patternString.toUpperCase());
					result.addMatch(new Match(v,m.start()+offset,patternString.length()));
				}
			}
		}
		return Status.OK_STATUS;
	}

}
