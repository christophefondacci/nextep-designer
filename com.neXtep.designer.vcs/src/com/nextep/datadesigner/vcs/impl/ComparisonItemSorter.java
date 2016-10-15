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

import java.util.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionable;

public class ComparisonItemSorter implements Comparator<IComparisonItem> {

	private static final Log log = LogFactory.getLog(ComparisonItemSorter.class);
	@Override
	public int compare(IComparisonItem o1, IComparisonItem o2) {
		// We only put tables first
		if("TABLE".equals(o1.getType().getId()) && "TABLE".equals(o2.getType().getId())) {
			if(o1.getSource()!=null && o2.getSource()!=null && ((IVersionable<?>)o1.getSource()).getReferenceDependencies().contains(o2.getSource().getReference())) {
				log.info( ((IVersionable<?>)o1.getSource()).getName() + " depends on " + ((IVersionable<?>)o2.getSource()).getName()); 
				return 1;
			}
			return -1;
		} else if("TABLE".equals(o1.getType().getId())) {
			return -1;
		} else if("TABLE".equals(o2.getType().getId())) {
			return 1;
		} else {
			return -1;
		}
	}
	

}
