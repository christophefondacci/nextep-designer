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
package com.nextep.designer.synch.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import com.nextep.designer.synch.ui.SynchUIPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences
	 * ()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = SynchUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PROP_NOTIFY_SWITCH_PERSPECTIVE, true);
		store.setDefault(PreferenceConstants.PROP_SELECT_DEPENDENCIES_WITHOUT_PROMPTING, false);
	}

}
