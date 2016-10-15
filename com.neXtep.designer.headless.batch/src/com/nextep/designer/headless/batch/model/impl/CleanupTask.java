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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.nextep.datadesigner.sqlgen.model.IGenerationSubmitter;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.vcs.impl.MergeStrategy;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.headless.batch.BatchMessages;
import com.nextep.designer.headless.batch.helpers.Assert;
import com.nextep.designer.headless.batch.helpers.TaskHelper;
import com.nextep.designer.headless.batch.model.base.AbstractBatchTask;
import com.nextep.designer.headless.exceptions.BatchException;
import com.nextep.designer.headless.model.HeadlessConstants;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IComparisonManager;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This action cleans the target database by dropping every existing database
 * entity from it.
 * 
 * @author Christophe Fondacci
 */
public class CleanupTask extends AbstractBatchTask {

	private static final Log LOGGER = LogFactory.getLog(CleanupTask.class);

	@Override
	public IStatus execute(IConnection targetConnection, Map<String, String> propertiesMap,
			IProgressMonitor monitor) throws BatchException {
		final ICaptureService captureService = SQLGenPlugin.getService(ICaptureService.class);
		final IComparisonManager comparisonManager = VCSPlugin.getService(IComparisonManager.class);
		final IGenerationService generationService = SQLGenPlugin
				.getService(IGenerationService.class);

		Assert.notNull(targetConnection,
				BatchMessages.getString("task.cleanup.targetConnectionMissing")); //$NON-NLS-1$
		// Fetching database state
		final Collection<IVersionable<?>> capturedObjects = captureService.getContentsFromDatabase(
				targetConnection, monitor);

		// Comparing with void sources
		final Collection<IVersionable<?>> emptySources = Collections.emptyList();
		final List<IComparisonItem> compItems = comparisonManager.compare(emptySources,
				capturedObjects, MergeStrategy.create(ComparisonScope.DATABASE), true);

		// Generating everything
		final IGenerationResult generation = generationService.batchGenerate(monitor,
				targetConnection.getDBVendor(), "cleanup", "", compItems); //$NON-NLS-1$ //$NON-NLS-2$

		final List<ISQLScript> generationScripts = generation.buildScript();
		final ISQLScript flatScript = buildFlatScript(generationScripts);
		final boolean isVerbose = propertiesMap.containsKey(HeadlessConstants.VERBOSE);
		if (isVerbose) {
			LOGGER.info(BatchMessages.getString("task.cleanup.verboseLogScript") + flatScript.getSql()); //$NON-NLS-1$
		}

		monitor.subTask(MessageFormat.format(
				BatchMessages.getString("task.cleanup.submitting"), targetConnection)); //$NON-NLS-1$
		IGenerationSubmitter submitter = TaskHelper.getSQLSubmitter(targetConnection.getDBVendor(),
				monitor);
		submitter.setGenerationResult(generation);
		submitter.submit(monitor, flatScript, targetConnection);
		monitor.subTask(BatchMessages.getString("task.cleanup.ok")); //$NON-NLS-1$
		return Status.OK_STATUS;
	}

	private ISQLScript buildFlatScript(List<ISQLScript> scripts) {
		final ITypedObjectFactory objectFactory = CorePlugin.getTypedObjectFactory();
		final ISQLScript flatScript = objectFactory.create(ISQLScript.class);
		for (ISQLScript script : scripts) {
			flatScript.appendScript(script);
		}
		return flatScript;
	}

}
