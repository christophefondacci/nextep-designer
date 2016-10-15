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
package com.nextep.designer.synch.ui.model;

import java.util.Collection;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * A categorized type is a wrapper on top of a {@link IElementType} information to handle UI related
 * information for easy JFace's content provider management. It completes the {@link IElementType}
 * info to add the corresponding typed items and the change count.
 * 
 * @author Christophe Fondacci
 */
public interface ICategorizedType {

	/**
	 * The {@link IElementType} corresponding to this category
	 * 
	 * @return a {@link IElementType} for this category
	 */
	IElementType getType();

	/**
	 * Retrieves all {@link IComparisonItem} of this type (category)
	 * @return all {@link IComparisonItem} contained in this category
	 */
	Collection<IComparisonItem> getItems();

	/**
	 * Retrieves the number of changed items in this category. This is a convenience
	 * accessor method.
	 * 
	 * @return the number of non equals comparison items of this category
	 */
	int getChangedItems();
}
