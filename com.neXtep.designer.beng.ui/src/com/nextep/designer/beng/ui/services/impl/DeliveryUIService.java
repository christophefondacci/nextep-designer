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
package com.nextep.designer.beng.ui.services.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.beng.ui.BengUIMessages;
import com.nextep.designer.beng.ui.services.IDeliveryUIService;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.ui.helpers.BlockingJob;
import com.nextep.designer.ui.model.base.RunnableWithReturnedValue;
import com.nextep.designer.util.Assert;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

public class DeliveryUIService implements IDeliveryUIService {

	private IDeliveryService deliveryService;

	@Override
	public void build(final IDeliveryModule module) {
		Job j = new BlockingJob(BengUIMessages.getString("service.ui.delivery.building")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				SubMonitor m = SubMonitor.convert(monitor);
				m.beginTask(BengUIMessages.getString("service.ui.delivery.building"), 100); //$NON-NLS-1$
				try {
					List<IGenerationResult> results = getDeliveryService().build(module,
							m.newChild(70));
					m.setWorkRemaining(30);
					cleanupDelivery(module, results);
					getDeliveryService().addDeliveryGenerations(module, results, m.newChild(30));
					m.done();
					return Status.OK_STATUS;
				} catch (CancelException e) {
					return Status.CANCEL_STATUS;
				}
			}
		};
		j.setUser(true);
		j.schedule();
	}

	private void cleanupDelivery(final IDeliveryModule module, List<IGenerationResult> results) {
		// Checking that we can remove any pre-existing script
		// First checking scripts to remove
		final List<IDeliveryItem<?>> deliveriesToRemove = new ArrayList<IDeliveryItem<?>>();
		final StringBuffer scriptListBuf = new StringBuffer(100);
		for (IGenerationResult result : results) {
			Collection<ISQLScript> resultScripts = result.buildScript();
			if (resultScripts == null) {
				resultScripts = Collections.emptyList();
			}
			for (ISQLScript s : resultScripts) {
				// This loop is a safety check because the UI service should have cleaned everything
				final List<IDeliveryItem<?>> deliveries = module.getDeliveryItems();
				for (IDeliveryItem<?> item : deliveries) {
					if (item.getName().equals(s.getName())) {
						deliveriesToRemove.add(item);
						scriptListBuf.append("\n  - " + s.getName()); //$NON-NLS-1$
					}
				}
			}
		}

		// Should we need to remove any previous script from the delivery, we ask the user
		if (!deliveriesToRemove.isEmpty()) {
			RunnableWithReturnedValue<Boolean> runnable = new RunnableWithReturnedValue<Boolean>() {

				@Override
				public void run() {
					returnedValue = MessageDialog.openQuestion(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), BengUIMessages
							.getString("overwriteDeliveriesTitle"), MessageFormat.format( //$NON-NLS-1$
							BengUIMessages.getString("overwriteDeliveries"), scriptListBuf //$NON-NLS-1$
									.toString()));

					if (returnedValue) {
						for (IDeliveryItem<?> i : deliveriesToRemove) {
							module.removeDeliveryItem(i);
						}
					}
				}
			};
			Display.getDefault().syncExec(runnable);
			if (!runnable.returnedValue) {
				throw new CancelException(BengUIMessages.getString("service.ui.delivery.cancel")); //$NON-NLS-1$
			}

		}
	}

	@Override
	public void showArtefactComparison(final IDeliveryModule module) {
		Assert.notNull(module, BengUIMessages.getString("service.ui.delivery.nullArtefact")); //$NON-NLS-1$
		Job j = new Job(BengUIMessages.getString("service.ui.delivery.compareArtefactJob")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor m) {
				final SubMonitor monitor = SubMonitor.convert(m, 100);
				// We build the whole delivery comparison so that we got full scoped comparison
				final List<IComparisonItem> moduleItems = deliveryService.buildDeliveryModuleItems(
						module, monitor.newChild(60));
				monitor.setWorkRemaining(40);

				if (moduleItems != null) {
					VCSUIPlugin
							.getComparisonUIManager()
							.showComparison(
									MessageFormat.format(
											BengUIMessages
													.getString("service.ui.delivery.moduleComparisonTitle"), module.getName()), //$NON-NLS-1$
									moduleItems.toArray(new IComparisonItem[moduleItems.size()]));
				}
				monitor.worked(20);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.schedule();
	}

	public void setDeliveryService(IDeliveryService deliveryService) {
		this.deliveryService = deliveryService;
	}

	public IDeliveryService getDeliveryService() {
		return deliveryService;
	}
}
