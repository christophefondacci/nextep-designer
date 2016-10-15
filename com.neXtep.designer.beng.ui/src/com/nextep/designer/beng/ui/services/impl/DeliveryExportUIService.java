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

import java.io.File;
import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ExternalReferenceException;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.exception.UndeliverableIncrementException;
import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.IDeliveryExportService;
import com.nextep.designer.beng.ui.BengUIMessages;
import com.nextep.designer.beng.ui.services.IDeliveryExportUIService;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;
import com.nextep.designer.ui.helpers.BlockingJob;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Default implementation of the delivery export ui service
 * 
 * @author Christophe Fondacci
 */
public class DeliveryExportUIService implements IDeliveryExportUIService {

	private IDeliveryExportService exportService;
	private static final Log log = LogFactory.getLog(DeliveryExportUIService.class);

	@Override
	public void exportDelivery(final IDeliveryModule module) {
		// Checking container status
		try {
			IVersionContainer c = (IVersionContainer) VersionHelper.getReferencedItem(module
					.getModuleRef());
			IVersionable<IVersionContainer> v = VersionHelper.getVersionable(c);
			if (v.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
				boolean confirmed = MessageDialog.openQuestion(getShell(),
						BengUIMessages.getString("exportedModuleNotCheckedInTitle"), //$NON-NLS-1$
						BengUIMessages.getString("exportedModuleNotCheckedIn")); //$NON-NLS-1$
				if (!confirmed) {
					throw new CancelException("Export canceled by the user.");
				}
			}
		} catch (ExternalReferenceException e) {
			// ignoring for now
		}
		// Retrieving output folder
		String folder = SQLGenUtil.getPreference(PreferenceConstants.OUTPUT_FOLDER).trim();
		char lastChar = folder.charAt(folder.length() - 1);
		if (lastChar == File.separatorChar) {
			folder = folder.substring(0, folder.length() - 1);
		}
		// Generating outputs
		String exportLoc = folder + File.separator + module.getName();
		File f = new File(exportLoc);
		boolean newFolder = f.mkdir();
		if (!newFolder) {
			boolean confirmed = MessageDialog.openQuestion(getShell(),
					BengUIMessages.getString("exportLocationExistsTitle"), //$NON-NLS-1$
					BengUIMessages.getString("exportLocationExists")); //$NON-NLS-1$
			if (!confirmed) {
				throw new CancelException("Export canceled by the user.");
			}
		}
		final String targetDirectory = folder;
		// Generating module
		Job j = new BlockingJob("Exporting delivery " + module.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					exportService.exportDelivery(targetDirectory, module, monitor);
				} catch (UndeliverableIncrementException e) {
					final IDeliveryIncrement dlvInc = e.getDeliveryIncrement();
					// Error property label
					final String titleMsg = e.isUniversal() ? "universalDeliveryNotAllowedTitle"
							: "prerequisiteFailedTitle";
					final String errorMsg = e.isUniversal() ? "universalDeliveryNotAllowed"
							: "prerequisiteFailed";
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openWarning(PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(), BengUIMessages
									.getString(titleMsg), MessageFormat.format(BengUIMessages
									.getString(errorMsg), dlvInc.getModule().getName(), dlvInc
									.getFromRelease() == null ? "[Scratch]" : dlvInc
									.getFromRelease().getLabel(), dlvInc.getToRelease().getLabel()));
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		j.addJobChangeListener(new JobChangeAdapter() {

			public void done(IJobChangeEvent event) {
				if (event.getResult().isOK()) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog
									.openInformation(
											PlatformUI.getWorkbench().getActiveWorkbenchWindow()
													.getShell(),
											"Exported successfully", "Your delivery has been successfully exported to " + targetDirectory + File.separator + module.getName() + "."); //$NON-NLS-3$

						}
					});
				}
			};
		});
		j.setUser(true);
		j.schedule();

	}

	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	public void setDeliveryExportService(IDeliveryExportService exportService) {
		this.exportService = exportService;
	}
}
