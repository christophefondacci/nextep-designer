package com.nextep.designer.sqlgen.postgre.impl;

import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.model.base.AbstractSqlScriptBuilder;

public class PostGreScriptBuilder extends AbstractSqlScriptBuilder {

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
