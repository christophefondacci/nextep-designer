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
package com.nextep.designer.vcs.ui.services;

import java.util.List;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IComparisonManager;
import com.nextep.designer.vcs.ui.compare.IComparisonEditorProvider;

/**
 * An extension to the {@link IComparisonManager} which provides UI related methods for comparing
 * elements.
 * 
 * @author Christophe Fondacci
 */
public interface IComparisonUIManager extends IComparisonManager {

	/**
	 * Defines the current implementation which will provide the comparison editor to the workbench
	 * to display comparisons.
	 * 
	 * @param editorProvider a {@link IComparisonEditorProvider} providing the comparison editors
	 */
	void setComparisonEditorProvider(IComparisonEditorProvider editorProvider);

	/**
	 * Retrieves the current comparison editor provider which is able to provide the comparison
	 * editor to open the comparison with. When <code>type</code> is not null, any specialized
	 * comparison editor provider registered against this type will be returned, even if there is
	 * another current provider selected.
	 * 
	 * @param type a {@link IElementType} for which caller want a comparison editor provider, or
	 *        <code>null</code> to indicate that global current editor provider should be retrieved.
	 * @return a {@link IComparisonEditorProvider} implementation
	 */
	IComparisonEditorProvider getComparisonEditorProvider(IElementType type);

	/**
	 * Retrieves all available comparison editor providers registered in the current running
	 * environment. Providers will be sorted by their label.
	 * 
	 * @param type a type for which we should retrieve providers, when specified the list may
	 *        contain some specific {@link IComparisonEditorProvider} to this type, could be
	 *        <code>null</code> for global providers
	 * @return a list of all defined {@link IComparisonEditorProvider} sorted by label
	 */
	List<IComparisonEditorProvider> getAvailableComparisonEditorProviders(IElementType type);

	/**
	 * Opens the comparison editor for the specified {@link IComparisonItem} with the given
	 * {@link IComparisonEditorProvider}.
	 * 
	 * @param item item to compare
	 * @param provider provider of the editor-related information for comparing the input
	 */
	void openComparisonEditor(IComparisonItem item, IComparisonEditorProvider provider);

	/**
	 * Shows the specified comparison items visually to the user.
	 * 
	 * @param items the series of {@link IComparisonItem} to show to the user
	 */
	void showComparison(String description, IComparisonItem... items);
}
