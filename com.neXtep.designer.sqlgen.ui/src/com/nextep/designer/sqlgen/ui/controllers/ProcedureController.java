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
package com.nextep.designer.sqlgen.ui.controllers;

import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.gui.ProcedureEditorGUI;
import com.nextep.datadesigner.sqlgen.gui.VersionedProcedureNavigator;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.ui.ProcedureEditorInput;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;
import com.nextep.designer.vcs.model.IVersionable;

public class ProcedureController extends VersionableController {

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new ProcedureEditorGUI((IProcedure) content, this);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new VersionedProcedureNavigator((IProcedure) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void beforeCreation(IVersionable<?> v, Object parent) {
		IProcedure p = (IProcedure) v.getVersionnedObject().getModel();
		StringBuffer b = new StringBuffer(50);
		b.append("procedure " + p.getName() + " () ");
		if (VersionHelper.getCurrentView().getDBVendor() == DBVendor.ORACLE) {
			b.append("is");
		}
		b.append("\nbegin\n\nend;\n");
		p.setSQLSource(b.toString());
	}

	@Override
	public String getEditorId() {
		return "com.neXtep.designer.sqlgen.ui.procedureEditor"; //$NON-NLS-1$
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new ProcedureEditorInput((IProcedure) model);
	}

	@Override
	public void defaultOpen(ITypedObject model) {
		if (model instanceof IProcedure) {
			final IProcedure proc = (IProcedure) model;
			if (proc.getParent() instanceof IPackage) {
				// Delegating this action to factorized code
				SQLEditorUIServices.getInstance().openPackageProcedureEditor(
						(IPackage) proc.getParent(), proc);
			} else {
				super.defaultOpen(model);
			}
		} else {
			super.defaultOpen(model);
		}
	}
}
