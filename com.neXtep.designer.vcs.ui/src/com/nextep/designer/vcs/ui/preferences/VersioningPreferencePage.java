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
package com.nextep.designer.vcs.ui.preferences;

import java.text.MessageFormat;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.VersioningOperation;
import com.nextep.designer.vcs.preferences.VersioningPreferenceConstants;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIMessages;

public class VersioningPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public VersioningPreferencePage() {
		super(GRID);
		setTitle(VCSUIMessages.getString("preferences.versioning.title")); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromImage(VCSImages.ICON_VERSIONTREE));
		setDescription(VCSUIMessages.getString("preferences.versioning.description")); //$NON-NLS-1$
		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(), VCSPlugin.PLUGIN_ID));
	}

	@Override
	protected void createFieldEditors() {

		addField(new ComboFieldEditor(
				VersioningPreferenceConstants.DEFAULT_RELEASE_INCREMENT,
				VCSUIMessages.getString("preferences.versioning.defaultIncrement"), new String[][] { //$NON-NLS-1$

						{
								VCSUIMessages.getString("preferences.versioning.major"), String.valueOf(IVersionInfo.MAJOR) }, //$NON-NLS-1$
						{
								VCSUIMessages.getString("preferences.versioning.minor"), String.valueOf(IVersionInfo.MINOR) }, //$NON-NLS-1$
						{
								VCSUIMessages.getString("preferences.versioning.iteration"), String.valueOf(IVersionInfo.ITERATION) }, //$NON-NLS-1$
						{
								VCSUIMessages.getString("preferences.versioning.patch"), String.valueOf(IVersionInfo.PATCH) } }, getFieldEditorParent())); //$NON-NLS-1$
		VersioningOperation operation = VersioningOperation.CHECKOUT;
		addField(new ComboFieldEditor(
				VersioningPreferenceConstants.VALIDATE_PREFIX + operation,
				MessageFormat.format(
						VCSUIMessages.getString("preferences.versioning.validationLabel"), operation.name().toLowerCase().replace('_', ' ')), //$NON-NLS-1$
				new String[][] {
						{
								VCSUIMessages.getString("preferences.versioning.noValidationText"), "false" }, //$NON-NLS-1$ //$NON-NLS-2$
						{
								VCSUIMessages
										.getString("preferences.versioning.promptForValidationText"), "true" } }, //$NON-NLS-1$ //$NON-NLS-2$
				getFieldEditorParent()));

	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
