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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * @author Christophe Fondacci
 *
 */
public class DiagramMerger extends MergerWithChildCollections {

	private static final String CATEGORY_ITEMS = "Diagram contents"; 
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object, com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		IDiagram diagram = (IDiagram)target;
		fillName(result, diagram);
		if(diagram.getName() == null || "".equals(diagram.getName())) {
			return null;
		}
		save(diagram);
		// Building merged contents
		List<?> contents = getMergedList(CATEGORY_ITEMS, result, activity);
		for(Object o : contents) {
			IDiagramItem item = (IDiagramItem)o;
			item.setParentDiagram(diagram);
			save(item);
			diagram.getItems().add(item);

		}
		// Saving filled diagram and returning it
		save(diagram);
		return diagram;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IDiagram src = (IDiagram)source;
		IDiagram tgt = (IDiagram)target;
		
		IComparisonItem result = new ComparisonResult(source,target,ComparisonScope.REPOSITORY); //getMergeStrategy().getComparisonScope());
		// Adding name / description comparison information
		compareName(result, src, tgt);
		// Comparing diagram contents (items)
		listCompare(CATEGORY_ITEMS, result,
				src == null ? Collections.EMPTY_LIST : adapt(src.getItems()),
				tgt == null ? Collections.EMPTY_LIST : adapt(tgt.getItems())
			);
		// Returning the comparison result
		return result;
	}

	private Collection<IDiagramItem> adapt(Collection<IDiagramItem> items) {
		List<IDiagramItem> adaptedItems = new ArrayList<IDiagramItem>();
		for(IDiagramItem item : items) {
			adaptedItems.add(new ReferenceableDiagramItemAdapter(item));
		}
		return adaptedItems;
	}
}
