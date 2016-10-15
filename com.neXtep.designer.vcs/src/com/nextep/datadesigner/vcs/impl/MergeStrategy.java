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
package com.nextep.datadesigner.vcs.impl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMergeStrategy;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.MergeStatus;

/**
 * @author Christophe Fondacci
 */
public abstract class MergeStrategy implements IMergeStrategy {

	private static boolean caseSensitive = false;
	private static boolean isGenerating = true;

	protected MergeStatus mergeSubItems(IComparisonItem result, IComparisonItem sourceRootDiff,
			IComparisonItem targetRootDiff) {
		MergeStatus status = MergeStatus.NOT_MERGED;
		for (IComparisonItem item : result.getSubItems()) {
			IMerger m = MergerFactory.getMerger(item.getType(), getComparisonScope());
			if (m != null) {
				m.setMergeStrategy(this);
				if (item.getReference() != null) {
					status = status
							.compute(item.getMergeInfo().getStatus() == MergeStatus.NOT_MERGED ? m
									.merge(item,
											sourceRootDiff == null ? null : sourceRootDiff
													.getSubItem(item.getReference()),
											targetRootDiff == null ? null : targetRootDiff
													.getSubItem(item.getReference())).getStatus()
									: item.getMergeInfo().getStatus());
				}
			} else {
				status = status.compute(MergeStatus.NOT_MERGEABLE);
				// Ambiguity, cannot set
				// result.setMergeProposal(null);
			}
		}
		// Handling case of no subitem
		if (status == MergeStatus.NOT_MERGED) {
			status = MergeStatus.MERGE_UNRESOLVED;
		}
		return status;
	}

	public static IMergeStrategy create(ComparisonScope scope) {
		switch (scope) {
		case DATABASE:
			return new MergeStrategyDatabase();
		case REPOSITORY:
			return new MergeStrategyRepository();
		case DB_TO_REPOSITORY:
			return new MergeStrategyDBToRepository();
		}
		throw new ErrorException("Unsupported merge scope: " + scope.name());
	}

	/**
	 * @param o object to drop or not
	 * @return whether the merger should suggest a drop or not
	 */
	protected boolean shouldDrop(Object o) {
		if (!isGenerating())
			return false;
		IConfigurationElement elt = Designer.getInstance().getExtension(
				ICommand.COMMAND_EXTENSION_ID, "name", "suggestDrop"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			ICommand registerConnCmd = (ICommand) elt.createExecutableExtension("class"); //$NON-NLS-1$
			return (Boolean) registerConnCmd.execute(o);
		} catch (CoreException e) {
			throw new ErrorException("Unable to determine merge drop strategy, "
					+ "please check the generator drop strategies preferences", e);
		}
	}

	public static boolean isCaseSensitive() {
		return caseSensitive;
	}

	public static void setCaseSensitive(boolean caseSensitive) {
		MergeStrategy.caseSensitive = caseSensitive;
	}

	/**
	 * Defines if the merge strategies are used for a generation operation. The default is
	 * <code>true</code>. Call this method to change the strategy mode. TODO: QUICKFIX - replace
	 * this with a new MergeStrategy for a new ComparisonScope like REPOSITORY_GENERATION.
	 * 
	 * @param generating generating mode
	 */
	public static void setIsGenerating(boolean generating) {
		MergeStrategy.isGenerating = generating;
	}

	/**
	 * @return <code>true</code> for repository generations, <code>false</code> for standard merges.
	 * @see MergeStrategy#isGenerating()
	 */
	public static boolean isGenerating() {
		return isGenerating;
	}

}
