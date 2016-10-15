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

import org.eclipse.ui.forms.IManagedForm;

/**
 * A specific extension to a regular component container that provides access to global form
 * singletons that are needed to create contents.
 * 
 * @author Christophe Fondacci
 */
public interface IFormComponentContainer extends IUIComponentContainer {

	/**
	 * Provides the managed form that should be used to create forms UI components
	 * 
	 * @return the {@link IManagedForm}
	 */
	IManagedForm getForm();
}
