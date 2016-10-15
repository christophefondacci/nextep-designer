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
package com.nextep.datadesigner.vcs.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.MergeStatus;

public class MergeUtils {

	public static List<IComparisonItem> mergeCompare(String source, String target)
			throws IOException {

		LineComparator t, o;
		t = new LineComparator(target);
		o = new LineComparator(source);
		// Initializing items list
		String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		if (lineSeparator == null)
			lineSeparator = "\n"; //$NON-NLS-1$
		// Processing differenciation
		RangeDifference[] diffs = RangeDifferencer.findRanges(t, o);
		Comparer comp = new Comparer(o, t);
		for (int i = 0; i < diffs.length; i++) {
			RangeDifference rd = diffs[i];
			comp.process(rd);
		}

		return comp.getItems();
	}

	public static List<IComparisonItem> mergeCompare(String source, String target, String ancestor)
			throws IOException {

		LineComparator t, o, a = null;
		t = new LineComparator(target);
		o = new LineComparator(source);
		if (ancestor != null) {
			a = new LineComparator(ancestor);
		}
		// Initializing items list
		String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		if (lineSeparator == null)
			lineSeparator = "\n"; //$NON-NLS-1$
		// Processing differenciation
		RangeDifference[] diffs = RangeDifferencer.findRanges(null, a, t, o);
		Comparer comp = new Comparer(o, t);
		for (int i = 0; i < diffs.length; i++) {
			RangeDifference rd = diffs[i];
			comp.process(rd);
		}

		return comp.getItems();
	}

	/**
	 * A convenience internal class to help with the line by line comparison
	 * 
	 * @author Christophe
	 */
	private static class Comparer {

		LineComparator o, t;
		List<IComparisonItem> items;
		int index;

		public Comparer(LineComparator o, LineComparator t) {
			this.o = o;
			this.t = t;
			items = new ArrayList<IComparisonItem>();
			index = 1;
		}

		public void process(RangeDifference rd) {
			switch (rd.kind()) {
			// case RangeDifference.ANCESTOR: // pseudo conflict
			case RangeDifference.NOCHANGE:
			case RangeDifference.LEFT:
				addComparisonItem(rd, false, true);
				break;
			case RangeDifference.CONFLICT:
				addComparisonItem(rd, false, false);
				break;
			case RangeDifference.RIGHT:

				addComparisonItem(rd, true, false);
				break;
			default:
				break;
			}
		}

		private void addComparisonItem(RangeDifference rd, boolean rightSel, boolean leftSel) {
			for (int j = rd.rightStart(), k = rd.leftStart(); j < rd.rightEnd() || k < rd.leftEnd(); j++, k++) {
				String right = j < rd.rightEnd() ? o.getLine(j) : null;
				String left = k < rd.leftEnd() ? t.getLine(k) : null;
				ComparisonAttribute attr = new ComparisonAttribute(String.valueOf(index++), right,
						left);
				if (rightSel) {
					attr.getMergeInfo().setStatus(MergeStatus.MERGE_RESOLVED);
					attr.getMergeInfo().setMergeProposal(attr.getSource());
				} else if (leftSel) {
					attr.getMergeInfo().setStatus(MergeStatus.MERGE_RESOLVED);
					attr.getMergeInfo().setMergeProposal(attr.getTarget());
				}
				items.add(attr);
			}
		}

		public List<IComparisonItem> getItems() {
			return items;
		}
	}

	/**
	 * This method computes whether the given item is a target selection. A target selection happens
	 * when :<br>
	 * - The target is explicitly selected by the user<br>
	 * - All the children of a node have the target proposal selected<br>
	 * <br>
	 * This method returns false as soon as one element has not a target selection.<br>
	 * Returns true when <b>all</b> elements are a target selection.
	 * 
	 * @param item a {@link IComparisonItem} to check selected proposals against
	 * @return <code>true</code> if we can consider that the target element of this
	 *         {@link IComparisonItem} is the proposal, else <code>false</code>
	 */
	public static boolean isSelected(IComparisonItem item, ComparedElement element) {
		if (item.getSubItems() == null || item.getSubItems().isEmpty()) {
			return item.getMergeInfo().getMergeProposal() == element.get(item);
		}
		for (IComparisonItem subItem : item.getSubItems()) {
			if (subItem.getDifferenceType() != DifferenceType.EQUALS) {
				if (subItem.getMergeInfo().getMergeProposal() == null) {
					if (element.getOther(subItem) == null && element.get(subItem) != null) {
						return false;
					} else {
						// We can only return if false because otherwise we need to go on with
						// all other items of the loop
						if (!isSelected(subItem, element)) {
							return false;
						}
					}
				} else if (subItem.getMergeInfo().getMergeProposal() != element.get(subItem)) {
					return false;
				}
			}
		}
		return true;
	}
}
