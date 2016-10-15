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
package com.nextep.designer.vcs.ui.validators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import com.nextep.designer.ui.dialogs.ComponentWizard;
import com.nextep.designer.ui.dialogs.ComponentWizardDialog;
import com.nextep.designer.ui.dialogs.TitleAreaDialogWrapper;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.IVersioningValidator;
import com.nextep.designer.vcs.model.VersioningOperation;
import com.nextep.designer.vcs.preferences.VersioningPreferenceConstants;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.dialogs.ConfirmCommitDialog;
import com.nextep.designer.vcs.ui.dialogs.VersionSettingsDialog;

/**
 * This validator handles check and user adjustments / confirmation on checkout and commits.
 * Depending on user settings, user will be prompt for adjusting version information on checkouts
 * and / or on commit and to validate committed elements.
 * 
 * @author Christophe Fondacci
 */
public class VersionSettingsValidator implements IVersioningValidator {

	@Override
	public boolean isActiveFor(IVersioningOperationContext context) {
		switch (context.getVersioningOperation()) {
		case CHECKOUT:
			return needsSettingsValidation(context);
		case COMMIT:
			return true;
		}
		return false;
	}

	@Override
	public IStatus validate(IVersioningOperationContext context) {
		List<IUIComponent> components = new ArrayList<IUIComponent>();
		// Adding settings validation dialog if needed
		if (needsSettingsValidation(context)) {
			components.add(new VersionSettingsDialog(context));
		}
		// Adding commit confirmation (on commit)
		if (context.getVersioningOperation() == VersioningOperation.COMMIT) {
			List<IVersionable<?>> elementsToConfirm = buildConfirmList(context.getVersionables());
			components
					.add(new ConfirmCommitDialog(context, elementsToConfirm, components.isEmpty()));
		}
		// Displaying
		Dialog dlg = null;
		if (components.size() == 1) {
			dlg = new TitleAreaDialogWrapper(UIHelper.getShell(), components.iterator().next(),
					SWT.RESIZE | SWT.TITLE | SWT.BORDER);
		} else if (components.size() > 1) {
			final IWizard wiz = new ComponentWizard(
					VCSUIMessages.getString("dialog.versionSettings.title"), components); //$NON-NLS-1$
			dlg = new ComponentWizardDialog(UIHelper.getShell(), wiz, true);
		} else {
			// Nothing to validate
			return Status.OK_STATUS;
		}
		dlg.setBlockOnOpen(true);
		dlg.open();
		return dlg.getReturnCode() == Window.OK ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

	private boolean needsSettingsValidation(IVersioningOperationContext context) {
		// Lookup at our current preferences to know whether we need validation
		final IEclipsePreferences prefs = new InstanceScope().getNode(VCSPlugin.PLUGIN_ID);
		final String validationNeededString = prefs.get(
				VersioningPreferenceConstants.VALIDATE_PREFIX
						+ context.getVersioningOperation().name(), "true"); //$NON-NLS-1$
		Boolean validationNeeded = Boolean.valueOf(validationNeededString);
		if (validationNeeded) {
			for (IVersionable<?> v : context.getVersionables()) {
				if (v.getContainer() == VCSPlugin.getViewService().getCurrentWorkspace()
						&& (v instanceof IVersionContainer)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Builds the list of elements to be confirmed for commit. It recursively integrates any child
	 * element of modules.
	 * 
	 * @param versionables initial elements to commit
	 * @return full list of elements that will be committed
	 */
	private List<IVersionable<?>> buildConfirmList(Collection<IVersionable<?>> versionables) {
		List<IVersionable<?>> elementsToCommit = new ArrayList<IVersionable<?>>();
		// Building list
		for (IVersionable<?> v : versionables) {
			// Filling the list of elements to commit
			if (v instanceof IVersionContainer) {
				elementsToCommit.addAll(getVersioningService().listCheckouts((IVersionContainer) v,
						true));
			}
			elementsToCommit.add(v);
		}
		return elementsToCommit;
	}

	public IVersioningService getVersioningService() {
		return VCSPlugin.getService(IVersioningService.class);
	}

}
