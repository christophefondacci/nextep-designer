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
/**
 *
 */
package com.nextep.datadesigner.vcs.gui.external;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import com.nextep.datadesigner.exception.DesignerException;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.ui.impl.ExceptionHandler;

/**
 * A table editor which checks the version update
 * abilities before performing an edition.
 *
 * @author Christophe Fondacci
 *
 */
public class VersionedTableEditor extends NextepTableEditor {
	/**
	 * Overridden constructor which specifies a versioned object
	 * to be controlled for this table edition
	 *
	 * @param table the SWT table to edit
	 * @param versionedParent the IVersionable object to check before edition
	 */
	private VersionedTableEditor(Table table, Object versionedParent) {
		super(table);
		table.setData(versionedParent);
	}
	/**
	 * Handles the specified table by this table editor. The versionedParent
	 * object allows callers to specify the object being monitored.
	 *
	 * @param t SWT table object to handle
	 * @param versionedParent versioned object containing the edited data
	 * @return a new TableEditor object handling this table
	 */
	public static NextepTableEditor handle(Table t, Object versionedParent) {
		return new VersionedTableEditor(t,versionedParent);
	}
	/**
	 * Overriding the default behaviour to condition the edition
	 * only on an updatable version.
	 *
	 */
	@Override
	public void handleEvent(Event arg0) {
		try {
			if(!VersionHelper.ensureModifiable(table.getData(),false)) {
				return;
			}
		} catch(DesignerException e) {
			ExceptionHandler.handle(e);
			return;
		}
		// Standard behaviour
		super.handleEvent(arg0);
	}
}
