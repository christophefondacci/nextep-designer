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

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	// Generator main properties
	public static final String OUTPUT_FOLDER = "com.nextep.designer.sqlgen.outputFolder";
	public static final String TEMP_FOLDER = "com.nextep.designer.sqlgen.tempFolder";
	public static final String GENERATOR_BINARY_PREFIX = "com.nextep.designer.sqlgen.generator.";
	public static final String SYNCHRONIZE_WARN_EMPTY_GENERATION = "com.nextep.designer.sqlgen.empty.warn";
	public static final String GENERATOR_METHOD = "com.nextep.designer.sqlgen.method";
	public static final String SQL_SCRIPT_ENCODING = "com.nextep.designer.sqlgen.script.charset";
	public static final String SQL_SCRIPT_NEWLINE = "com.nextep.designer.sqlgen.script.newline";
	public static final String SQL_SCRIPT_NEWLINE_CONVERT = "com.nextep.designer.sqlgen.script.newline.convert";
	// Drop strategies properties
	public static final String DROP_STRATEGY_PREFIX = "com.nextep.designer.sqlgen.dropStrategy.";

	// Synchronization properties
	public static final String SYNCHRONIZE_POLICY = "com.nextep.designer.sqlgen.synchPolicy";
	public static final String SYNCHRONIZE_POLICY_REPOSITORY = "repository";
	public static final String SYNCHRONIZE_POLICY_STRICT = "strict";
	public static final String SYNCHRONIZE_POLICY_FULL = "full";

	public static final String MATCH_PREFIX_WITH_CONTAINERS = "com.nextep.designer.sqlgen.matchPrefixWithContainers";
	public static final String SYNCHRONIZE_SILENTLY = "com.nextep.designer.sqlgen.synchSilently";
	public static final String DEFAULT_TARGET = "com.nextep.designer.sqlgen.defaultTarget";
	public static final String SUBMITION_JDBC_NOWARN = "com.nextep.sqlgen.ui.submition.jdbc.nowarn";
}
