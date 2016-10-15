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
package com.nextep.datadesigner.sqlgen.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.TargetType;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;

/**
 * @author Christophe Fondacci
 */
public class SQLGenUtil extends Observable {

	private static final Log log = LogFactory.getLog(SQLGenUtil.class);
	/** Instance for observable-related operations */
	private static SQLGenUtil instance = null;
	private final List<BuildResult> buildResults;

	private SQLGenUtil() {
		buildResults = new ArrayList<BuildResult>();
	}

	public static SQLGenUtil getInstance() {
		if (instance == null)
			instance = new SQLGenUtil();
		return instance;
	}

	private static String getPreference(String key, IScopeContext[] contexts) {
		return Platform.getPreferencesService().getString(SQLGenPlugin.PLUGIN_ID, key, null,
				contexts);
	}

	public static String getPreference(String key) {
		return getPreference(key, new IScopeContext[] { new InstanceScope(), new DefaultScope() });
	}

	public static String getDefaultPreference(String key) {
		return getPreference(key, new IScopeContext[] { new DefaultScope() });
	}

	public static void setPreference(String key, String value) {
		try {
			final IEclipsePreferences pref = new InstanceScope().getNode(SQLGenPlugin.PLUGIN_ID);
			pref.put(key, value);
			pref.flush();
		} catch (BackingStoreException e) {
			log.error("Unable to save properties: " + e.getMessage(), e);
		}
	}

	// public static IPreferenceStore getPreferenceStore() {
	// return SQLGenPlugin.getDefault().getPreferenceStore();
	// }

	public static boolean getPreferenceBool(String key) {
		final String prefStr = getPreference(key);
		return prefStr == null ? false : Boolean.parseBoolean(prefStr);
	}

	/**
	 * @return the default target type for SQL generation / capture defined by
	 *         properties
	 */
	public static TargetType getDefaultTargetType() {
		try {
			return TargetType.valueOf(SQLGenUtil.getPreference(PreferenceConstants.DEFAULT_TARGET));
		} catch (IllegalArgumentException e) {
			return TargetType.DEVELOPMENT;
		}
	}

	public void addBuildResult(BuildResult r) {
		buildResults.add(r);
		notifyListeners(ChangeEvent.BUILD_ADDED, r);
	}

	public List<BuildResult> getBuildResults() {
		return buildResults;
	}

	/**
	 * Retrieves the tag which can invoke external SQL scripts from a SQL script
	 * 
	 * @deprecated please use {@link ISQLParser#getScriptCallerTag()} instead
	 * @param vendor
	 *            vendor to get the tag for
	 * @return the tag to invoke inner SQL scripts
	 */
	@Deprecated
	public static String getScriptCallerTag(DBVendor vendor) {
		ISQLParser parser = GeneratorFactory.getSQLParser(vendor);
		return parser.getScriptCallerTag();
	}

	/**
	 * @deprecated use {@link ISQLParser#getExitCommand()} instead.
	 */
	@Deprecated
	public static String getScriptExitTag() {
		// Retrieving preferred vendor from settings
		DBVendor vendor = DBGMHelper.getCurrentVendor(); // DBVendor.valueOf(SQLGenUtil.getPreference(PreferenceConstants.GENERATOR_VENDOR));
		switch (vendor) {
		case ORACLE:
			return "exit"; //$NON-NLS-1$
		case MYSQL:
			return "quit"; //$NON-NLS-1$
		case POSTGRE:
			return "\\q"; //$NON-NLS-1$
		default:
			throw new ErrorException("Unsupported vendor");
		}
	}

	// /**
	// * Helper method to append a statement delimiter followed by a new line
	// character to the last
	// * statement appended to the specified SQLScript. This method does not
	// check if the SQL script
	// * actually contains a statement to close.
	// *
	// * @param script a script with an unclosed statement at the end of the
	// script.
	// * @return the specified script with a statement delimiter appended at the
	// end.
	// */
	// public static ISQLScript closeLastStatement(ISQLScript script) {
	// return script.appendSQL(
	// SQLGenerationService.getInstance().getCurrentSQLParser().getStatementDelimiter())
	// .appendSQL(ISQLGenerator.NEWLINE);
	// }

	/**
	 * @return the generator's binary
	 */
	public static String getGeneratorBinary(DBVendor vendor) {
		final String binary = getPreference(PreferenceConstants.GENERATOR_BINARY_PREFIX
				+ vendor.name().toLowerCase());
		if (binary == null || "".equals(binary.trim())) {
			return DBGMHelper.getCurrentVendor().getDefaultExecutableName();
		}
		return binary;
	}

}
