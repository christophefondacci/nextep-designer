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
package com.nextep.designer.p2.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.p2.P2Messages;
import com.nextep.designer.p2.P2Plugin;
import com.nextep.designer.p2.exceptions.UnavailableLicenseServerException;
import com.nextep.designer.p2.services.ILicenseService;

/**
 * UpdateHandler invokes the check for updates UI
 * 
 * @since 3.4
 */
public class UpdateHandler extends AbstractHandler {

	private static final Log LOGGER = LogFactory.getLog(UpdateHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ILicenseService licenseService = P2Plugin.getService(ILicenseService.class);
		try {
			final IProxyService proxyService = CorePlugin.getService(IProxyService.class);
			if (proxyService.isProxiesEnabled()) {
				proxyService.getProxyData();
			}
		} catch (RuntimeException e) {
			LOGGER.error("Unable to initialize proxy service : " + e.getMessage(), e);
		}

		try {
			licenseService.checkForUpdates(false);
		} catch (UnavailableLicenseServerException e) {
			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell(), P2Messages.getString("handler.update.serverUnavailableTitle"), //$NON-NLS-1$
					P2Messages.getString("handler.update.serverUnavailable") //$NON-NLS-1$
							+ e.getMessage());
		}
		return null;
	}
}
