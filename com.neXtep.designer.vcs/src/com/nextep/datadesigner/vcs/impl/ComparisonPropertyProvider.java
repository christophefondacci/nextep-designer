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
import com.nextep.datadesigner.model.IProperty;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * A property provider which uses the comparison framework to publish the properties. A comparison
 * is made between the item and a null object and the comparison items will be wrapped into
 * properties.
 * 
 * @author Christophe Fondacci
 */
public class ComparisonPropertyProvider implements IPropertyProvider {

	private Object model;

	public ComparisonPropertyProvider(Object model) {
		this.model = model;
	}

	/**
	 * @see com.nextep.datadesigner.model.IPropertyProvider#getProperties()
	 */
	@Override
	public List<IProperty> getProperties() {
		if (model instanceof IReferenceable) {
			IMerger m = MergerFactory.getMerger((IReferenceable) model, ComparisonScope.REPOSITORY);
			if (m != null && m instanceof Merger) {
				IComparisonItem item = ((Merger) m).doCompare((IReferenceable) model, null);
				return getChildProperties(item);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * @see com.nextep.datadesigner.model.IPropertyProvider#setProperty(com.nextep.datadesigner.model.IProperty)
	 */
	@Override
	public void setProperty(IProperty property) {
		// TODO Auto-generated method stub

	}

	public static List<IProperty> getChildProperties(IComparisonItem item) {
		Collection<IComparisonItem> processed = new ArrayList<IComparisonItem>();
		List<IProperty> children = new ArrayList<IProperty>();
		// Quick fix of a NPE here
		if (item == null)
			return children;
		for (String categ : item.getCategories()) {
			List<IComparisonItem> subItems = item.getSubItems(categ);
			if (subItems != null) {
				// A global container for all our categorized sub items
				ComparisonProperty category = new ComparisonProperty(categ, null);
				// By default we consider it is EQUAL, unless a child item says it is not
				category.setDifferenceType(DifferenceType.EQUALS);
				children.add(category);
				for (IComparisonItem i : subItems) {
					if (i != null) {
						category.addChild(new ComparisonPropertyWrapper(categ, i));
						// Adjusting category property node difference according to children
						if (i.getDifferenceType() != DifferenceType.EQUALS) {
							category.setDifferenceType(DifferenceType.DIFFER);
						}
					}
					processed.add(i);
				}
			}
		}
		int index = 0;
		for (IComparisonItem i : item.getSubItems()) {
			if (!processed.contains(i)) {
				children.add(index++, new ComparisonPropertyWrapper("", i));
			}
		}
		return children;
	}
}
