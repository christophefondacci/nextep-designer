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
package com.nextep.designer.vcs.controllers;

import java.util.ArrayList;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.base.AbstractController;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

public class VersionViewController extends AbstractController {

	@Override
	public void modelChanged(Object content) {
	}

	@Override
	public void modelDeleted(Object content) {
		final IWorkspace view = (IWorkspace) content;
		// Removing all items.
		for (IVersionable<?> v : new ArrayList<IVersionable<?>>(view.getContents())) {
			view.removeVersionable(v);
		}
		// Saving view, for views loaded from the view selector, they will not contain any content
		// so all previous content will be erased => this is what we want here.
		save(view);
		CorePlugin.getIdentifiableDao().delete(view);
	}

}
