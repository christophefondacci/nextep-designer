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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.synch.ui.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.preferences.PreferenceConstants;
import com.nextep.designer.synch.ui.SynchUIMessages;

/**
 * @author Christophe Fondacci
 */
public class SynchronizationDbgmPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SynchronizationDbgmPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(), DbgmPlugin.PLUGIN_ID));
		setDescription(SynchUIMessages.getString("preferences.synch.dbgm.desc")); //$NON-NLS-1$
	}

	@Override
	protected void createFieldEditors() {

		addField(new BooleanFieldEditor(PreferenceConstants.COMPARE_PHYSICALS,
				SynchUIMessages.getString("preferences.synch.dbgm.synchPhysicals"), getFieldEditorParent())); //$NON-NLS-1$
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
