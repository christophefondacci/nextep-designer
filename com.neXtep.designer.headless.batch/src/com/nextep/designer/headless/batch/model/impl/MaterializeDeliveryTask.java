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
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.dao.IDeliveryDao;
import com.nextep.designer.beng.exception.UndeliverableIncrementException;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.IDeliveryExportService;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.headless.batch.BatchMessages;
import com.nextep.designer.headless.batch.BatchPlugin;
import com.nextep.designer.headless.batch.model.BatchConstants;
import com.nextep.designer.headless.batch.model.IBatchTask;
import com.nextep.designer.headless.batch.model.base.AbstractBatchTask;
import com.nextep.designer.headless.exceptions.BatchException;

/**
 * @author Christophe Fondacci
 */
public class MaterializeDeliveryTask extends AbstractBatchTask implements IBatchTask {

	// private static final Log LOGGER = LogFactory.getLog(MaterializeDeliveryTask.class);

	@Override
	public IStatus execute(IConnection targetConnection, Map<String, String> propertiesMap,
			IProgressMonitor monitor) throws BatchException {
		final IDeliveryExportService exportService = BengPlugin
				.getService(IDeliveryExportService.class);
		final IDeliveryDao deliveryDao = BengPlugin.getService(IDeliveryDao.class);

		final String exportDir = propertiesMap.get(BatchConstants.DELIVERY_EXPORT_DIR);
		// We need an export directory
		if (exportDir == null) {
			return new Status(IStatus.ERROR, BatchPlugin.PLUGIN_ID, MessageFormat.format(
					BatchMessages.getString("task.materialize.noExportDirMsg"), //$NON-NLS-1$
					BatchConstants.DELIVERY_EXPORT_DIR));
		}
		monitor.setTaskName(BatchMessages.getString("task.materialize.fetchingDeliveries")); //$NON-NLS-1$
		final List<IDeliveryInfo> deliveries = getBatchTaskService().getDeliveries(propertiesMap);
		final String module = getBatchTaskService().getModuleNameFromDeliveries(deliveries);
		monitor.beginTask(
				MessageFormat.format(
						BatchMessages.getString("task.materialize.deliveriesFound"), deliveries.size(), module, //$NON-NLS-1$
						exportDir), deliveries.size());
		for (IDeliveryInfo dlvInfo : deliveries) {
			monitor.subTask(MessageFormat.format(
					BatchMessages.getString("task.materialize.exporting"), dlvInfo.getName())); //$NON-NLS-1$
			final IDeliveryModule delivery = deliveryDao.loadModule(dlvInfo);
			try {
				exportService.exportDelivery(exportDir, delivery, monitor);
			} catch (UndeliverableIncrementException e) {
				return new Status(IStatus.ERROR, BatchPlugin.PLUGIN_ID, e.getMessage(), e);
			} catch (Exception e) {
				return new Status(IStatus.ERROR, BatchPlugin.PLUGIN_ID, e.getMessage(), e);
			}
			// Freeing memory
			HibernateUtil.getInstance().clearAllSessions();
		}
		return Status.OK_STATUS;
	}

}
