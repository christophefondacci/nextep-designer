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
package com.nextep.designer.sqlgen.oracle.impl;

import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.base.AbstractSqlScriptBuilder;

/**
 * A builder which builds {@link ISQLScript} from a {@link IGenerationResult} for the Oracle vendor.
 * This builder generates SQL script wrappers.
 * 
 * @author Christophe Fondacci
 */
public class OracleScriptBuilder extends AbstractSqlScriptBuilder {

	@Override
	protected ISQLScript createScript(String name, ScriptType type) {
		return new SQLWrapperScript(name, ""); //$NON-NLS-1$
	}

	@Override
	protected void appendScript(ISQLScript source, ISQLScript appendedScript) {
		if (source instanceof SQLWrapperScript) {
			((SQLWrapperScript) source).addChildScript(appendedScript);
		} else {
			source.appendScript(appendedScript);
		}
	}

	@Override
	protected ISQLScript createDataScript(String name, ScriptType type) {
		return new SQLScript(name, "", "", type); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void appendDropScript(ISQLScript source, ISQLScript appendedDropScript) {
		if (source instanceof SQLWrapperScript) {
			final ScriptType scriptType = appendedDropScript.getScriptType();
			try {
				appendedDropScript.setScriptType(ScriptType.DROP);
				((SQLWrapperScript) source).addChildScript(appendedDropScript);
			} finally {
				appendedDropScript.setScriptType(scriptType);
			}
		} else {
			source.appendScript(appendedDropScript);
		}
	}
}
