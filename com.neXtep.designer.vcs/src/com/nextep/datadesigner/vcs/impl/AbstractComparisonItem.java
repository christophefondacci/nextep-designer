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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;

import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.MergeInfo;

/**
 * Default implementation of the {@link IComparisonItem} interface.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractComparisonItem extends Observable implements IComparisonItem {

	private final IReferenceable source;
	private final IReferenceable target;
	private transient MergeInfo mergeInfo;
	private transient final DifferenceType diffType;
	private final List<IComparisonItem> subItems;
	private transient final Map<String, IComparisonItem> attributesMap;
	private transient final Map<UID, IComparisonItem> refMap;
	private transient final MultiValueMap listMap;
	private transient IComparisonItem parent;
	private ComparisonScope scope = ComparisonScope.ALL;

	public AbstractComparisonItem(IReferenceable source, IReferenceable target,
			DifferenceType diffType) {
		this.source = source;
		this.target = target;
		this.diffType = diffType;
		mergeInfo = new MergeInfo();
		subItems = new ArrayList<IComparisonItem>();
		attributesMap = new HashMap<String, IComparisonItem>();
		refMap = new HashMap<UID, IComparisonItem>();
		listMap = new MultiValueMap();
	}

	@Override
	public DifferenceType getDifferenceType() {
		return diffType;
	}

	@Override
	public IReferenceable getSource() {
		return source;
	}

	@Override
	public IReferenceable getTarget() {
		return target;
	}

	@Override
	public IReference getReference() {
		return (source != null ? source.getReference() : target != null ? target.getReference()
				: null);
	}

	@Override
	public void setReference(IReference ref) {
		// Nonsense
		// if(source instanceof IReference) {
		// source = ref;
		// }
	}

	@Override
	public IElementType getType() {
		if (source instanceof IReference || target instanceof IReference) {
			return IElementType.getInstance(IReference.TYPE_ID);
		} else {
			if (source instanceof ITypedObject || target instanceof ITypedObject) {
				return (source != null ? ((ITypedObject) source).getType()
						: target != null ? ((ITypedObject) target).getType() : IElementType
								.getInstance("ATTRIBUTE")); //$NON-NLS-1$
			} else {
				return IElementType.getInstance("ATTRIBUTE"); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void addSubItem(IComparisonItem subItem) {
		subItem.setParent(this);
		subItems.add(subItem);
		if (subItem.getReference() != null) {
			refMap.put(subItem.getReference().getUID(), subItem);
		} else {
			// log.debug("Null comparison item reference added");
		}
		// Attribute handling for convenience method getAttribute
		if (subItem instanceof ComparisonAttribute) {
			setAttribute((ComparisonAttribute) subItem);
		}
	}

	/**
	 * Adds the specified comparison attribute to the attributes map.
	 * 
	 * @param attr
	 *            comparison attribute to define
	 */
	protected void setAttribute(ComparisonAttribute attr) {
		attributesMap.put(attr.getName(), attr);
	}

	@Override
	public void addSubItem(String category, IComparisonItem subItem) {
		addSubItem(subItem);
		listMap.put(category, subItem);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IComparisonItem> getSubItems(String category) {
		return (List<IComparisonItem>) listMap.get(category);
	}

	@Override
	public List<IComparisonItem> getSubItems() {
		return subItems;
	}

	@Override
	public MergeInfo getMergeInfo() {
		return mergeInfo;
	}

	@Override
	public void setMergeInfo(MergeInfo mergeInfo) {
		this.mergeInfo = mergeInfo;
	}

	@Override
	public IComparisonItem getAttribute(String attributeName) {
		return attributesMap.get(attributeName);
	}

	@Override
	public IComparisonItem getSubItem(IReference ref) {
		return refMap.get(ref.getUID());
	}

	@Override
	public String toString() {
		return getDifferenceType().toString() + ": " + getSource() //$NON-NLS-1$
				+ (getMergeInfo().getMergeProposal() == getSource() ? " [X]" : "") + " / " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ getTarget() + (getMergeInfo().getMergeProposal() == getTarget() ? " [X]" : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ "\n"; //$NON-NLS-1$
	}

	@Override
	public ComparisonScope getScope() {
		return scope;
	}

	@Override
	public void setScope(ComparisonScope scope) {
		this.scope = scope;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getCategories() {
		return listMap.keySet();
	}

	@Override
	public IComparisonItem getParent() {
		return parent;
	}

	@Override
	public void setParent(IComparisonItem parentItem) {
		this.parent = parentItem;
	}

}
