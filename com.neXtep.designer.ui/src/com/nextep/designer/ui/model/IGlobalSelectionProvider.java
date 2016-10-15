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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.ui.model;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Christophe Fondacci
 */
public interface IGlobalSelectionProvider extends ISelectionProvider {

	/**
	 * Registers the selection provider for a workbench part. When this part is shown in a
	 * multi-page editor context, the global selection provider will use the associated registered
	 * selection provider to provide the selection to the workbench.
	 * 
	 * @param pageEditor the {@link IWorkbenchPart} for which the selection provider should be
	 *        registered
	 * @param provider the corresponding part's inner {@link ISelectionProvider}
	 */
	void registerSelectionProvider(IWorkbenchPart pageEditor, ISelectionProvider provider);
}
