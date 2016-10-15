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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * @author Christophe Fondacci
 */
public class DropStrategyPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private static final Log log = LogFactory.getLog(DropStrategyPreferencePage.class);

	public DropStrategyPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(), SQLGenPlugin.PLUGIN_ID));
		setDescription("Generator strategies for dropping database objects.\r\n");
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		try {
			List<IElementType> types = GeneratorFactory.getGeneratedTypes();
			Collections.sort(types, NameComparator.getInstance());
			for (IElementType t : types) {
				Map<String, IDropStrategy> availableStrategies = SQLGenPlugin.getService(
						IGenerationService.class).getAvailableDropStrategies(t);
				if (!availableStrategies.isEmpty()) {
					String[][] comboEntries = new String[availableStrategies.size()][2];
					int i = 0;
					for (String id : availableStrategies.keySet()) {
						IDropStrategy ds = availableStrategies.get(id);
						comboEntries[i][0] = ds.getName();
						comboEntries[i][1] = id;
						i++;
					}
					if (i > 1) {
						addField(new ComboFieldEditor(PreferenceConstants.DROP_STRATEGY_PREFIX
								+ t.getId().toLowerCase(), t.getCategoryTitle() + " : ",
								comboEntries, getFieldEditorParent()));
					}
				}

			}
		} catch (RuntimeException e) {
			log.error("Problems while generating the drop strategies preferences", e);
		}

	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
