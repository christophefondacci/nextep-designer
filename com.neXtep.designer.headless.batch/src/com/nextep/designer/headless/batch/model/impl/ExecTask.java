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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.headless.batch.model.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.headless.batch.BatchMessages;
import com.nextep.designer.headless.batch.BatchPlugin;
import com.nextep.designer.headless.batch.helpers.TaskHelper;
import com.nextep.designer.headless.batch.model.BatchConstants;
import com.nextep.designer.headless.batch.model.IBatchTask;
import com.nextep.designer.headless.batch.model.base.AbstractBatchTask;
import com.nextep.designer.headless.exceptions.BatchException;

/**
 * This tasks executes the specified SQL script against a target connection.
 * 
 * @author Christophe Fondacci
 */
public class ExecTask extends AbstractBatchTask implements IBatchTask {

	@Override
	public IStatus execute(IConnection targetConnection, Map<String, String> propertiesMap,
			IProgressMonitor monitor) throws BatchException {
		final String scriptsStr = propertiesMap.get(BatchConstants.SCRIPT_LIST_ARG);
		// Checking argument
		if (scriptsStr == null || "".equals(scriptsStr.trim())) { //$NON-NLS-1$
			return new Status(IStatus.ERROR, BatchPlugin.PLUGIN_ID, MessageFormat.format(
					BatchMessages.getString("task.exec.noScriptError"), //$NON-NLS-1$
					BatchConstants.SCRIPT_LIST_ARG));
		}
		final String[] scripts = scriptsStr.split(","); //$NON-NLS-1$
		// Loading workspace
		TaskHelper.loadWorkspace(propertiesMap, monitor);

		// Retrieving the scripts
		final IReferenceManager referenceManager = CorePlugin.getService(IReferenceManager.class);
		final List<ISQLScript> scriptsList = new ArrayList<ISQLScript>(scripts.length);
		for (String script : scripts) {
			try {
				// Lookup for the specified SQL script
				final ISQLScript resolvedScript = (ISQLScript) referenceManager.findByTypeName(
						IElementType.getInstance(ISQLScript.TYPE_ID), script, true);
				// Processing scripts variables
				final ISQLScript computedScript = processScriptVariables(resolvedScript,
						propertiesMap, monitor);
				scriptsList.add(computedScript);
			} catch (ReferenceNotFoundException e) {
				throw new BatchException(MessageFormat.format(
						BatchMessages.getString("task.exec.scriptNotFound"), //$NON-NLS-1$
						script), e);
			}
		}

		// Now we can submit
		TaskHelper.submit(targetConnection, monitor,
				scriptsList.toArray(new ISQLScript[scriptsList.size()]));

		// Everything is OK
		return Status.OK_STATUS;
	}

	/**
	 * Processes the input SQL script to replace every occurrence of defined variable by their value
	 * and returns the resulting script.
	 * 
	 * @param template the SQL script template to substitute
	 * @param propertiesMap the key/value map of the command line arguments
	 * @param monitor monitor to report progress to
	 * @return the substituted script
	 */
	private ISQLScript processScriptVariables(ISQLScript template,
			Map<String, String> propertiesMap, IProgressMonitor monitor) {
		// Creating new script
		final ISQLScript script = CorePlugin.getTypedObjectFactory().create(ISQLScript.class);
		script.setName(template.getName());
		script.setScriptType(template.getScriptType());
		script.setSql(template.getSql());
		// Processing scripts variables
		for (String key : propertiesMap.keySet()) {
			if (key.startsWith(BatchConstants.SCRIPT_VARIABLES_PREFIX)) {
				final String var = key.substring(BatchConstants.SCRIPT_VARIABLES_PREFIX.length());
				final String velocityVar = "${" + var + "}"; //$NON-NLS-1$ //$NON-NLS-2$
				final String srcSql = script.getSql();
				if (srcSql.contains(velocityVar)) {
					final String value = propertiesMap.get(key);
					monitor.subTask(MessageFormat.format(
							BatchMessages.getString("task.exec.varSubstituted"), var, value)); //$NON-NLS-1$
					final String tgtSql = srcSql.replace(velocityVar, value);
					script.setSql(tgtSql);
				}
			}
		}
		return script;
	}

}
