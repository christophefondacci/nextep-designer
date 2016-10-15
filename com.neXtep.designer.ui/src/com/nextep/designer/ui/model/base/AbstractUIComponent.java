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
package com.nextep.designer.ui.model.base;

import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;

/**
 * Base class for {@link IUIComponent} providing implementation for container injection. This is a
 * convenience base class, the {@link IUIComponent} may be implemented directly.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractUIComponent implements IUIComponent {

	private IUIComponentContainer container;

	@Override
	public void setUIComponentContainer(IUIComponentContainer container) {
		this.container = container;
	}

	@Override
	public IUIComponentContainer getUIComponentContainer() {
		return container;
	}

	@Override
	public void dispose() {
	}
}
