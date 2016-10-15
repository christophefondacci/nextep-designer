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
package com.nextep.designer.vcs.ui.navigators;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.core.runtime.IAdaptable;
import com.nextep.designer.vcs.VCSPlugin;

/**
 * Root element of the version navigator. This element provides the root elements.
 * 
 * @author Christophe Fondacci
 */
public class VersionNavigatorRoot implements IAdaptable {

	private static VersionNavigatorRoot instance;

	private VersionNavigatorRoot() {
	}

	public static VersionNavigatorRoot getInstance() {
		if (instance == null) {
			instance = new VersionNavigatorRoot();
		}
		return instance;
	}

	public Collection<?> getContents() {
		return Arrays.asList(VCSPlugin.getViewService().getCurrentWorkspace());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
}
