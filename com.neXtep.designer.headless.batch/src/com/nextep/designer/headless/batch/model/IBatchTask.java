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
package com.nextep.designer.headless.batch.model;

import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.headless.batch.services.IBatchTaskService;
import com.nextep.designer.headless.exceptions.BatchException;

/**
 * This interface describes an action executable through a batch process.
 * 
 * @author Christophe Fondacci
 */
public interface IBatchTask {

	/**
	 * Executes this action against the specified target connection. A map of properties is supplied
	 * to the action and can alter the execution behavior of this action.
	 * 
	 * @param targetConnection the {@link IConnection} against which the action should be executed
	 * @param argsMap the map of user-defined properties that may relate to action customization
	 * @param monitor a monitor to report progress to
	 * @return a {@link IStatus} indicating whether or not the action succeeds.
	 */
	IStatus execute(IConnection targetConnection, Map<String, String> propertiesMap,
			IProgressMonitor monitor) throws BatchException;

	/**
	 * Retrieves the unique identifier of this action
	 * 
	 * @return the action's unique identifier
	 */
	String getId();

	/**
	 * Defines the unique identifier of this action. Unique identifier will be injected by the batch
	 * action service as it will correspond to the unique identifier under which this class has been
	 * contributed.
	 * 
	 * @param id this action's unique ID
	 */
	void setId(String id);

	/**
	 * Retrieves the description of this batch action
	 * 
	 * @return this action's description
	 */
	String getDescription();

	/**
	 * Defines the description of this batch task
	 * 
	 * @param description the task's description
	 */
	void setDescription(String description);

	/**
	 * Returns the option groups used by this task.
	 * 
	 * @return the option groups used by this task
	 */
	List<String> getUsedOptionGroups();

	/**
	 * Defines the option groups used by this task
	 * 
	 * @param optionGroups the list of option groups used
	 */
	void setUsedOptionGroups(List<String> optionGroups);

	/**
	 * Injector of the {@link IBatchTaskService} that provides helper methods
	 * 
	 * @param batchTaskService the {@link IBatchTaskService} instance
	 */
	void setBatchTaskService(IBatchTaskService batchTaskService);

	/**
	 * Retrieves the {@link IBatchTaskService} instance
	 * 
	 * @return the current {@link IBatchTaskService} instance
	 */
	IBatchTaskService getBatchTaskService();
}
