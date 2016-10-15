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

import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * A reference merger which will not compare referenced instances
 * while comparing references to import from db into repository.
 * 
 * @author Christophe
 *
 */
public class ReferenceNonTransitiveMerger extends ReferenceTransitiveMerger {

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		Reference src = (Reference)source;
		Reference tgt = (Reference)target;
		ComparisonResult r = new ComparisonResult(src,tgt,getMergeStrategy().getComparisonScope());

		return r;
	}
}
