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
package com.nextep.designer.synch.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.synch.ui.SynchUIPlugin;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace
 * that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 */

public class SynchronizationPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SynchronizationPreferencePage() {
		super(GRID);
		setPreferenceStore(SynchUIPlugin.getDefault().getPreferenceStore());
		setDescription(SynchUIMessages.getString("preferences.synch.desc")); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
	 * manipulate various types of preferences. Each field editor knows how to save and restore
	 * itself.
	 */
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.PROP_NOTIFY_SWITCH_PERSPECTIVE,
				SynchUIMessages.getString("preferences.synch.notifySwitch"), getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(
				PreferenceConstants.PROP_SELECT_DEPENDENCIES_WITHOUT_PROMPTING,
				SynchUIMessages.getString("preferences.synch.selectDependenciesWithoutPrompt"), getFieldEditorParent())); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
