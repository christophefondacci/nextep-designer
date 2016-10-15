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
package com.nextep.designer.beng.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.BengMessages;
import com.nextep.designer.beng.exception.UndeliverableIncrementException;
import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.model.impl.FileUtils;
import com.nextep.designer.beng.model.impl.ModuleDeliveryIncrement;
import com.nextep.designer.beng.services.IDeliveryExportService;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;

public class DeliveryExportService implements IDeliveryExportService {

	private static final Log log = LogFactory.getLog(DeliveryExportService.class);
	private IDeliveryService deliveryService;

	@Override
	public void exportDelivery(String directoryTarget, IDeliveryModule module, IProgressMonitor m)
			throws UndeliverableIncrementException {
		exportDelivery(directoryTarget, module, true, false, m);
	}

	private void exportDelivery(String directoryTarget, IDeliveryModule module,
			boolean exportInstaller, boolean universalExport, IProgressMonitor m)
			throws UndeliverableIncrementException {
		int workCount = 100;
		if (module.isUniversal()) {
			workCount += 200;
		}
		final SubMonitor monitor = SubMonitor
				.convert(
						m,
						MessageFormat.format(
								BengMessages.getString("service.deliveryExport.exportingTask"), module.getName()), workCount); //$NON-NLS-1$
		final String exportLoc = directoryTarget + File.separator + module.getName();

		// Retrieving dependencies from reference
		final List<IVersionInfo> dependencies = deliveryService.buildDependencies(
				new ArrayList<IVersionInfo>(), module);
		monitor.worked(20);
		module.generateArtefact(directoryTarget);
		monitor.worked(30);

		// Creating dependencies / requirements sub folders
		File depFolder = new File(exportLoc + File.separator + "dependencies"); //$NON-NLS-1$
		depFolder.mkdir();
		File prereqFolder = new File(exportLoc + File.separator + "requirements"); //$NON-NLS-1$
		prereqFolder.mkdir();

		// Processing dependencies export
		processDependencies(exportLoc, module, dependencies, universalExport, monitor.newChild(50));

		// Universal delivery
		// Adding a requirement to itself with all previous deliveries.
		// That makes a delivery from [Scratch] -> From Release of the current module
		if (module.isUniversal()) { // && !universalExport) {
			monitor.setWorkRemaining(200);
			try {
				IVersionContainer container = null;
				IReference ref = module.getModuleRef();
				try {
					container = (IVersionContainer) VersionHelper.getReferencedItem(ref);
				} catch (UnresolvedItemException e) {
					container = null;
				}
				// Get delivery chain from scratch
				List<IDeliveryInfo> dlvInfo = deliveryService
						.getDeliveries(new ModuleDeliveryIncrement(container, null, module
								.getFromRelease()));
				// Exporting everything
				final SubMonitor universalMonitor = SubMonitor
						.convert(
								monitor.newChild(200),
								BengMessages
										.getString("service.deliveryExport.universalGeneration"), dlvInfo.size() * 100); //$NON-NLS-1$
				for (IDeliveryInfo i : dlvInfo) {
					universalMonitor
							.subTask(MessageFormat.format(
									BengMessages
											.getString("service.deliveryExport.exportingReleaseTask"), i.getTargetRelease().getLabel())); //$NON-NLS-1$
					final IDeliveryModule previousModule = deliveryService.loadDelivery(i
							.getTargetRelease());
					universalMonitor.worked(60);
					exportDelivery(
							exportLoc + File.separator + "requirements", previousModule, false, true, //$NON-NLS-1$
							universalMonitor.newChild(40));
				}
			} catch (UndeliverableIncrementException e) {
				e.setUniversal(true);
				throw e;
			}
		}
		// Exporting jar
		if (exportInstaller) {
			monitor.subTask(BengMessages.getString("service.deliveryExport.exportingInstallerTask")); //$NON-NLS-1$
			exportInstaller(exportLoc);
		}
		monitor.done();
	}

	/**
	 * Generates the "dependencies" and "requirements" export of this delivery.
	 * 
	 * @param exportLoc root export location of the module contents
	 * @param module module which is being exported
	 * @param dependencies dependencies to process
	 * @param m the monitor to report progress to
	 * @throws UndeliverableIncrementException whenever one of the dependency could not be generated
	 */
	private void processDependencies(String exportLoc, IDeliveryModule module,
			List<IVersionInfo> dependencies, boolean universalExport, IProgressMonitor m)
			throws UndeliverableIncrementException {
		final SubMonitor monitor = SubMonitor
				.convert(
						m,
						MessageFormat.format(
								BengMessages
										.getString("service.deliveryExport.processingDependenciesTask"), module.getName()), dependencies.size() * 100); //$NON-NLS-1$

		// Processing dependent containers
		for (final IVersionInfo c : dependencies) {
			if (c.equals(module.getTargetRelease())) {
				monitor.worked(100);
				continue;
			}
			IDeliveryModule dependentModule = deliveryService.loadDelivery(c);
			monitor.worked(40);
			// We raise if we cannot resolve the dependency
			if (dependentModule == null) {
				throw new ErrorException(MessageFormat.format(BengMessages
						.getString("service.deliveryExport.missingDependentDeliveryException"), //$NON-NLS-1$
						c.getLabel()));
			}
			monitor.subTask(MessageFormat.format(
					BengMessages.getString("service.deliveryExport.exportingDependencyTask"), //$NON-NLS-1$
					dependentModule.getName()));
			if (dependentModule != module) {
				// Our module is not volatile if we want proper resolution during export
				// TODO Check this, but should stay commented
				// dependentModule.getModuleRef().setVolatile(false);
				dependentModule.generateArtefact(exportLoc + File.separator + "dependencies"); //$NON-NLS-1$
			}

			// Exporting requirements by first computing the increment we need
			IDeliveryIncrement inc = deliveryService.computeIncrement(module, c);
			// We compute the chain of deliveries that can install this increment
			List<IDeliveryInfo> dlvInfo = deliveryService.getDeliveries(inc);
			// We generate each of them
			for (IDeliveryInfo i : dlvInfo) {
				exportDelivery(
						exportLoc + File.separator + "requirements", //$NON-NLS-1$
						deliveryService.loadDelivery(i.getTargetRelease()), false, universalExport,
						monitor.newChild((int) (60 / dlvInfo.size())));
			}
		}
		monitor.done();
	}

	public void setDeliveryService(IDeliveryService deliveryService) {
		this.deliveryService = deliveryService;
	}

	/**
	 * Exports the installer jar to the export location
	 * 
	 * @param exportLoc directory where the installer jar should be generated
	 */
	private void exportInstaller(String exportLoc) {
		InputStream is = this.getClass().getResourceAsStream("/installer/neXtep.jar"); //$NON-NLS-1$
		if (is == null)
			return;
		File binaryTarget = new File(exportLoc + File.separator + "lib"); //$NON-NLS-1$
		binaryTarget.mkdir();
		File jarTarget = new File(exportLoc + File.separator + "lib" + File.separator //$NON-NLS-1$
				+ "neXtep.jar"); //$NON-NLS-1$
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(jarTarget);
			FileUtils.copyStreams(is, os);
			os.close();
			is.close();
			is = this.getClass().getResourceAsStream("/installer/neXShared.jar"); //$NON-NLS-1$
			jarTarget = new File(exportLoc + File.separator + "lib" + File.separator //$NON-NLS-1$
					+ "neXShared.jar"); //$NON-NLS-1$
			os = new FileOutputStream(jarTarget);
			FileUtils.copyStreams(is, os);
		} catch (IOException e) {
			log.warn(
					BengMessages.getString("service.deliveryExport.installerExportFailedWarning"), e); //$NON-NLS-1$
		} finally {
			FileUtils.closeStream(is);
			FileUtils.closeStream(os);
		}
	}

}
