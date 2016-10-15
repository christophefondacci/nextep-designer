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
package com.nextep.designer.sqlclient.ui.preferences;

import java.text.SimpleDateFormat;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace
 * that allows us to create a page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 */

public class SQLClientPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public SQLClientPreferencePage() {
		super(GRID);
		setPreferenceStore(SQLClientPlugin.getDefault().getPreferenceStore());
		setDescription("SQL Client preference customization");
	}

	private class DateFormatFieldEditor extends StringFieldEditor {

		public DateFormatFieldEditor(String pref, String label, Composite parent) {
			super(pref, label, parent);
			setValidateStrategy(VALIDATE_ON_KEY_STROKE);
		}

		@Override
		protected boolean doCheckState() {
			final String pattern = getTextControl().getText();
			try {
				new SimpleDateFormat(pattern);
				return true;
			} catch (IllegalArgumentException e) {
				setErrorMessage("Date format pattern is not valid: " + e.getMessage());
				return false;
			}
		}
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
	 * manipulate various types of preferences. Each field editor knows how to save and restore
	 * itself.
	 */
	public void createFieldEditors() {
		// addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, "&Directory preference:",
		// getFieldEditorParent()));
		// addField(new BooleanFieldEditor(PreferenceConstants.P_BOOLEAN,
		// "&An example of a boolean preference", getFieldEditorParent()));
		//
		// addField(new RadioGroupFieldEditor(PreferenceConstants.P_CHOICE,
		// "An example of a multiple-choice preference", 1, new String[][] {
		// { "&Choice 1", "choice1" }, { "C&hoice 2", "choice2" } },
		// getFieldEditorParent()));
		// addField(new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:",
		// getFieldEditorParent()));
		addField(new DateFormatFieldEditor(PreferenceConstants.EXPORT_DATE_FORMAT,
				"Date format : ", getFieldEditorParent()));
		final StringFieldEditor decimalEditor = new StringFieldEditor(
				PreferenceConstants.EXPORT_DECIMAL_SEPARATOR, "Decimal separator : ",
				getFieldEditorParent());
		decimalEditor.setTextLimit(1);
		addField(decimalEditor);
		final StringFieldEditor enclosedEditor = new StringFieldEditor(
				PreferenceConstants.EXPORT_ENCLOSER, "Field enclosed by : ", getFieldEditorParent());
		enclosedEditor.setTextLimit(1);
		addField(enclosedEditor);
		final StringFieldEditor escapeEditor = new StringFieldEditor(
				PreferenceConstants.EXPORT_ESCAPER, "Escape character:", getFieldEditorParent());
		escapeEditor.setTextLimit(1);
		addField(escapeEditor);

		addField(new StringFieldEditor(PreferenceConstants.EXPORT_NULL_VALUE, "Null value : ",
				getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.EXPORT_SEPARATOR, "Field separator : ",
				getFieldEditorParent()));

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
