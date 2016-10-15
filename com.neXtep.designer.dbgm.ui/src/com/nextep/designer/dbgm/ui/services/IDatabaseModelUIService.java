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
package com.nextep.designer.dbgm.ui.services;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import com.nextep.datadesigner.dbgm.model.IColumnable;

/**
 * @author Christophe Fondacci
 */
public interface IDatabaseModelUIService {

	/**
	 * Creates the column table editor for the given columnable element under the specified
	 * composite
	 * 
	 * @param part the workbench part hosting this editor (may be null if out of purpose)
	 * @param parentComposite the parent SWT {@link Composite} to create the control in
	 * @param parent the parent {@link IColumnable} to extract column information from
	 * @return the created control, mostly for layout adjustments
	 */
	Composite createColumnEditor(IWorkbenchPart part, Composite parentComposite, IColumnable parent);
}
