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

import java.text.MessageFormat;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace
 * that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 */

public class GeneratorPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private LineDelimiterEditor lineDelimiterEditor;

	public GeneratorPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(), SQLGenPlugin.PLUGIN_ID));
		setDescription("NeXtep SQL generator preferences for capture / generate / synchronize\r\n\r\n");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
	 * manipulate various types of preferences. Each field editor knows how to save and restore
	 * itself.
	 */
	public void createFieldEditors() {
		// addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH,
		// "&Directory preference:", getFieldEditorParent()));
		// DB vendor preferences
		String[][] submitters = new String[2][2];
		submitters[0][0] = "neXtep JDBC-based client";
		submitters[0][1] = "JDBC";
		submitters[1][0] = "Vendor built-in binary";
		submitters[1][1] = "BUILTIN";

		addField(new ComboFieldEditor(PreferenceConstants.GENERATOR_METHOD,
				SQLGenMessages.getString("prefs.preferredGenerationMethod"), submitters,
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceConstants.SUBMITION_JDBC_NOWARN,
				SQLGenMessages.getString("prefs.nonPreferredGeneratorWarn"), getFieldEditorParent()));

		for (DBVendor v : DBVendor.values()) {
			if (v != DBVendor.JDBC) {
				addField(new FileFieldEditor(PreferenceConstants.GENERATOR_BINARY_PREFIX
						+ v.name().toLowerCase(), MessageFormat.format(
						SQLGenMessages.getString("prefs.vendorBinary"), v.toString()),
						getFieldEditorParent()));
			}
		}

		addField(new BooleanFieldEditor(PreferenceConstants.SYNCHRONIZE_WARN_EMPTY_GENERATION,
				SQLGenMessages.getString("prefs.warnEmpty"), getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.OUTPUT_FOLDER,
				SQLGenMessages.getString("prefs.generatorOutDir"), getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.TEMP_FOLDER,
				SQLGenMessages.getString("prefs.generatorTempDir"), getFieldEditorParent()));

		// Composite c = new Composite(getFieldEditorParent(), SWT.NONE);
		// c.setLayout(new GridLayout(2, false));
		Composite encodingParent = new Composite(getFieldEditorParent(), SWT.NONE);
		encodingParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		final GeneratorEncodingFieldEditor encodingEditor = new GeneratorEncodingFieldEditor(
				PreferenceConstants.SQL_SCRIPT_ENCODING, "",
				SQLGenMessages.getString("prefs.generator.sqlScriptEncoding"), encodingParent);
		addField(encodingEditor);
		lineDelimiterEditor = new LineDelimiterEditor(getFieldEditorParent(), getPreferenceStore());
		lineDelimiterEditor.doLoad();

		addField(new BooleanFieldEditor(PreferenceConstants.SQL_SCRIPT_NEWLINE_CONVERT,
				"Convert newlines in exported deliveries", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performOk() {
		lineDelimiterEditor.store();
		return super.performOk();
	}

}
