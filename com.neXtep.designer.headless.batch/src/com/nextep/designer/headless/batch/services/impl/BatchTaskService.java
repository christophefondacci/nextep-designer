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
package com.nextep.designer.headless.batch.services.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.designer.beng.exception.InvalidStrategyException;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IMatchingStrategy;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.dao.IIdentifiableDAO;
import com.nextep.designer.headless.batch.BatchMessages;
import com.nextep.designer.headless.batch.exceptions.ActionNotFoundException;
import com.nextep.designer.headless.batch.model.BatchConstants;
import com.nextep.designer.headless.batch.model.IBatchOption;
import com.nextep.designer.headless.batch.model.IBatchTask;
import com.nextep.designer.headless.batch.model.impl.BatchOption;
import com.nextep.designer.headless.batch.services.IBatchTaskService;
import com.nextep.designer.headless.exceptions.BatchException;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * @author Christophe Fondacci
 */
public class BatchTaskService implements IBatchTaskService {

	private final static String DEFAULT_STRATEGY_ID = "DEFAULT"; //$NON-NLS-1$
	private static final Log LOGGER = LogFactory.getLog(BatchTaskService.class);
	private static final String TASK_EXTENSION_ID = "com.neXtep.designer.headless.batch.batchTask"; //$NON-NLS-1$
	private static final String ATTR_TASK_ID = "taskId"; //$NON-NLS-1$
	private static final String ATTR_TASK_CLASS = "taskClass"; //$NON-NLS-1$
	private static final String ATTR_TASK_DESC = "description"; //$NON-NLS-1$
	private static final String ATTR_TASK_OPTIONS = "optionGroups"; //$NON-NLS-1$

	private static final String OPTION_EXTENSION_ID = "com.neXtep.designer.headless.batch.batchOption"; //$NON-NLS-1$
	private static final String ATTR_OPTION_CODE = "code"; //$NON-NLS-1$
	private static final String ATTR_OPTION_GROUP = "groupId"; //$NON-NLS-1$
	private static final String ATTR_OPTION_DESC = "description"; //$NON-NLS-1$

	private Map<String, List<IBatchOption>> optionsMap = new HashMap<String, List<IBatchOption>>();
	private IDeliveryService deliveryService;

	public BatchTaskService() {
		// Initializing options map from extension registry
		final Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
				OPTION_EXTENSION_ID, ATTR_OPTION_CODE, "*"); //$NON-NLS-1$
		for (IConfigurationElement elt : elts) {
			// Building option
			IBatchOption option = new BatchOption();
			option.setName(elt.getAttribute(ATTR_OPTION_CODE));
			option.setDescription(elt.getAttribute(ATTR_OPTION_DESC));

			// Registering it in the appropriate option group
			final String groupId = elt.getAttribute(ATTR_OPTION_GROUP);
			List<IBatchOption> options = optionsMap.get(groupId);
			if (options == null) {
				options = new ArrayList<IBatchOption>();
				optionsMap.put(groupId, options);
			}
			options.add(option);
		}
	}

	@Override
	public Collection<IBatchTask> getAllTasks() {
		final Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
				TASK_EXTENSION_ID, ATTR_TASK_ID, "*"); //$NON-NLS-1$
		final List<IBatchTask> actions = new ArrayList<IBatchTask>();
		for (IConfigurationElement elt : elts) {
			try {
				final IBatchTask action = buildAction(elt);
				actions.add(action);
			} catch (BatchException e) {
				LOGGER.error(
						MessageFormat.format(
								BatchMessages
										.getString("service.batchActions.instantiationException"), e.getMessage()), e); //$NON-NLS-1$
			}
		}
		return actions;
	}

	@Override
	public IBatchTask getTask(String actionId) throws BatchException {
		// Retrieving requested action extension
		final IConfigurationElement elt = Designer.getInstance().getExtension(TASK_EXTENSION_ID,
				ATTR_TASK_ID, actionId);
		// Do we have an action ?
		if (elt == null) {
			throw new ActionNotFoundException(actionId);
		}
		return buildAction(elt);
	}

	private IBatchTask buildAction(IConfigurationElement elt) throws BatchException {
		try {
			// Instantiating action class
			final IBatchTask action = (IBatchTask) elt.createExecutableExtension(ATTR_TASK_CLASS);
			final String actionId = elt.getAttribute(ATTR_TASK_ID);
			final String description = elt.getAttribute(ATTR_TASK_DESC);
			final String options = elt.getAttribute(ATTR_TASK_OPTIONS);
			action.setId(actionId);
			action.setDescription(description);
			if (options != null) {
				String[] opts = options.split(","); //$NON-NLS-1$
				action.setUsedOptionGroups(Arrays.asList(opts));
			}
			action.setBatchTaskService(this);
			return action;
		} catch (CoreException e) {
			throw new BatchException(MessageFormat.format(BatchMessages
					.getString("service.batchActions.instantiationException"), e.getMessage()), e); //$NON-NLS-1$
		}
	}

	@Override
	public Collection<IBatchOption> getAvailableOptions() {
		final List<IBatchOption> allOptions = new ArrayList<IBatchOption>();
		for (List<IBatchOption> options : optionsMap.values()) {
			allOptions.addAll(options);
		}
		return allOptions;
	}

	@Override
	public Collection<IBatchOption> getOptionsFor(String groupId) {
		final List<IBatchOption> options = optionsMap.get(groupId);
		if (options != null) {
			return options;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public IMatchingStrategy getMatchingStrategy(Map<String, String> propertiesMap)
			throws BatchException {
		// Configuring matching strategy from arguments
		final String matching = propertiesMap.get(BatchConstants.MATCHING_ARG);
		final String strategyId = matching == null ? DEFAULT_STRATEGY_ID : matching;
		try {
			return deliveryService.getMatchingStrategy(strategyId);
		} catch (InvalidStrategyException e) {
			// Because we never know
			throw new BatchException(
					MessageFormat
							.format(BatchMessages
									.getString("service.batchActions.invalidMatchingStrategy"), strategyId), e); //$NON-NLS-1$
		}
	}

	@Override
	public List<IDeliveryInfo> getDeliveries(Map<String, String> propertiesMap)
			throws BatchException {
		final IMatchingStrategy strategy = getMatchingStrategy(propertiesMap);
		final String moduleRef = propertiesMap.get(BatchConstants.MODULE_REF_ARG);
		if (moduleRef != null && !"".equals(moduleRef)) { //$NON-NLS-1$
			final String[] moduleArray = moduleRef.split(":"); //$NON-NLS-1$
			final String module = moduleArray[0];
			final String versionPattern = moduleArray.length <= 1 ? null : moduleArray[1];
			try {
				return deliveryService.getDeliveriesWithReference(module, versionPattern, strategy);
			} catch (ReferenceNotFoundException e) {
				throw new BatchException(
						MessageFormat
								.format(BatchMessages
										.getString("service.batchActions.unresolvedModuleRef"), module, e.getMessage()), e); //$NON-NLS-1$
			}
		} else {
			final String moduleVersion = propertiesMap.get(BatchConstants.MODULE_ARG);
			final String[] moduleArray = moduleVersion.split(":"); //$NON-NLS-1$
			final String module = moduleArray[0];
			final String versionPattern = moduleArray.length <= 1 ? null : moduleArray[1];
			return deliveryService.getDeliveries(module, versionPattern, strategy);

		}
	}

	@Override
	public String getModuleNameFromDeliveries(List<IDeliveryInfo> deliveries) throws BatchException {
		// Empty check, because the following assumes non empty list
		if (deliveries.isEmpty()) {
			return null;
		}
		final IDeliveryInfo lastDelivery = deliveries.get(deliveries.size() - 1);
		final IVersionInfo lastRelease = lastDelivery.getTargetRelease();

		final IIdentifiableDAO dao = CorePlugin.getIdentifiableDao();
		ContainerInfo module = (ContainerInfo) dao.load(ContainerInfo.class, lastRelease.getUID());
		return module.getName();
	}

	/**
	 * @param deliveryService the deliveryService to set
	 */
	public void setDeliveryService(IDeliveryService deliveryService) {
		this.deliveryService = deliveryService;
	}
}
