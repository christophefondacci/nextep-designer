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
package com.nextep.designer.vcs.ui.compare;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * This interface describes elements which are able to provide the editor inputs and editor ids for
 * a given comparison.
 * 
 * @author Christophe Fondacci
 */
public interface IComparisonEditorProvider {

	/**
	 * Retrieves the editor input for comparing {@link IComparisonItem} elements.
	 * 
	 * @param compItem {@link IComparisonItem} containing the comparison information
	 * @return the editor input to pass to the comparison editor
	 */
	IEditorInput getEditorInput(IComparisonItem comparisonItem);

	/**
	 * Retrieves the editor id to use for comparing a given {@link IComparisonItem}
	 * 
	 * @param comparisonItem comparison information to display in editor
	 * @return editor id the identifier of the comparison editor that will display the comparison to
	 *         the user
	 */
	String getEditorId(IComparisonItem comparisonItem);

	/**
	 * Retrieves the image associated with this comparison editor provider. The provided icon will
	 * be used to update UI controls when the user selects usage of this provider. Typically, the
	 * returned image should be the same as the one for the contributed UI command.
	 * 
	 * @return the icon for this provider
	 */
	Image getIcon();
	
	/**
	 * Provides a user-friendly label to the user corresponding to this comparison editor provider. 
	 * It will allow the user to know what kind of provider this element is, in addition to the
	 * provided icon.
	 * 
	 * @return the label which will be displayed in the UI menus.
	 */
	String getLabel();

}
