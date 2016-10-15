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
package com.nextep.designer.p2.services.impl;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.internal.p2.ui.dialogs.UpdateSingleIUWizard;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.p2.P2Messages;
import com.nextep.designer.p2.P2Plugin;
import com.nextep.designer.p2.exceptions.InvalidLicenseException;
import com.nextep.designer.p2.exceptions.UnavailableLicenseServerException;
import com.nextep.designer.p2.model.ILicenseInformation;
import com.nextep.designer.p2.model.impl.LicenseInformation;
import com.nextep.designer.p2.services.ILicenseService;

public class LicenseService implements ILicenseService {

	private final static String PROPERTY_LICENSE_KEY = "nextep.license"; //$NON-NLS-1$

	private final static Log log = LogFactory.getLog(LicenseService.class);
	private final static String DEFAULT_LICENSE_KEY = "NEXT-GPLv3-LICENSE"; //$NON-NLS-1$
	private final static String PERMISSION = "permission"; //$NON-NLS-1$
	private final static String PERMISSION_WARN = "warning"; //$NON-NLS-1$
	private final static String PERMISSION_ERROR = "error"; //$NON-NLS-1$
	private IRepositoryService repositoryService;

	@Override
	public ILicenseInformation registerLicenseKey(final String licenseKey)
			throws InvalidLicenseException, UnavailableLicenseServerException {
		final ILicenseInformation license = getLicenseFromServer(licenseKey);
		if (license == null) {
			throw new InvalidLicenseException(MessageFormat.format(
					P2Messages.getString("service.license.invalidLicense"), //$NON-NLS-1$
					licenseKey));
		} else {
			repositoryService.setProperty(PROPERTY_LICENSE_KEY, licenseKey);
		}
		return license;
	}

	@Override
	public URI getUpdateRepository() throws UnavailableLicenseServerException {
		try {
			final ILicenseInformation license = getCurrentLicense();
			// If none, we use the default license
			return new URI(license.getUpdateSiteLocation());
		} catch (URISyntaxException e) {
			throw new ErrorException(P2Messages.getString("service.license.invalidUpdateSite")); //$NON-NLS-1$
		}
	}

	@Override
	public ILicenseInformation getCurrentLicense() throws UnavailableLicenseServerException {
		// Trying to retrieve license key from repository
		String licenseKey = repositoryService.getProperty(PROPERTY_LICENSE_KEY);
		ILicenseInformation license = null;
		if (licenseKey == null || "".equals(licenseKey)) { //$NON-NLS-1$
			licenseKey = DEFAULT_LICENSE_KEY;
		}
		// Querying the license server to get the update site
		license = getLicenseFromServer(licenseKey);
		return license;
	}

	private ILicenseInformation getLicenseFromServer(String licenseKey)
			throws UnavailableLicenseServerException {
		String uri = buildRequest(licenseKey, false);
		ILicenseInformation licenseInfo = null;
		try {
			licenseInfo = getLicenseFromUrl(licenseKey, uri);
		} catch (UnavailableLicenseServerException e) {
			log.error("Unable to reach nextep server URL " + uri + ": " + e.getMessage(), e); //$NON-NLS-1$//$NON-NLS-2$
		} catch (RuntimeException e) {
			log.error("Unable to reach nextep server URL " + uri + ": " + e.getMessage(), e); //$NON-NLS-1$//$NON-NLS-2$
		}
		if (licenseInfo == null) {
			// Trying direct access (existing clients may have only authorized
			// this access on their proxy)
			uri = buildRequest(licenseKey, true);
			licenseInfo = getLicenseFromUrl(licenseKey, uri);
		}
		return licenseInfo;
	}

	private ILicenseInformation getLicenseFromUrl(String licenseKey, String uri)
			throws UnavailableLicenseServerException {

		// As we don't have CXF WebClient here for connect & parse, we use the
		// old doc builder way
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(uri);
			Element elt = doc.getDocumentElement();
			NodeList list = elt.getElementsByTagName("license"); //$NON-NLS-1$
			Element license = (Element) list.item(0);
			if (license != null) {
				NodeList urls = license.getElementsByTagName("updateUrl"); //$NON-NLS-1$
				Element updateUrlElt = (Element) urls.item(0);
				String updateUrl = updateUrlElt.getTextContent();

				NodeList organizations = license.getElementsByTagName("organization"); //$NON-NLS-1$
				String organization = ((Element) organizations.item(0)).getTextContent();

				NodeList contactEmails = license.getElementsByTagName("contactEmail"); //$NON-NLS-1$
				String contactEmail = "unknown";
				if (contactEmails != null && contactEmails.getLength() > 0) {
					contactEmail = ((Element) contactEmails.item(0)).getTextContent();
				}

				NodeList contactNames = license.getElementsByTagName("contactName"); //$NON-NLS-1$
				String contactName = null;
				if (contactNames != null && contactNames.getLength() > 0) {
					contactName = ((Element) contactNames.item(0)).getTextContent();
				}

				//				NodeList validFromNode = license.getElementsByTagName("validFrom"); //$NON-NLS-1$
				// long validFrom = Long.parseLong(((Element)
				// validFromNode.item(0)).getTextContent());
				// Date validFromDate = new Date(validFrom);

				NodeList validUntilNode = license.getElementsByTagName("validUntil"); //$NON-NLS-1$
				long validUntil = Long.parseLong(((Element) validUntilNode.item(0))
						.getTextContent());
				Date validUntilDate = new Date(validUntil);

				NodeList permissions = license.getElementsByTagName(PERMISSION);
				if (permissions != null && permissions.getLength() > 0) {
					String val = permissions.item(0).getTextContent();
					if (PERMISSION_WARN.equals(val)) {
						String msgKey = "preferences.license.licenseExpiredMsg"; //$NON-NLS-1$
						if (validUntil > System.currentTimeMillis()) {
							msgKey = "preferences.license.licenseQuotaMsg"; //$NON-NLS-1$
						}
						// Injecting contact email and name into warning message
						final String dlgMsg = MessageFormat.format(P2Messages.getString(msgKey),
								contactName, contactEmail);

						// Popping our dialog
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								MessageDialog.openWarning(null, P2Messages
										.getString("preferences.license.licenseExpiredTitle"), //$NON-NLS-1$
										dlgMsg);

							}
						});
					} else if (PERMISSION_ERROR.equals(val)) {
						String msgKey = "preferences.license.licenseExpiredError"; //$NON-NLS-1$
						if (validUntil > System.currentTimeMillis()) {
							msgKey = "preferences.license.licenseQuotaError"; //$NON-NLS-1$
						}
						// Injecting contact email and name into warning message
						final String dlgMsg = MessageFormat.format(P2Messages.getString(msgKey),
								contactName, contactEmail);
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								MessageDialog.openWarning(null, P2Messages
										.getString("preferences.license.licenseExpiredTitle"), //$NON-NLS-1$
										dlgMsg);
								System.exit(-1);
							}
						});
					}
				}
				LicenseInformation info = new LicenseInformation();
				info.setUpdateSiteLocation(updateUrl);
				info.setLicenseKey(licenseKey);
				info.setOrganization(organization);
				info.setExpirationDate(validUntilDate);
				return info;
			}
		} catch (RuntimeException e) {
			throw new UnavailableLicenseServerException(
					P2Messages.getString("service.license.unexpectedException") + e.getMessage(), e); //$NON-NLS-1$
		} catch (Exception e) {
			throw new UnavailableLicenseServerException(
					P2Messages.getString("service.license.serverProblemException") + e.getMessage(), e); //$NON-NLS-1$
		}
		throw new UnavailableLicenseServerException(MessageFormat.format(
				P2Messages.getString("service.license.unregisteredLicense"), //$NON-NLS-1$
				licenseKey));
	}

	/**
	 * Builds the request URI to the license server
	 * 
	 * @param licenseKey
	 *            license key to query
	 * @return the URI of the request to the license server
	 */
	private String buildRequest(String licenseKey, boolean direct) {
		final String version = P2Plugin.getDefault().getBundle().getVersion().toString();
		String ipAddress = ""; //$NON-NLS-1$
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {

		}

		// Preserving direct and indirect access to preserve compatibility for
		// pre 1.0.7 users
		// accessing license servers from direct calls who may have configured
		// their proxy
		// for this exact path. As we do need to secure our connection to our
		// license servers.
		final StringBuilder buf = new StringBuilder();
		if (direct) {
			buf.append("http://www.nextep-softwares.com:8282"); //$NON-NLS-1$
		} else {
			buf.append("http://license.nextep-softwares.com"); //$NON-NLS-1$
		}
		buf.append("/license/ws/licenseInfo/" + licenseKey + "/" //$NON-NLS-1$ //$NON-NLS-2$
				+ ipAddress + "/" + version //$NON-NLS-1$
		);
		if (!direct) {
			buf.append("-direct"); //$NON-NLS-1$
		}

		return buf.toString();
	}

	@Override
	public void checkForUpdates(final boolean isSilent) {
		final RepositoryTracker repoMan = getProvisioningUI().getRepositoryTracker();

		// Checking whether the update site is available
		Job j = new Job("Checking for updates...") {

			@Override
			protected IStatus run(org.eclipse.core.runtime.IProgressMonitor monitor) {
				if (repoMan.getKnownRepositories(getProvisioningUI().getSession()).length == 0) {
					if (!isSilent) {
						MessageDialog.openWarning(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								P2Messages.getString("service.license.serverUnavailableTitle"), //$NON-NLS-1$
								P2Messages.getString("service.license.serverUnavailableMsg")); //$NON-NLS-1$
					} else {
						log.info(P2Messages.getString("service.license.serverUnavailableSilent")); //$NON-NLS-1$
					}
					return Status.CANCEL_STATUS;
				}

				// We are clear for update check
				Job.getJobManager().cancel(LoadMetadataRepositoryJob.LOAD_FAMILY);
				final LoadMetadataRepositoryJob loadJob = new LoadMetadataRepositoryJob(
						getProvisioningUI());
				loadJob.setProperty(LoadMetadataRepositoryJob.ACCUMULATE_LOAD_ERRORS,
						Boolean.toString(true));

				loadJob.addJobChangeListener(new JobChangeAdapter() {

					@Override
					public void done(IJobChangeEvent event) {
						if (PlatformUI.isWorkbenchRunning())
							if (event.getResult().isOK()) {

								performUpdateCheck(loadJob, isSilent);
							}
					}
				});
				loadJob.setUser(!isSilent);
				loadJob.schedule();
				return Status.OK_STATUS;
			}
		};
		j.schedule();
	}

	private void performUpdateCheck(final LoadMetadataRepositoryJob job, final boolean isSilent) {
		// Retrieving update site
		try {
			final URI updateSiteLocation = P2Plugin.getService(ILicenseService.class)
					.getUpdateRepository();
			final UpdateOperation operation = getProvisioningUI().getUpdateOperation(null,
					new URI[] { updateSiteLocation });
			// check for updates
			operation.resolveModal(null);
			// If we are in silent mode, we only continue if we have any update
			if (isSilent) {
				final IStatus status = operation.getResolutionResult();
				if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE
						|| status.getSeverity() == IStatus.CANCEL) {
					return;
				}
			}
			Display.getDefault().syncExec(new Runnable() {

				@SuppressWarnings("restriction")
				@Override
				public void run() {
					// Getting shell
					final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell();
					if (getProvisioningUI().getPolicy().continueWorkingWithOperation(operation,
							shell)) {

						if (UpdateSingleIUWizard.validFor(operation)) {
							// Special case for only updating a single root
							UpdateSingleIUWizard wizard = new UpdateSingleIUWizard(
									getProvisioningUI(), operation);
							WizardDialog dialog = new WizardDialog(shell, wizard);
							dialog.create();
							dialog.open();
						} else {
							// Open the normal version of the update wizard
							getProvisioningUI().openUpdateWizard(false, operation, job);
						}

					}
				};
			});
		} catch (UnavailableLicenseServerException e) {
			log.error(P2Messages.getString("service.license.serverUnavailableWithReason") //$NON-NLS-1$
					+ e.getMessage(), e);
		}
	}

	protected ProvisioningUI getProvisioningUI() {
		return ProvisioningUI.getDefaultUI();
	}

	public void setRepositoryService(IRepositoryService service) {
		this.repositoryService = service;
	}
}
