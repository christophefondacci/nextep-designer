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
package com.nextep.designer.sqlgen.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ide.IDEEncoding;
import org.eclipse.ui.ide.dialogs.AbstractEncodingFieldEditor;
import org.eclipse.ui.ide.dialogs.EncodingFieldEditor;

/**
 * This class has been created to override the behavior of the
 * {@link AbstractEncodingFieldEditor#setPreferenceStore(IPreferenceStore)} method which does not
 * manage the case when the specified <code>IPreferenceStore</code> is null, which is the case when
 * the <code>FieldEditorPreferencePage</code> disposes of its resources. Most of the code of this
 * class has been duplicated from the <code>EncodingFieldEditor</code> class, which was not intended
 * to be subclassed.
 * 
 * @author Bruno Gautier
 * @see FieldEditorPreferencePage#dispose()
 * @see EncodingFieldEditor
 */
public final class GeneratorEncodingFieldEditor extends AbstractEncodingFieldEditor {

	public GeneratorEncodingFieldEditor(String name, String labelText, Composite parent) {
		super();
		init(name, labelText);
		createControl(parent);
	}

	public GeneratorEncodingFieldEditor(String name, String labelText, String groupTitle,
			Composite parent) {
		super();
		init(name, labelText);
		setGroupTitle(groupTitle);
		createControl(parent);
	}

	@Override
	protected String getStoredValue() {
		return getPreferenceStore().getString(getPreferenceName());
	}

	@Override
	protected void doStore() {
		String encoding = getSelectedEncoding();

		if (hasSameEncoding(encoding)) {
			return;
		}

		IDEEncoding.addIDEEncoding(encoding);

		if (encoding.equals(getDefaultEnc())) {
			getPreferenceStore().setToDefault(getPreferenceName());
		} else {
			getPreferenceStore().setValue(getPreferenceName(), encoding);
		}

		/*
		 * FIXME [BGA]: We should set here the value of the default Eclipse preference for the
		 * Editors encoding
		 */
		// ResourcesPlugin.getEncoding()Plugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_ENCODING,
		// encoding);
	}

	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		if (store != null) {
			super.setPreferenceStore(store);
		}
	}

}
