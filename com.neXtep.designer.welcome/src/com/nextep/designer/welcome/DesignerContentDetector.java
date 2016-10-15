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
package com.nextep.designer.welcome;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.intro.IntroContentDetector;

/**
 * This class is called when starting the Workbench to determine if the Welcome page should be
 * displayed. Callers should be aware that the {@link #isNewContentAvailable()} method will only
 * detect new content at the first call. Subsequent calls will always return false, since the last
 * welcome page version number is only stored when different from the current version number.
 * 
 * @author Bruno Gautier
 */
public final class DesignerContentDetector extends IntroContentDetector {

	private static final String WELCOME_VERSION = "1.0.7.201108051127"; //$NON-NLS-1$
	private static final String PROP_LAST_WELCOME_VERSION = "lastWelcomeVersion"; //$NON-NLS-1$

	@Override
	public boolean isNewContentAvailable() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		final String lastWelcomeVersion = store.getString(PROP_LAST_WELCOME_VERSION);

		if (null == lastWelcomeVersion || !lastWelcomeVersion.equals(WELCOME_VERSION)) {
			store.setValue(PROP_LAST_WELCOME_VERSION, WELCOME_VERSION);
			return true;
		}

		return false;
	}

}
