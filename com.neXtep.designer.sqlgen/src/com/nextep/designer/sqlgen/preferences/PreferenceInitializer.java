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
package com.nextep.designer.sqlgen.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.TargetType;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IEclipsePreferences prefs = new DefaultScope().getNode(SQLGenPlugin.PLUGIN_ID);
		for (DBVendor v : DBVendor.values()) {
			prefs.put(PreferenceConstants.GENERATOR_BINARY_PREFIX + v.name().toLowerCase(), ""); //$NON-NLS-1$
		}
		prefs.putBoolean(PreferenceConstants.MATCH_PREFIX_WITH_CONTAINERS, false);
		prefs.put(PreferenceConstants.SYNCHRONIZE_POLICY, "repository"); //$NON-NLS-1$
		prefs.putBoolean(PreferenceConstants.SYNCHRONIZE_WARN_EMPTY_GENERATION, true);
		prefs.put(PreferenceConstants.OUTPUT_FOLDER, Platform.getLocation().toOSString()); // InstanceLocation().getURL().getPath());
		prefs.put(PreferenceConstants.DEFAULT_TARGET, TargetType.DEVELOPMENT.name());
		prefs.put(PreferenceConstants.TEMP_FOLDER, Platform.getLocation().toOSString()); // Platform.getInstanceLocation().getURL().getPath());
		prefs.putBoolean(PreferenceConstants.SYNCHRONIZE_SILENTLY, false);
		prefs.put(PreferenceConstants.GENERATOR_METHOD, "BUILTIN"); //$NON-NLS-1$

		// We set the default encoding value to UTF-8 to avoid cross-platform problems.
		prefs.put(PreferenceConstants.SQL_SCRIPT_ENCODING, "UTF-8"); //$NON-NLS-1$
		prefs.put(PreferenceConstants.SQL_SCRIPT_NEWLINE, "\r\n"); //$NON-NLS-1$
		prefs.putBoolean(PreferenceConstants.SQL_SCRIPT_NEWLINE_CONVERT, false); //$NON-NLS-1$

		for (IElementType t : GeneratorFactory.getGeneratedTypes()) {
			final IDropStrategy defaultStrategy = SQLGenPlugin.getService(IGenerationService.class)
					.getDefaultDropStrategy(t);
			prefs.put(PreferenceConstants.DROP_STRATEGY_PREFIX + t.getId().toLowerCase(),
					defaultStrategy.getId());
		}
	}
}
