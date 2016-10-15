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
package com.nextep.designer.vcs.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.VersioningOperation;

/**
 * Initializes the default preferences for version control.
 * 
 * @author Christophe Fondacci
 */
public class VersioningPreferenceInitializer extends AbstractPreferenceInitializer {

	public VersioningPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences prefs = new DefaultScope().getNode(VCSPlugin.PLUGIN_ID);
		// Default is a patch increment
		prefs.put(VersioningPreferenceConstants.DEFAULT_RELEASE_INCREMENT,
				String.valueOf(IVersionInfo.PATCH));
		// Default confirmation on checkout and commit
		prefs.put(VersioningPreferenceConstants.VALIDATE_PREFIX + VersioningOperation.CHECKOUT,
				Boolean.TRUE.toString());
		prefs.put(VersioningPreferenceConstants.VALIDATE_PREFIX + VersioningOperation.COMMIT,
				Boolean.TRUE.toString());

	}

}
