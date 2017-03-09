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
import java.util.regex.PatternSyntaxException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import com.nextep.datadesigner.dbgm.model.ISqlBased;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * A query for searching a pattern within the SQL code of repository elements.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class SQLSearchQuery implements ISearchQuery {

	// The plug-in ID
	private static final String PLUGIN_ID = "com.neXtep.designer.sqlgen.ui"; //$NON-NLS-1$

	// The text to search for
	private String searchedText;
	// Defines whether the searched text is a regular expression or not
	private boolean isRegexp;
	// Are we looking for a whole word?
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
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		if (searchedText == null || "".equals(searchedText)) { //$NON-NLS-1$
			return new Status(Status.WARNING, PLUGIN_ID, "Search cancelled: empty search string"); //$NON-NLS-1$
		}
		final int searchedTextLength = searchedText.length();

		/*
		 * If the search string is a regular expression, we take it as is. Otherwise, we turn it
		 * into a literal pattern string, and encapsulate it with word boundary matchers if the
		 * "Whole word only" checkbox has been checked.
		 */
		final String regex = (isRegexp ? searchedText
				: (wholeWord
						? "\\b" //$NON-NLS-1$
								+ Pattern.quote(searchedText) + "\\b" //$NON-NLS-1$
						: Pattern.quote(searchedText)));

		/*
		 * Regular expression is compiled with the CASE_INSENSITIVE match flag so we don't have to
		 * systematically put all the searched strings in uppercase.
		 */
		Pattern p;
		try {
			p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException pse) {
			return new Status(Status.ERROR, PLUGIN_ID, "Invalid regular expression pattern: " //$NON-NLS-1$
					+ regex);
		}

		// Fetching all objects from the current workspace
		final IWorkspaceService workspaceService = CorePlugin.getService(IWorkspaceService.class);
		final IWorkspace currentWorkspace = workspaceService.getCurrentWorkspace();
		final Collection<IVersionable<?>> versionables = VersionHelper
				.getAllVersionables(currentWorkspace, null);

		// Searching for the specified text in the SQL source code of all workspace objects
		for (IVersionable<?> v : versionables) {
			/*
			 * Objects that are not defined by SQL code are excluded from the search, e.g. tables,
			 * indexes, sequences, synonyms, etc. This is because search results must refer to a
			 * specific line number in a SQL editor.
			 */
			/*
			 * TODO [BGA] We might consider adding a "DDL code" tab to the editors of objects that
			 * are currently not defined by SQL code. This way they could be included to the SQL
			 * search perimeter.
			 */
			if (v instanceof ISqlBased) {
				String source = ((ISqlBased) v).getSql();

				/*
				 * ISSUE GH-14: We need to check if the SQL source of the current database object is
				 * not null or empty before searching for the specified text. User-defined types are
				 * rarely declared with a body, and packages can sometimes have only a specification
				 * (for application-wide constants declaration for example).
				 */
				if (source != null && !"".equals(source)) { //$NON-NLS-1$
					final Matcher m = p.matcher(source);
					while (m.find()) {
						result.addMatch(new Match(v, m.start(), searchedTextLength));
					}
				}
			}
		}

		return Status.OK_STATUS;
	}

}
