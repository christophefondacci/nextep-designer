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
package com.nextep.datadesigner.vcs.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 *
 */
public class AttributeMerger extends Merger {
	private static final Log log = LogFactory.getLog(AttributeMerger.class);

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
//		log.warn("Trying to compare attributes");
		throw new ErrorException("Trying to compare attributes");
		// TODO Auto-generated method stub
//		return null;
	}
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#isVersionable()
	 */
	@Override
	public boolean isVersionable() {
		return false;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object, com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		log.error("Trying to fill attribute.");
		return null;
	}
}
