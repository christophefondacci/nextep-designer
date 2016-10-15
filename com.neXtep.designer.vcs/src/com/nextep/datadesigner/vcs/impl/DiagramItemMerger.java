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

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * @author Christophe Fondacci
 *
 */
public class DiagramItemMerger extends Merger {

	private static final String ATTR_X = "X";
	private static final String ATTR_Y = "Y";
	private static final String ATTR_WIDTH = "Width";
	private static final String ATTR_HEIGHT ="Height";
	private static final String ATTR_ITEMREF="Referenced item"; 
	
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object, com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		IDiagramItem item = (IDiagramItem)target;
		
		// Filling item attributes
			// Checking item presence (diagram item must have X,Y coordinates)
		if(getStringProposal(ATTR_X, result)==null) {
			return null;
		}
		item.setXStart(Integer.parseInt(getStringProposal(ATTR_X, result)));
		item.setYStart(Integer.parseInt(getStringProposal(ATTR_Y, result)));
		item.setWidth(Integer.parseInt(getStringProposal(ATTR_WIDTH, result)));
		item.setHeight(Integer.parseInt(getStringProposal(ATTR_HEIGHT, result)));
		// Filling item reference
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		IReference r = (IReference)m.buildMergedObject(result.getSubItems(ATTR_ITEMREF).iterator().next(), activity);
		item.setItemReference(r);
		// Returning filled object
		return item;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IDiagramItem src = (IDiagramItem)source;
		IDiagramItem tgt = (IDiagramItem)target;
		// Initializing our comparison result
		IComparisonItem result = new ComparisonResult(source,target,getMergeStrategy().getComparisonScope());
		// Filling diagram item comparison informations
		result.addSubItem(new ComparisonAttribute(ATTR_X,src == null ? null : String.valueOf(src.getXStart()), tgt == null ? null : String.valueOf(tgt.getXStart())));
		result.addSubItem(new ComparisonAttribute(ATTR_Y,src == null ? null : String.valueOf(src.getYStart()), tgt == null ? null : String.valueOf(tgt.getYStart())));
		result.addSubItem(new ComparisonAttribute(ATTR_WIDTH,src == null ? null : String.valueOf(src.getWidth()), tgt == null ? null : String.valueOf(tgt.getWidth())));
		result.addSubItem(new ComparisonAttribute(ATTR_HEIGHT,src == null ? null : String.valueOf(src.getHeight()), tgt == null ? null : String.valueOf(tgt.getHeight())));
		// Comparing referenced model
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		result.addSubItem(ATTR_ITEMREF,m.compare(src == null ? null : src.getItemReference(), tgt == null ? null : tgt.getItemReference()));

		// Returning our comparison result
		return result;
	}
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#isVersionable()
	 */
	@Override
	public boolean isVersionable() {
		return false;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#copyWhenUnchanged()
	 */
	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#createTargetObject(com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object createTargetObject(IComparisonItem result,
			IActivity mergeActivity) {
		return new DiagramItem();
	}
}
