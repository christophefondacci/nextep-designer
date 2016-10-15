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

import com.nextep.datadesigner.dbgm.gui.navigators.ProcedureNavigator;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;

public class LightProcedureNavigator extends ProcedureNavigator {

	private IPackage pkg;

	public LightProcedureNavigator(IProcedure p, IPackage pack) {
		super(p);
		this.pkg = pack;
		// pkg.addListener(new IEventListener() {
		// @Override
		// public void handleEvent(ChangeEvent event, IObservable source,
		// Object data) {
		// if(source!=pkg) {
		// pkg.removeListener(this);
		// pkg=(IPackage)source;
		// pkg.addListener(this);
		// }
		// }
		// });
	}

	@Override
	public void defaultAction() {
		// Delegating this action to factorized code
		SQLEditorUIServices.getInstance().openPackageProcedureEditor(pkg, (IProcedure) getModel());
	}
}
