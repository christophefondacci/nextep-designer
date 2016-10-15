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
package com.nextep.designer.sqlgen.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;

/**
 * @author Christophe Fondacci
 */
public class SQLScriptEditorFactory implements IPersistableElement, IElementFactory {

	private ISQLScript script;

	public SQLScriptEditorFactory(ISQLScript script) {
		this.script = script;
	}

	public SQLScriptEditorFactory() {
	}

	/**
	 * @see org.eclipse.ui.IPersistableElement#getFactoryId()
	 */
	@Override
	public String getFactoryId() {
		return "com.neXtep.designer.sqlgen.ui.SQLScriptFactory"; //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento) {
		if (script != null && script.getUID() != null) {
			memento.putString("VIEW_ID", VersionHelper.getCurrentView().getUID().toString());
			memento.putString("EXTERNAL", String.valueOf(script.isExternal()));
			memento.putString("DIRECTORY", script.getDirectory());
			memento.putString("FILENAME", script.getFilename());
			if (script.getUID() != null) {
				memento.putString("ID", script.getUID().toString());
			}
		}

	}

	/**
	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
	 */
	@Override
	public IAdaptable createElement(IMemento memento) {
		String viewID = memento.getString("VIEW_ID");
		boolean isExternal = Boolean.valueOf(memento.getString("EXTERNAL"));
		String directory = memento.getString("DIRECTORY");
		String filename = memento.getString("FILENAME");
		String scriptID = memento.getString("ID");
		if (viewID == null) {
			return null;
		}
		UID viewUID = new UID(Long.valueOf(viewID));
		if (viewUID.equals(VersionHelper.getCurrentView().getUID())) {
			if (!isExternal && scriptID != null) {
				script = (ISQLScript) CorePlugin.getIdentifiableDao().load(SQLScript.class,
						new UID(Long.valueOf(scriptID)));
				return new SQLEditorInput(script);
			} else {
				script = new SQLScript(directory, filename);
				return new SQLEditorInput(script);
			}
		}
		return null;
	}

}
