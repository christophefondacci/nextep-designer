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
package com.nextep.designer.headless.batch.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IMatchingStrategy;
import com.nextep.designer.headless.batch.model.IBatchOption;
import com.nextep.designer.headless.batch.model.IBatchTask;
import com.nextep.designer.headless.exceptions.BatchException;

/**
 * @author Christophe Fondacci
 */
public interface IBatchTaskService {

	/**
	 * Retrieves all registered tasks.
	 * 
	 * @return a collection of all known tasks
	 */
	Collection<IBatchTask> getAllTasks();

	/**
	 * Retrieves the task registered under the specified name.
	 * 
	 * @param actionId unique identifier of the task to look for
	 * @return the corresponding {@link IBatchTask}, if existing else <code>null</code>
	 */
	IBatchTask getTask(String actionId) throws BatchException;

	/**
	 * Retrieves the list of all available options
	 * 
	 * @return the list of all available options
	 */
	Collection<IBatchOption> getAvailableOptions();

	/**
	 * Retrieves all options registered under the specified group id
	 * 
	 * @param groupId identifier of the option group
	 * @return a collection of options registered for this group
	 */
	Collection<IBatchOption> getOptionsFor(String groupId);

	/**
	 * Retrieves the matching strategy to use
	 * 
	 * @param propertiesMap the map of command line properties
	 * @return a {@link IMatchingStrategy}
	 * @throws BatchException if the specified command line matching strategy is invalid
	 */
	IMatchingStrategy getMatchingStrategy(Map<String, String> propertiesMap) throws BatchException;

	/**
	 * Retrieves the list of deliveries that are referenced by the command line options.
	 * 
	 * @param propertiesMap the command line options map
	 * @return the list of {@link IDeliveryInfo} referenced by the command line arguments
	 * @throws BatchException
	 */
	List<IDeliveryInfo> getDeliveries(Map<String, String> propertiesMap) throws BatchException;

	/**
	 * <p>
	 * Retrieves the module name from the deliveries list. This operation is not that obvious since
	 * several names can be assigned to a single module. The list of deliveries is used to get the
	 * last delivery for the module, fetch the corresponding module instance, and return its name.
	 * </p>
	 * <p>
	 * For example, a module can be named :<br>
	 * - <code>MOD</code> in release 1.0.0.0<br>
	 * - <code>NEW_MOD</code> in release 1.0.1.0<br>
	 * - <code>LATEST_MOD</code> in release 1.0.2.0<br>
	 * <br>
	 * Calling this method with only release 1.0.0.0 as the delivery info will return
	 * <code>MOD</code> while calling this method with 1.0.0.0, 1.0.1.0 and 1.0.2.0 will return
	 * <code>LATEST_MOD</code>.
	 * </p>
	 * 
	 * @param deliveries the list of {@link IDeliveryInfo} from which the module name should be
	 *        extracted.
	 * @return the last known name of the module from the deliveries list
	 * @throws BatchException
	 */
	String getModuleNameFromDeliveries(List<IDeliveryInfo> deliveries) throws BatchException;
}
