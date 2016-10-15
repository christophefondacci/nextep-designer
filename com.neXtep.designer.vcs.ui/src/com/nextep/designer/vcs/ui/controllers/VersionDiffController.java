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
/**
 *
 */
package com.nextep.designer.vcs.ui.controllers;

import java.util.ArrayList;
import java.util.List;
import com.nextep.datadesigner.gui.model.InvokableController;
import com.nextep.datadesigner.vcs.gui.dialog.VersionDiffViewer;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 *
 */
public class VersionDiffController extends InvokableController {

	/**
	 * @see com.nextep.datadesigner.model.IInvokable#invoke(java.lang.Object[])
	 */
	@Override
	public Object invoke(Object... arg) {
		List<IComparisonItem> comps = new ArrayList<IComparisonItem>();
		for(Object o : arg) {
			comps.add((IComparisonItem)o);
//			expandLineByLine((IComparisonItem)o);
		}
//		IMerger m = MergerFactory.getInstance().getMerger(comp.getType());
//		if(m != null) {
//			m.merge(comp, null, null);
//		}
		invokeGUI(new VersionDiffViewer(comps.toArray(new IComparisonItem[comps.size()])));
		// Since hibernate may have altered some objects, we reload the entire view
//		VersionHelper.setCurrentView((IVersionView)IdentifiableDAO.getInstance().load(VersionView.class, VersionHelper.getCurrentView().getUID()));
		// TODO Auto-generated method stub
		return null;
	}

//	private void expandLineByLine(IComparisonItem item) {
//		IMerger m = MergerFactory.getMerger(item.getType(), ComparisonScope.REPOSITORY);
//		if(m instanceof MergerWithMultilineAttributes) {
//			try {
//				((MergerWithMultilineAttributes)m).addLineByLineSubItems(item, null, item.getSource(), item.getTarget());
//			} catch(IOException e) {
//				throw new ErrorException(e);
//			}
//		} else {
//			for(IComparisonItem subItem : item.getSubItems()) {
//				expandLineByLine(subItem);
//			}
//		}
//	}
}
