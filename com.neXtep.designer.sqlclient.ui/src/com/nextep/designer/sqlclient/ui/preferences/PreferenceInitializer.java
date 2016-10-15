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
package com.nextep.designer.sqlclient.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;

/**
 * Class used to initialize default preference values.
 * 
 * @author Christophe Fondacci
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = SQLClientPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_BOOLEAN, true);
		store.setDefault(PreferenceConstants.P_CHOICE, "choice2"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.P_STRING, "Default value"); //$NON-NLS-1$

		store.setDefault(PreferenceConstants.EXPORT_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EXPORT_DECIMAL_SEPARATOR, "."); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EXPORT_ENCLOSER, "\""); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EXPORT_ESCAPER, ""); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EXPORT_NULL_VALUE, ""); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.EXPORT_SEPARATOR, ","); //$NON-NLS-1$
	}

}
