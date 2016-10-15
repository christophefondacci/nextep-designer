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
package com.nextep.designer.ui.model.base;

import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.model.IFormActionProvider;

/**
 * This abstract class should always be used as a base class for {@link IFormActionProvider} as it
 * can absorb interface changes easily. This default abstract implementation defines no edition
 * support and no sorting support.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractFormActionProvider implements IFormActionProvider {

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isSortable() {
		return false;
	}

	@Override
	public boolean isAddRemoveEnabled() {
		return true;
	}

	public void edit(ITypedObject parent, ITypedObject element) {
	}

	@Override
	public void down(ITypedObject parent, ITypedObject element) {
	}

	@Override
	public void up(ITypedObject parent, ITypedObject element) {
	}
}
