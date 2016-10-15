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
package com.nextep.designer.synch.ui.listeners;

import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.sqlgen.ui.SQLScriptEditorInput;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.navigators.ComparisonNavigatorRoot;
import com.nextep.designer.synch.ui.perpectives.SynchronizationPerspective;
import com.nextep.designer.synch.ui.preferences.PreferenceConstants;
import com.nextep.designer.vcs.model.ComparisonScope;

public class SwitchPerspectiveListener implements ISynchronizationListener {

	public static final Log LOG = LogFactory.getLog(SwitchPerspectiveListener.class);

	@Override
	public void newSynchronization(ISynchronizationResult synchronizationResult) {
		try {
			// Checking if we should notify the user about a perspective change
			// 1. We check that we are not in SYNCH perspective already
			final IPerspectiveDescriptor perspectiveDesc = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().getPerspective();
			if (!perspectiveDesc.getId().equals(SynchronizationPerspective.PERSPECTIVE_ID)) {
				if (PlatformUI.getWorkbench() == null
						|| PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
					return;
				}
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				// 2. We check user has not yet said he does not want to be notified
				boolean notifyUser = SynchUIPlugin.getDefault().getPreferenceStore().getBoolean(
						PreferenceConstants.PROP_NOTIFY_SWITCH_PERSPECTIVE);
				if (notifyUser) {
					// 3. We notify, allowing user to disable notifications
					MessageDialogWithToggle
							.openInformation(
									shell,
									SynchUIMessages.getString("synch.perspective.switch.title"), SynchUIMessages //$NON-NLS-1$
											.getString("synch.perspective.switch"), SynchUIMessages //$NON-NLS-1$
											.getString("preferences.synch.notifySwitchToggle"), false, SynchUIPlugin //$NON-NLS-1$
											.getDefault().getPreferenceStore(),
									PreferenceConstants.PROP_NOTIFY_SWITCH_PERSPECTIVE);
				}
				// 4. Anyway, we show the perpective if it is not current
				PlatformUI.getWorkbench().showPerspective(
						SynchronizationPerspective.PERSPECTIVE_ID,
						PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			}

			// Here comes the real work : notiying the navigator root and opening script editor
			ComparisonNavigatorRoot.getInstance().newSynchronization(synchronizationResult);
			ISQLScript s = synchronizationResult.getGeneratedScript();
			if (s != null) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
						new SQLScriptEditorInput(s), SQLEditorUIServices.getEditorId(s));
			}
		} catch (WorkbenchException e) {
			LOG.error(MessageFormat.format(
					SynchUIMessages.getString("synch.perspective.openError"), e.getMessage()), e); //$NON-NLS-1$
		}
	}

	@Override
	public void scopeChanged(ComparisonScope scope) {
	}
}
