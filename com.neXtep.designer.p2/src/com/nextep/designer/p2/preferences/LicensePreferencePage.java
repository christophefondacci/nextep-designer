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
package com.nextep.designer.p2.preferences;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.p2.P2Messages;
import com.nextep.designer.p2.P2Plugin;
import com.nextep.designer.p2.exceptions.UnavailableLicenseServerException;
import com.nextep.designer.p2.model.ILicenseInformation;
import com.nextep.designer.p2.services.ILicenseService;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class LicensePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	Button autoUpdateCheck;
	Label licenseKeyText;
	Label organizationText;
	Label validityText;
	Label updateSiteText;
	Text newLicenseKeyText;
	Label statusLabel;

	private boolean autoUpdate;

	public LicensePreferencePage() {
		setPreferenceStore(P2Plugin.getDefault().getPreferenceStore());

		// Initialize the default value of auto-update preference
		IPreferenceStore prefs = getPreferenceStore();
		prefs.setDefault(PreferenceConstants.P_AUTO_UPDATE_NEXTEP, true);
		if (prefs.contains(PreferenceConstants.P_AUTO_UPDATE_NEXTEP)) {
			autoUpdate = prefs.getBoolean(PreferenceConstants.P_AUTO_UPDATE_NEXTEP);
		} else {
			autoUpdate = prefs.getDefaultBoolean(PreferenceConstants.P_AUTO_UPDATE_NEXTEP);
		}
	}

	public LicensePreferencePage(String title) {
		super(title);
	}

	public LicensePreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));

		autoUpdateCheck = new Button(container, SWT.CHECK);
		autoUpdateCheck.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		autoUpdateCheck.setText(P2Messages.getString("preferences.autoUpdate.enable")); //$NON-NLS-1$
		autoUpdateCheck.setSelection(autoUpdate);
		autoUpdateCheck.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean wasSelected = autoUpdate;
				autoUpdate = autoUpdateCheck.getSelection();
				updateStore(wasSelected, autoUpdate);
			}

		});
		autoUpdateCheck.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent event) {
				autoUpdateCheck = null;
			}
		});

		Label introLbl = new Label(container, SWT.NONE);
		introLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		introLbl.setText(P2Messages.getString("preferences.license.currentLicenseInfoLabel")); //$NON-NLS-1$

		Label lineFiller = new Label(container, SWT.NONE);
		lineFiller.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		Label filler = new Label(container, SWT.NONE);
		filler.setText("     "); //$NON-NLS-1$

		Label licenseKey = new Label(container, SWT.RIGHT);
		licenseKey.setText(P2Messages.getString("preferences.license.licenseKey")); //$NON-NLS-1$
		licenseKeyText = new Label(container, SWT.BORDER);
		// licenseKeyText.setEditable(false);
		licenseKeyText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(container, SWT.NONE);
		Label organization = new Label(container, SWT.RIGHT);
		organization.setText(P2Messages.getString("preferences.license.organization")); //$NON-NLS-1$
		organizationText = new Label(container, SWT.BORDER);
		// organizationText.setEditable(false);
		organizationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(container, SWT.NONE);
		Label validity = new Label(container, SWT.RIGHT);
		validity.setText(P2Messages.getString("preferences.license.validUntil")); //$NON-NLS-1$
		validityText = new Label(container, SWT.BORDER);
		// validityText.setEditable(false);
		validityText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		new Label(container, SWT.NONE);
		Label updateSite = new Label(container, SWT.RIGHT);
		updateSite.setText(P2Messages.getString("preferences.license.updateSite")); //$NON-NLS-1$
		updateSiteText = new Label(container, SWT.BORDER);
		// updateSiteText.setEditable(false);
		updateSiteText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label lineFiller2 = new Label(container, SWT.NONE);
		lineFiller2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		Label registerLicenseLbl = new Label(container, SWT.NONE);
		registerLicenseLbl
				.setText(P2Messages.getString("preferences.license.contactInfo")); //$NON-NLS-1$
		registerLicenseLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 2));
		newLicenseKeyText = new Text(container, SWT.BORDER);
		newLicenseKeyText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		Button registerButton = new Button(container, SWT.PUSH);
		registerButton.setText(P2Messages.getString("preferences.license.registerButton")); //$NON-NLS-1$
		registerButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final String key = newLicenseKeyText.getText();
				Job j = new Job(P2Messages.getString("preferences.license.contactingServerJob")) { //$NON-NLS-1$

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask(P2Messages.getString("preferences.license.registerLicenseJob"), 2); //$NON-NLS-1$
						registerLicense(key, monitor);
						monitor.worked(1);
						refresh();
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				j.schedule();
			}
		});
		statusLabel = new Label(container, SWT.NONE);
		statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		statusLabel.setForeground(FontFactory.ERROR_COLOR);
		fireRefresh();

		return container;
	}

	private void updateStore(boolean wasSelected, boolean isSelected) {
		if (wasSelected ^ isSelected) {
			getPreferenceStore().setValue(PreferenceConstants.P_AUTO_UPDATE_NEXTEP, isSelected);
		}
	}

	private void registerLicense(String key, IProgressMonitor monitor) {
		monitor.beginTask(P2Messages.getString("preferences.license.serverUnavailable"), 100); //$NON-NLS-1$
		monitor.worked(20);
		try {
			final ILicenseInformation license = P2Plugin.getService(ILicenseService.class)
					.registerLicenseKey(key);
			monitor.worked(60);

			// Storing the property
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					statusLabel.setText(""); //$NON-NLS-1$
					MessageDialog.openInformation(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), P2Messages.getString("preferences.license.licenseRegisteredTitle"), //$NON-NLS-1$
							"You have been registered as member of " + license.getOrganization()
									+ ".\nThank you for supporting neXtep!");

				}
			});
			refresh();
		} catch (final Exception e) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					statusLabel.setText(e.getMessage());
				}
			});
		}

	}

	private void fireRefresh() {
		Job j = new Job(P2Messages.getString("preferences.license.serverUnavailable")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				refresh();
				return Status.OK_STATUS;
			}
		};
		j.schedule();
	}

	private void refresh() {
		ILicenseService licenseService = P2Plugin.getService(ILicenseService.class);
		ILicenseInformation license = null;
		try {
			license = licenseService.getCurrentLicense();
		} catch (UnavailableLicenseServerException e) {
			license = null;
		}
		final ILicenseInformation licenseInfo = license;
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				updateLicense(licenseInfo);
			}
		});

	}

	private void updateLicense(ILicenseInformation license) {
		if (license != null) {
			licenseKeyText.setText(license.getLicenseKey());
			organizationText.setText(license.getOrganization());
			validityText.setText(license.getExpirationDate().toString());
			updateSiteText.setText(license.getUpdateSiteLocation());
		} else {
			licenseKeyText.setText(P2Messages.getString("preferences.license.unavailable")); //$NON-NLS-1$
			organizationText.setText(P2Messages.getString("preferences.license.unavailable")); //$NON-NLS-1$
			validityText.setText(P2Messages.getString("preferences.license.unavailable")); //$NON-NLS-1$
			updateSiteText.setText(P2Messages.getString("preferences.license.unavailable")); //$NON-NLS-1$
		}
	}

}
