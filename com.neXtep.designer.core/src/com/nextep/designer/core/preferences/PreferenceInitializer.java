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
package com.nextep.designer.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;

/**
 * Class used to initialize default preference values.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IEclipsePreferences store = new DefaultScope().getNode(CorePlugin.PLUGIN_ID);
		store.putBoolean(DesignerCoreConstants.FORCE_DELETE, false);
		store.put(DesignerCoreConstants.REP_SSO_PROPERTY, "false"); //$NON-NLS-1$
		store.put(DesignerCoreConstants.REP_USER_PROPERTY, ""); //$NON-NLS-1$
		store.put(DesignerCoreConstants.REP_PASSWORD_SAVED_PROPERTY, "true"); //$NON-NLS-1$
		store.put(DesignerCoreConstants.REP_PASSWORD_PROPERTY, ""); //$NON-NLS-1$
		store.put(DesignerCoreConstants.REP_SERVER_PROPERTY, ""); //$NON-NLS-1$
		store.put(DesignerCoreConstants.REP_PORT_PROPERTY, ""); //$NON-NLS-1$
		store.put(DesignerCoreConstants.REP_DATABASE_PROPERTY, ""); //$NON-NLS-1$
		store.put(DesignerCoreConstants.REP_INSTANCE_PROPERTY, ""); //$NON-NLS-1$
		store.put(DesignerCoreConstants.REP_SCHEMA_PROPERTY, ""); //$NON-NLS-1$
		store.put(DesignerCoreConstants.REP_DB_VENDOR_PROPERTY, DBVendor.getDefaultVendor().name());
		store.put(DesignerCoreConstants.REP_TNS_PROPERTY, ""); //$NON-NLS-1$
		store.putBoolean(DesignerCoreConstants.ICON_TINY, false);
		try {
			store.flush();
		} catch (BackingStoreException e) {

		}
	}

}
