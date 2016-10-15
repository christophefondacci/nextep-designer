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
package com.nextep.datadesigner.sqlgen.gui;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.impl.LightProcedure;
import com.nextep.datadesigner.sqlgen.impl.VersionedProcedure;
import com.nextep.designer.sqlgen.ui.ProcedureEditorInput;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class VersionedProcedureNavigator extends TypedNavigator {

	public VersionedProcedureNavigator(IProcedure procedure, ITypedObjectUIController controller) {
		super(procedure, controller);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {

	}

	@Override
	public String getTitle() {
		IProcedure proc = (IProcedure) getModel();
		// Only for native code (not Java)
		if (proc.getLanguageType() == LanguageType.STANDARD) {
			if (!proc.isParsed()) {
				DBGMHelper.parse(proc);
			}
		}
		return super.getTitle();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#defaultAction()
	 */
	@Override
	public void defaultAction() {
		try {
			if (getModel() instanceof LightProcedure)
				return;
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					new ProcedureEditorInput((VersionedProcedure) getModel()),
					"com.neXtep.designer.sqlgen.ui.SQLEditor"); // "com.neXtep.designer.sqlgen.ui.SQLEditor");
		} catch (PartInitException e) {
			throw new ErrorException(e);
		}
	}
}
