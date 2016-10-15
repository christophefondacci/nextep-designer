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
package com.nextep.datadesigner.vcs.gui.rcp;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorInput;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * This interface describes editor input which can hold comparison information. This editor input
 * should be typically provided by other editor inputs through the
 * {@link IAdaptable#getAdapter(Class)}. For example, a SQL editor input may provide a
 * {@link IComparisonItemEditorInput} by calling <code>
 * getAdapter(IComparisonEditorInput.class)</code>
 * which will provide an input compatible with itself but taking advantage of the comparison.
 * 
 * @author Christophe Fondacci
 */
public interface IComparisonItemEditorInput extends IEditorInput {

	/**
	 * Attaches a {@link IComparisonItem} information to this container
	 * 
	 * @param item comparison item information, resulting from a previous comparison
	 */
	void setComparisonItem(IComparisonItem item);

	/**
	 * Retrieves the {@link IComparisonItem} information for this container. This method may return
	 * <code>null</code> when no comparison information has been attached.
	 * 
	 * @return the comparison item information
	 */
	IComparisonItem getComparisonItem();

	/**
	 * This methods indicates whether the source or the target of the comparison should be displayed
	 * as the content.
	 * 
	 * @param comparedElement the part of the comparison to display
	 */
	void setComparedElement(ComparedElement comparedElement);
	/**
	 * Indicates whether this input should display source or target information from the comparison 
	 * @return the {@link ComparedElement} part of the {@link IComparisonItem} comparison info
	 */
	ComparedElement getComparedElement();
}
