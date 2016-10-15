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
package com.nextep.designer.sqlgen.model.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.ISqlScriptBuilder;

public abstract class AbstractSqlScriptBuilder implements ISqlScriptBuilder {

	private final static String NEWLINE = "\r\n"; //$NON-NLS-1$
	private final static String SCRIPT_NAME_DATA_DELETIONS = "data_deletions"; //$NON-NLS-1$
	private final static String SCRIPT_NAME_DATA_ADDITIONS = "data_updates"; //$NON-NLS-1$

	@Override
	public List<ISQLScript> buildScript(IGenerationResult result) {
		if (result.getAdditions().size() + result.getUpdates().size() + result.getDrops().size() == 0) {
			return Collections.emptyList();
		}

		// Initializing script list and creating main SQL script for DDL structural changes
		List<ISQLScript> scripts = new ArrayList<ISQLScript>();
		ISQLScript script = createScript(result.getName(), ScriptType.CUSTOM);
		scripts.add(script);

		List<ScriptType> revTypes = Arrays.asList(ScriptType.values());
		Collections.reverse(revTypes);
		List<ISQLScript> processed = new ArrayList<ISQLScript>();
		for (ScriptType t : revTypes) {
			for (ISQLScript s : result.getDrops()) {
				if (s.getScriptType() == t && !processed.contains(s)) {
					if (t != ScriptType.DATA && t != ScriptType.DATADEL) {
						s.setName(result.getName());
						// s.setScriptType(ScriptType.DROP);
						appendDropScript(script, s);
						processed.add(s);
					} else {
						s.setScriptType(ScriptType.DATADEL);
						s.setName(getVendorName(s.getName(), result.getVendor()));
						scripts.add(s);
					}
				}
			}
		}
		// Now we can generate everything but the data
		for (ScriptType type : ScriptType.values()) {
			// Data scripts are treated specifically
			if (type != ScriptType.DATA) {
				// Appending scripts of this type from generation result to target script
				appendTypedScripts(script, result, type);
			} else {
				for (ISQLScript s : result.getAdditions()) {
					if (s.getScriptType() == type) {
						s.setName(getVendorName(s.getName(), result.getVendor()));
						scripts.add(s);
					}
				}
				for (ISQLScript s : result.getUpdates()) {
					if (s.getScriptType() == type) {
						s.setName(getVendorName(s.getName(), result.getVendor()));
						scripts.add(s);
					}
				}
			}
		}
		script.appendSQL(NEWLINE);

		return scripts;
	}

	/**
	 * A helper method with prepends the vendor name prefix (if available) to a script name
	 * 
	 * @param name name of the script to tag
	 * @param vendor the {@link DBVendor} to tag with
	 * @return the prepended script name
	 */
	private String getVendorName(String name, DBVendor vendor) {
		if (vendor == null) {
			return name;
		} else if (name.toUpperCase().startsWith(vendor.name().toUpperCase())) {
			return name;
		} else {
			return vendor.name() + "." + name;
		}
	}

	/**
	 * Appends all scripts of the specified type from the generation result to the specified target
	 * script.
	 * 
	 * @param s target script to append elements to
	 * @param result generation result which contains source script to add
	 * @param type type to filter, only scripts of this type will be appended
	 * @retrun <code>true</code> if at least one script has been appended, else <code>false</code>
	 */
	private boolean appendTypedScripts(ISQLScript script, IGenerationResult result, ScriptType type) {
		boolean scriptFound = false;
		for (ISQLScript s : result.getAdditions()) {
			if (s.getScriptType() == type) {
				scriptFound = true;
				appendScript(script, s);
			}
		}
		for (ISQLScript s : result.getUpdates()) {
			if (s.getScriptType() == type) {
				scriptFound = true;
				appendScript(script, s);
			}
		}
		return scriptFound;
	}

	/**
	 * Instantiates the appropriate {@link ISQLScript} implementation
	 * 
	 * @param name name of the {@link ISQLScript} to build
	 * @param type type of the script to build
	 * @return a {@link ISQLScript} implementation
	 */
	protected abstract ISQLScript createScript(String name, ScriptType type);

	/**
	 * Instantiates the appropriate {@link ISQLScript} implementation for a data script. Data script
	 * may differ from other scripts so they get a dedicated builder.
	 * 
	 * @param name name of the {@link ISQLScript} to build
	 * @param type type of the script to build
	 * @return a {@link ISQLScript} implementation
	 */
	protected abstract ISQLScript createDataScript(String name, ScriptType type);

	/**
	 * Appends the specified script to the source script.
	 * 
	 * @param source source script to which the other script will be appended
	 * @param appendedScript the {@link ISQLScript} to append
	 */
	protected abstract void appendScript(ISQLScript source, ISQLScript appendedScript);

	/**
	 * Appends the specified script as a drop script to the source script.
	 * 
	 * @param source source script to which the other script will be appended
	 * @param appendedDropScript the {@link ISQLScript} to append as a drop script
	 */
	protected abstract void appendDropScript(ISQLScript source, ISQLScript appendedDropScript);
}
